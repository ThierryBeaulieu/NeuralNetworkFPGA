from abc import ABC, abstractmethod
import numpy as np
import matplotlib.pyplot as plt
from numpy.typing import NDArray
import random

enablePlot = False

class LFSR:
    def __init__(self, seed, taps):
        "Linear-Feedback shift register"
        self.state = seed
        self.taps = taps
        self.max_bits = seed.bit_length()  # Determine the bit length of the initial seed

    def next_bit(self):
        "Generate a random bit based on the seed and the taps"
        # XOR the tapped bits to get the new bit
        new_bit = 0
        for tap in self.taps:
            new_bit ^= (self.state >> (tap - 1)) & 1  # XOR bit values at tap positions

        # Shift left and add the new bit to the LSB
        self.state = ((self.state << 1) | new_bit) & ((1 << self.max_bits) - 1)
        return new_bit

    def next_number(self, bits=8):
        "Generate a number with the specified number of bits. Range [0, 255]"
        return random.randint(0, 255)

class Module:
    @abstractmethod
    def tick(self):
        pass

class B2SUnipolar(Module):
    def __init__(self):
        "Binary 2 stochastic converter in unipolar format"
        self.seed = 0b11110111
        self.taps = [7, 5, 3, 1]
        self.lfsr = LFSR(self.seed, self.taps)

    def tick(self, value: np.uint8):
        """
        Converts a binary into a unipolar probability.

        Input : value [0, 255]
        Output : {0, 1}
        """
        value = np.uint8(value)
        generatedBits = self.lfsr.next_number(self.seed.bit_length())
        return int(value > generatedBits)

class B2SBipolar(Module):
    def __init__(self):
        "Binary 2 stochastic converter in bipolar format"
        self.seed = 0b11110111
        self.taps = [7, 5, 3, 1]
        self.lfsr = LFSR(self.seed, self.taps)

    def tick(self, value: np.int8):
        """
        Converts a binary into a probability.

        Input : value [-128, 127]
        Output : {-1, 1}
        """
        value = np.int8(value)
        generatedBits = self.lfsr.next_number(self.seed.bit_length())
        generatedBits = self.twos(generatedBits)
        return int(value > generatedBits) * 2 - 1

    def twos(self, bits):
        "shifts the range from [0,255] to [-128, 127]"
        return bits - 128


class B2ISBipolar(Module):

    def __init__(self):
        """
        Binary to integral stochastic convertor
        """
        self.bpB2S = B2SBipolar()
        

    def tick(self, weightValue):
        """
        Takes a weight [-128, 127] and converts it
        to an integral stochastic stream using m=1 B2S.

        Input : weightValue [-127, 128]
        Output : {-1, 1}
        """
        return self.bpB2S.tick(weightValue)

class BitwiseOperatorAND(Module):
    def tick(self, integralValue, bit):
        """
        Takes a value in the integral stream and returns
        a 0 or the integral value according to the bit

        Input : integralValue {-m,...,m} bit {0, 1}
        Output : {0, integralValue}
        """
        if bit >= 1:
            return integralValue
        return 0
    
class IntegralAdder(Module):
    def tick(self, value1, value2):
        """
        Takes two values of the integral stream and 
        adds them together.

        Input : value1 {-m1, m1}, value2 {-m2, m2}
        Output : {-(m1+m2), 1,..,m1+m2}
        """
        return value1 + value2
    
class CounterUnipolar(Module):
    def __init__(self):
        self.nbTick = 0
        self.sum = 0

    def tick(self, stochasticBit):
        """
        Accumulate incoming binary stochastic stream.
        WARNING: This isn't synthesizable in FPGA. This
        should be done using python by using incoming data
        from FPGA
        
        Input: stochasticBit {0, 1}
        Output: uint8 [0, 255]
        """
        self.nbTick = self.nbTick + 1
        self.sum = self.sum + stochasticBit
        if self.nbTick >= 1024:
            self.nbTick = 0
            res = self.sum / 4 # floating point
            self.sum = 0
            return res
        
class NStanh(Module):
    def __init__(self, offset):
        self.counter = 0
        self.offset = offset

    def tick(self, Si, mn):
        """
        NStanh function. Using a FSM, we simulate NStanh

        Input: Si {-m,...,m}, mn {1*n,2*n,4*n...}
        Output: {0, 1}
        """
        self.counter = self.counter + Si
        if self.counter > (mn - 1):
            self.counter = mn - 1
        if self.counter < 0:
            self.counter = 0
        if self.counter > self.offset:
            return 1
        else:
            return 0
        
class Neuron(Module):

    def __init__(self, offset, m):
        """
        Single Neuron from a neural network
        """
        self.m = m
        self.pixelConverters = np.array([B2SUnipolar() for _ in range(m)])
        self.weightConverters = np.array([B2ISBipolar() for _ in range(m)])
        self.bitwiseAND = np.array([BitwiseOperatorAND() for _ in range(m)])
        self.NSthan = NStanh(offset)

    def tick(self, weights: NDArray[np.int8], pixels: NDArray[np.uint8]):
        """
        Neuron. Takes i=4 W1, W2,...,Wi weights and v1, v2,...,vi pixels.
        The pixels are an array of int8 and the weights too.

        Input: weights int8, pixels int8
        Output: {-(m1+m2+..+mi), +(m1+m2+...+mi)}
        """
        unipolarPixelsConverted = []
        for i in range(0, len(self.pixelConverters)):
            bit = self.pixelConverters[i].tick(pixels[i])
            unipolarPixelsConverted.append(bit)

        bipolarWeightsConverted = []
        for i in range(0, len(self.weightConverters)):
            integer = self.weightConverters[i].tick(weights[i])
            bipolarWeightsConverted.append(integer)

        bitwiseResults = []
        for i in range(0, len(self.bitwiseAND)):
            bitwiseResult = self.bitwiseAND[i].tick(bipolarWeightsConverted[i], unipolarPixelsConverted[i])
            bitwiseResults.append(bitwiseResult)
        
        treeAdderRes = 0
        for i in range(0, len(bitwiseResults)):
            treeAdderRes = treeAdderRes + bitwiseResults[i]
        return self.NSthan.tick(treeAdderRes, 4 * self.m)
        

class Test(Module):
    def B2ISTest(self):
        print("### B2SITest")
        B2IS = B2ISBipolar()
        weights = np.array([-128, -64, -32, -16, 0, 16, 32, 64, 127], dtype=np.int8)
        for i in range(0, len(weights)):
            stream = []
            for _ in range(0, 1024):
                stream.append(B2IS.tick(weights[i]))
            sum = 0
            for element in stream:
                sum = sum + element
            print(f"Weight {weights[i]} approx : {((sum / (len(stream))) + 1) / 2}")

    def B2STest(self):
        print("### B2STest")
        B2S = B2SUnipolar()
        pixels = np.array([0, 16, 32, 64, 128, 255], dtype=np.uint8)
        for i in range(0, len(pixels)):
            stream = []
            for _ in range(0, 1024):
                stream.append(B2S.tick(pixels[i]))
            ones = 0
            zeros = 0
            for element in stream:
                if element == 1:
                    ones = ones + 1
                else:
                    zeros = zeros +1
            print(f"Pixel {pixels[i]} approx : {(ones / (ones + zeros)) * 256}")

    def BitwiseANDTest(self):
        print("### Bitwise Adder")
        B2S = B2SUnipolar()
        B2IS = B2ISBipolar()
        bitwiseAND = BitwiseOperatorAND()

        weights = np.array([-128, 0, 127], dtype=np.int8)
        pixels = np.array([0, 16, 32, 64, 128, 255], dtype=np.uint8)

        for i in range(0, len(pixels)):
            weight = weights[2]
            stream = []
            for _ in range(0, 1024):
                sBinary = B2S.tick(pixels[i])
                sInteger = B2IS.tick(weight)
                res = bitwiseAND.tick(sInteger, sBinary)
                stream.append(res)

            sum = 0
            for element in stream:
                sum = sum + element
            # Equivalent of W * x
            multiplicationResult = sum / len(stream)
            print(f"Pixel {pixels[i]} weight {weight} mul {multiplicationResult}")

    def UnipolarCounterTest(self):
        print("### Unipolar Counter test")
        B2S = B2SUnipolar()
        unipolarCounter = CounterUnipolar()

        pixels = np.array([0, 16, 32, 64, 128, 255], dtype=np.uint8)
        for i in range(0, len(pixels)):
            result = 0
            for _ in range(0, 1024):
                res = unipolarCounter.tick(B2S.tick(pixels[i]))
                if res is not None:
                    result = res
            print(f"Pixel {pixels[i]} result {result}")

    def NStanhTest1(self):
        """
        m = 2
        offset = 4
        n = 4
        """
        nStanh = NStanh(8)
        bipolar = B2ISBipolar()
        output = []
        input = np.arange(-128, 127, 1)
        s = []
        n = 8
        # practical model
        for i in range(0, len(input)):
            stream = []
            bipolarValues = []
            for _ in range(0, 1024):
                si1 = bipolar.tick(input[i])
                si2 = bipolar.tick(input[i])
                si = si1 + si2
                m = 2
                bipolarValues.append(si)
                stream.append(2 * nStanh.tick(si, n * m) - 1)
                bipolarValues.append(si)

            sum = 0
            for j in range(0, len(stream)):
                sum = sum + stream[j]

            probability = 0
            for j in range(0, len(bipolarValues)):
                probability = probability + bipolarValues[j]

            sum = sum / len(stream)
            probability = probability / len(bipolarValues)
            s.append(probability)
            output.append(sum)

        # theorical model
        th_input = np.arange(-2.0, 2.1, 0.1)
        th_ouput = np.tanh(n * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.scatter(s, output, marker='o', label='NStanh(s)')
            plt.plot(th_input, th_ouput, color="orange", label='tanh(s)')
            plt.xlabel('s')
            plt.ylabel('ouput')
            plt.legend()
            plt.title('Approximation de NStanh avec m = 2 et offset = 2')
            plt.show()


    def NStanhTest2(self):
        """
        m = 1
        offset = 0.5
        n = 2
        """
        nStanh = NStanh(0.5)
        bipolar = B2ISBipolar()
        output = []
        input = np.arange(-128, 127, 1)
        s = []
        # practical model
        for i in range(0, len(input)):
            stream = []
            bipolarValues = []
            for _ in range(0, 1024):
                si1 = bipolar.tick(input[i])
                si = si1
                m = 1
                bipolarValues.append(si)
                stream.append(2 * nStanh.tick(si, 2 * m) - 1)

            sum = 0
            for j in range(0, len(stream)):
                sum = sum + stream[j]

            probability = 0
            for j in range(0, len(bipolarValues)):
                probability = probability + bipolarValues[j]

            sum = sum / len(stream)
            probability = probability / len(bipolarValues)
            s.append(probability)
            output.append(sum)

        # theorical model
        th_input = np.arange(-2.0, 2.1, 0.1)
        th_ouput = np.tanh(2 * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.scatter(s, output, marker='o', label='NStanh(s)')
            plt.plot(th_input, th_ouput, color="orange", label='tanh(s)')
            plt.xlabel('s')
            plt.ylabel('ouput')
            plt.legend()
            plt.title('Approximation de NStanh avec m = 1 et offset = 0.5')
            plt.show()

    def NStanhTest3(self):
        """
        m = 4
        offset = 16
        n = 8
        """
        nStanh = NStanh(16)
        bipolar = B2ISBipolar()
        output = []
        input = np.arange(-128, 127, 1)
        s = []
        n = 8
        # practical model
        for i in range(0, len(input)):
            stream = []
            bipolarValues = []
            for _ in range(0, 1024):
                si1 = bipolar.tick(input[i])
                si2 = bipolar.tick(input[i])
                si3 = bipolar.tick(input[i])
                si4 = bipolar.tick(input[i])
                si = si1 + si2 + si3 + si4
                m = 4
                bipolarValues.append(si)
                stream.append(2 * nStanh.tick(si, n * m) - 1)
                bipolarValues.append(si)

            sum = 0
            for j in range(0, len(stream)):
                sum = sum + stream[j]

            probability = 0
            for j in range(0, len(bipolarValues)):
                probability = probability + bipolarValues[j]

            sum = sum / len(stream)
            probability = probability / len(bipolarValues)
            s.append(probability)
            output.append(sum)

        # theorical model
        th_input = np.arange(-4.0, 4.1, 0.1)
        th_ouput = np.tanh(n * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.scatter(s, output, marker='o', label='NStanh(s)')
            plt.plot(th_input, th_ouput, color="orange", label='tanh(s)')
            plt.xlabel('s')
            plt.ylabel('ouput')
            plt.legend()
            plt.title('Approximation de NStanh avec m = 4 et offset = 8')
            plt.show()

    def NStanhTest4(self):
            """
            La valeur du offset est toujours (m * n) / 2
            n peut varier, mais par expérience, on obtien de bon résultats avec m = 4 
            m = 8
            offset = 16
            n = 4
            """
            nStanh = NStanh(16)
            bipolar = B2ISBipolar()
            output = []
            input = np.arange(-128, 127, 1)
            s = []
            n = 4
            # practical model
            for i in range(0, len(input)):
                stream = []
                bipolarValues = []
                for _ in range(0, 1024):
                    si1 = bipolar.tick(input[i])
                    si2 = bipolar.tick(input[i])
                    si3 = bipolar.tick(input[i])
                    si4 = bipolar.tick(input[i])
                    si5 = bipolar.tick(input[i])
                    si6 = bipolar.tick(input[i])
                    si7 = bipolar.tick(input[i])
                    si8 = bipolar.tick(input[i])
                    si = si1 + si2 + si3 + si4 + si5 + si6 + si7 + si8
                    m = 8
                    bipolarValues.append(si)
                    stream.append(2 * nStanh.tick(si, n * m) - 1)
                    bipolarValues.append(si)

                sum = 0
                for j in range(0, len(stream)):
                    sum = sum + stream[j]

                probability = 0
                for j in range(0, len(bipolarValues)):
                    probability = probability + bipolarValues[j]

                sum = sum / len(stream)
                probability = probability / len(bipolarValues)
                s.append(probability)
                output.append(sum)

            # theorical model
            th_input = np.arange(-4.0, 4.1, 0.1)
            th_ouput = np.tanh(n * th_input / 2)

            enablePlot = True
            if enablePlot:
                plt.scatter(s, output, marker='o', label='NStanh(s)')
                plt.plot(th_input, th_ouput, color="orange", label='tanh(s)')
                plt.xlabel('s')
                plt.ylabel('ouput')
                plt.legend()
                plt.title('Approximation de NStanh avec m = 8 et offset = 16')
                plt.show()


    def NeuronTest1(self):
        print("something")
        th_weight = [[-128, -128], [-64, -64], [-32, -32], [32, -32], [32, -64], [0, 0], [32, 32], [64, 64], [127, 127]]
        th_weight = [[32, -32]]
        b2is1 = B2ISBipolar()
        s = []
        y = []
        pr_res = []
        print("### Practical")
        for j in range(0, len(th_weight)):
            stream2 = []
            stream1 = []    
            stream_combined = []
            for _ in range(0, 20):
                val1 = b2is1.tick(th_weight[j][0])
                stream1.append(val1)

                val2 = b2is1.tick(th_weight[j][1])
                stream2.append(val2)

                val3 = val1 + val2
                stream_combined.append(val3)
            
            print(f"stream combined {stream_combined}")
            nStanh = NStanh(offset=2)
            m = 2
            result_after_stanh = []
            for i in range(0, len(stream_combined)):
                val = nStanh.tick(Si=stream_combined[i], mn=3*m)
                result_after_stanh.append(val)
            print(f"result after stanh {result_after_stanh}")
            sum = 0
            for i in range(0, len(result_after_stanh)):
                sum = sum + result_after_stanh[i]

            stanh_average = sum / len(result_after_stanh)
            pr_res.append(stanh_average)
            print(f"weight {th_weight[j]} result {stanh_average}")
        
        print("### Theoretical")
        pixels = [255, 255]
        th_res = []
        for j in range(0, len(th_weight)):
            product = np.dot(th_weight[j], pixels)
            product = product / 65532
            res = (np.tanh(4 * product ) + 1) / 2
            th_res.append(res)
            print(f"weight {th_weight[j]} result {res}")            
        
        print("### Analyse")
        relative_difference = []
        for i in range(0, len(th_res)):
            abs_diff = abs(th_res[i] - pr_res[i])
            print(abs_diff)
            relative_difference.append(abs_diff * 100)
        
        average_difference = np.average(relative_difference)
        print(f"Average difference (%) : {average_difference}")
        
test = Test()
# test.B2ISTest()
# test.B2STest()
# test.BitwiseANDTest()
# test.UnipolarCounterTest()
test.NStanhTest1()
# test.NStanhTest2()
# test.NStanhTest3()
# test.NStanhTest4()
# test.NeuronTest1()
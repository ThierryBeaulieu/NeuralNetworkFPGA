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
        Converts a binary into a probability.

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

    def __init__(self):
        """
        Single Neuron from a neural network
        """
        self.pixelConverters = np.array([B2SUnipolar() for _ in range(4)])
        self.weightConverters = np.array([B2ISBipolar() for _ in range(4)])
        self.bitwiseAND = np.array([BitwiseOperatorAND() for _ in range(4)])
        self.NSthan = NStanh(1)

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
        return self.NSthan.tick(treeAdderRes, 4)
        

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
            weight = weights[1]
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

    def NeuronTest(self):
        print("### Neuron test")
        neuron = Neuron()

        # pratical values
        weights = np.array([10, -64, -32, 64])
        pixels = np.array([64, 32, 64, 32])

        stream = []
        for _ in range(0, 1024):
            res = neuron.tick(weights, pixels)
            stream.append(res)

        sum = 0
        for i in range(0, len(stream)): 
            sum = sum + stream[i]

        res = sum / len(stream)
        print(f"sum {sum / 4}")
        print(f"p {res}")

        # how do I compare the neuron with a probability
        unormalized = np.dot(weights, pixels)
        normalized = (unormalized >> 8) / 256
        print(f"res {normalized}")

    def NStanhTest(self):
        nStanh = NStanh(2)
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
                si2 = bipolar.tick(input[i])
                si = si1 + si2
                m = 2
                bipolarValues.append(si)
                stream.append(2 * nStanh.tick(si, 3 * m) - 1)
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
        th_ouput = np.tanh(4 * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.plot(s, output, marker='o')
            plt.plot(th_input, th_ouput)
            plt.xlabel('s')
            plt.ylabel('ouput')
            plt.title('NStanh approximation with m = 2, offset = 2')
            plt.show()

    def TheoricalComparison(self):
        print("### Theorical Comparison")
        neuron = Neuron()

        weights_data = [[-128, -128, -128, -128], [-64, -64, -64, -64], [0, 0, 0, 0], [64, 64, 64, 64], [127, 127, 127, 127]]
        pixels_data = [[0, 0, 0, 0], [32, 32, 32, 32], [64, 64, 64, 64], [128, 128, 128, 128], [255, 255, 255, 255]]

        stochastic = []
        floating = []

        for i in range(0, len(weights_data)):
            weight = weights_data[i]

            # theorical model
            theorical_results = []
            for j in range(0, len(pixels_data)):
                pixel = pixels_data[j]
                product = np.dot(weight, pixel)
                product = (product >> 8) / 256
                res = np.tanh(product)
                theorical_results.append(float((res + 1) / 2))
                # print(f"weight {weight} pixel {pixel}")

            # print(f"theorical res {theorical_results}")
            floating.append(theorical_results)

            # practical model
            neuron = Neuron()
            practical_results = []
            for j in range(0, len(pixels_data)):
                pixel = pixels_data[j]
                bitstream = []
                for _ in range(0, 1024):
                    bit = neuron.tick(weight, pixel)
                    bitstream.append(bit)
                sum = 0
                for bit in bitstream:
                    sum = sum + bit
                sum = sum / len(bitstream)
                practical_results.append(sum)
                # print(f"weight {weight} pixel {pixel}")

            # print(f"practical res {practical_results}")
            stochastic.append(practical_results)
        
        # print("STOCHASTIC")
        # print(stochastic)
        np.save("results/stochastic.npy", stochastic)
        # print("FLOAT")
        # print(floating)
        np.save("results/floating.npy", floating)


    def testLimits(self):
        print("### Limits")
        neuron = Neuron()

        weights_data = np.load("results/weights_sampled.npy")
        pixels_data = np.load("results/pixels_sampled.npy")

        stochastic = []
        floating = []

        for i in range(0, len(weights_data)):
            weight = weights_data[i]
            print(i)

            # theorical model
            theorical_results = []
            for j in range(0, len(pixels_data)):
                pixel = pixels_data[j]
                product = np.dot(weight, pixel)
                product = (product >> 8) / 256
                res = np.tanh(product)
                theorical_results.append(float((res + 1) / 2))
                # print(f"weight {weight} pixel {pixel}")

            # print(f"theorical res {theorical_results}")
            floating.append(theorical_results)

            # practical model
            neuron = Neuron()
            practical_results = []
            for j in range(0, len(pixels_data)):
                pixel = pixels_data[j]
                bitstream = []
                for _ in range(0, 1024):
                    bit = neuron.tick(weight, pixel)
                    bitstream.append(bit)
                sum = 0
                for bit in bitstream:
                    sum = sum + bit
                sum = sum / len(bitstream)
                practical_results.append(sum)
                # print(f"weight {weight} pixel {pixel}")

            # print(f"practical res {practical_results}")
            stochastic.append(practical_results)
        
        np.save("results/stochastic_limit.npy", stochastic)
        np.save("results/floating_limit.npy", floating)

    def dataHandling(self):
        x = np.load("limit_pixels.npy")
        y = np.load("limit_weights.npy")
        x = x[::3]
        y = y[::3]
        np.save("results/pixels_sampled.npy", x)
        np.save("results/weights_sampled.npy", y)

class DataHandling():
    def analyse():
        stochastic = np.load("results/stochastic_limit.npy")
        floating = np.load("results/floating_limit.npy")

test = Test()
# test.B2ISTest()
# test.B2STest()
# test.BitwiseANDTest()
# test.UnipolarCounterTest()
# test.NStanhTest()
# test.NeuronTest()
# test.TheoricalComparison()
# test.testLimits()
# test.dataHandling()
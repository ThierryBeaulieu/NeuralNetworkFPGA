from abc import ABC, abstractmethod
from LFSR import LFSR
import numpy as np
import matplotlib.pyplot as plt

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
        Output : {0, 1}
        """
        value = np.int8(value)
        generatedBits = self.lfsr.next_number(self.seed.bit_length())
        generatedBits = self.twos(generatedBits)
        return int(value > generatedBits)

    def twos(self, bits):
        "shifts the range from [0,255] to [-128, 127]"
        return bits - 128


class B2ISBipolar(Module):

    def __init__(self):
        """
        Binary to integral stochastic convertor
        """
        self.bpB2S = B2SBipolar()
        

    def tick(self, weightValue, m):
        """
        Takes a weight [-128, 127] and converts it
        to an integral stochastic stream using m=1 B2S.

        Input : weightValue [-127, 128], m {1, 2, 4, 8}
        Output : {-m, m}
        """
        return self.bpB2S.tick(weightValue) * 2 - m

class IntegralMultiplier(Module):
    def tick(self, integralValue, bit):
        """
        Takes a value in the integral stream and returns
        a 0 or the integral value according to the bit

        Input : 
        Output : {0, integralValue}
        """
        if bit:
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
    
class Neuron(Module):
    def tick(self, weightValue, pixelValue):
        print("tick from the neurone")

class CounterBipolar(Module):
    def __init__(self):
        self.nbTick = 0
        self.sum = 0

    def tick(self, stochasticBit):
        """
        Accumulate incoming binary stochastic stream.
        Generates a int8 [-128, 127]
        """
        self.nbTick = self.nbTick + 1
        self.sum = self.sum + stochasticBit
        if self.nbTick >= 1024:
            self.nbTick = 0
            res = self.sum / 4 - 128
            self.sum = 0
            return res
        
class CounterIntegralBipolar(Module):
    def __init__(self):
        self.nbTick = 0
        self.sum = 0

    def tick(self, stochasticBit):
        """
        Accumulate incoming integral stochastic stream.
        Generates a int8 [-128, 127]
        """
        # todo fix that issue
        self.nbTick = self.nbTick + 1
        self.sum = self.sum + stochasticBit
        if self.nbTick >= 512:
            self.nbTick = 0
            
            res = (self.sum / 4) - 128
            self.sum = 0
            return res

class CounterUnipolar(Module):
    def __init__(self):
        self.nbTick = 0
        self.sum = 0

    def tick(self, stochasticBit):
        """
        Accumulate incoming binary stochastic stream.
        Generates a uint8 [0, 255]
        """
        self.nbTick = self.nbTick + 1
        self.sum = self.sum + stochasticBit
        if self.nbTick >= 1024:
            self.nbTick = 0
            res = self.sum / 4
            self.sum = 0
            return res
        
class NStanh(Module):
    def tick(self, Si, mn):
        """
        NStanh function. Using a FSM, we simulate NStanh

        Input: Si {-m,...,m}, m {1,2,4...}
        Output: {0, 1}
        """
        counter = 0
        offset = 0
        counter = counter + Si
        if counter > mn - 1:
            counter = mn - 1
        if counter < 0:
            counter = 0
        if counter > offset:
            return 1
        else:
            return 0


class Test(Module):
    def __init__(self):

        self.upB2S = B2SUnipolar()
        self.upCounter = CounterUnipolar()

        self.bpB2S = B2SBipolar()
        self.bpCounter = CounterBipolar()

        self.bpB2IS = B2ISBipolar()
        self.bpiCounter = CounterIntegralBipolar()

        self.integralAdder = IntegralAdder()
        self.integralMultiplier = IntegralMultiplier()

        

    def executeNStanhTest(self):
        print("# NStanh test")
        input = np.arange(-128, 127.1, 0.1)
        nstanh = NStanh()
        m = 1
        res = []
        value = []

        for i in range(0, len(input)):
            si = self.bpB2IS.tick(input[i], m)
            value.append(si)
            tanVal = 2 * nstanh.tick(si, 4 * m) - 1
            res.append(tanVal)

        theorical_input = np.arange(-1.0, 1.01, 0.01)
        theoretical_output = np.tanh(theorical_input)

        # plot a graphic here
        #plt.plot(theorical_input, theoretical_output)
        plt.plot(value, res, marker='o')
        plt.show()


    def executeMultiplierAdderTest(self):
        print("# Multiplier Adder test")
        clock_cycles = 512
        for _ in range(0, clock_cycles):
            stream1 = self.bpB2IS.tick(0)
            stream2 = self.bpB2IS.tick(0)

            sum = self.integralAdder.tick(stream1, stream2)
            res = self.bpiCounter.tick(sum)
            if res is not None:
                print(f"sum integral {res}")


    def executeConversionTest(self):
        print("# Conversion test")
        clock_cycles = 1024
        for _ in range(0, clock_cycles):
            bpB2SOutput = self.bpB2S.tick(-127)
            bpCounterOutput = self.bpCounter.tick(bpB2SOutput)

            if bpCounterOutput is not None:
                print(f"bipolar {bpCounterOutput}")

            upB2SOutput = self.upB2S.tick(128)
            upCounterOutput = self.upCounter.tick(upB2SOutput)

            if upCounterOutput is not None:
                print(f"unipolar {upCounterOutput}")

test = Test()
# test.executeConversionTest()
# test.executeMultiplierAdderTest()
test.executeNStanhTest()
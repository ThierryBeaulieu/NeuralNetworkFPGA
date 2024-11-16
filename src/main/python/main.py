from abc import ABC, abstractmethod
from LFSR import LFSR
import numpy as np

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
        self.bpB2S1 = B2SBipolar()
        self.bpB2S2 = B2SBipolar()
        

    def tick(self, weightValue):
        """
        Takes a weight [-128, 127] and converts it
        to an integral stochastic stream using m=2 B2S
        Input : weightValue [-127, 128], m {1, 2, 4, 8}
        Output : {0, 1, ..., m}
        """
        bit1 = self.bpB2S1.tick(weightValue)
        bit2 = self.bpB2S2.tick(weightValue)
        return bit2 + bit1

class Multiplier(Module):
    def tick(self, integralValue: np.int8, bit):
        """
        Takes a value in the integral stream and returns
        a 0 or the integral value according to the bit
        Input : integralValue {0, 1,..,m} , bit {0, 1}
        Output : {0, integralValue}
        """
        if bit:
            return integralValue
        return 0
    
class Adder(Module):
    def tick(self, value1, value2):
        """
        Takes two values of the integral stream and 
        adds them together.
        Input : value1 {0, 1,..,m1}, value2 {0, 1,..,m2}
        Output : {0, 1,..,m1+m2}
        """
        return value1 + value2
    
class Neuron(Module):
    def __init__(self):
        self.b2Is = B2IS()
        self.b2s = B2S()
        self.multiplier = Multiplier()

    def tick(self, weightValue, pixelValue):
        print("tick from the neurone")
        integralBit = self.b2IS.tick(weightValue)
        bit = self.b2s.tick(pixelValue)
        multiplierResult = self.multiplier(bit, integralBit)

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
        self.nbTick = self.nbTick + 1
        self.sum = self.sum + stochasticBit
        if self.nbTick >= 1024:
            self.nbTick = 0
            m = 2
            res = (self.sum / (4 * m)) - 128
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

clock_cycles = 1024

upB2S = B2SUnipolar()
upCounter = CounterUnipolar()

bpB2S = B2SBipolar()
bpCounter = CounterBipolar()

bpB2IS = B2ISBipolar()
bpiCounter = CounterIntegralBipolar()

for i in range(0, clock_cycles):
    # clock cycle stuff
    bpB2SOutput = bpB2S.tick(-127)
    bpCounterOutput = bpCounter.tick(bpB2SOutput)

    if bpCounterOutput is not None:
        print(f"bipolar {bpCounterOutput}")

    upB2SOutput = upB2S.tick(128)
    upCounterOutput = upCounter.tick(upB2SOutput)

    if upCounterOutput is not None:
        print(f"unipolar {upCounterOutput}")

    bpB2ISOutput = bpB2IS.tick(-8)
    bpiCounterOutput = bpiCounter.tick(bpB2ISOutput)

    if bpiCounterOutput is not None:
        print(f"bipolar integral {bpiCounterOutput}")

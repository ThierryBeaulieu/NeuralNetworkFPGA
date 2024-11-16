from abc import ABC, abstractmethod
from LFSR import LFSR
import random
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

    def tick(self, pixelValue: np.uint8):
        """
        Converts a binary into a probability.
        Pixel is uint8 [0, 255]
        """
        pixelValue = np.uint8(pixelValue)
        generatedBits = self.lfsr.next_number(self.seed.bit_length())
        return int(pixelValue > generatedBits)

class B2SBipolar(Module):
    def __init__(self):
        "Binary 2 stochastic converter in bipolar format"
        self.seed = 0b11110111
        self.taps = [7, 5, 3, 1]
        self.lfsr = LFSR(self.seed, self.taps)

    def tick(self, pixelValue: np.int8):
        """
        Converts a binary into a probability.
        Pixel is int8 [-128, 127]
        """
        pixelValue = np.int8(pixelValue)
        generatedBits = self.lfsr.next_number(self.seed.bit_length())
        generatedBits = self.twos(generatedBits)
        return int(pixelValue > generatedBits)

    def twos(self, bits):
        "shifts the range from [0,255] to [-128, 127]"
        return bits - 128


class B2ISUnipolar(Module):
    def tick(self, weightValue):
        number = random.choice([0, 1, 2])
        return number

class Multiplier(Module):
    def tick(self, bit, integralBit):
        return bit & integralBit
    
class Adder(Module):
    def tick(self, integralBit1, integralBit2):
        return integralBit1 + integralBit2
    
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
b2Is = B2ISUnipolar()
bpCounter = CounterBipolar()

upB2s = B2SUnipolar()
bpB2s = B2SBipolar()
upCounter = CounterUnipolar()
for i in range(0, clock_cycles):
    # clock cycle stuff
    bpB2sOutput = bpB2s.tick(-127)
    bpCounterOutput = bpCounter.tick(bpB2sOutput)

    upB2sOutput = upB2s.tick(128)
    upCounterOutput = upCounter.tick(upB2sOutput)

    if bpCounterOutput is not None:
        print(f"bipolar {bpCounterOutput}")

    if upCounterOutput is not None:
        print(f"unipolar {upCounterOutput}")
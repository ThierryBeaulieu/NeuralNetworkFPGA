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
    
class CounterUnipolar(Module):
    def __init__(self):
        self.nbTick = 0
        self.sum = 0

    def tick(self, stochasticBit):
        """
        Accumulate incoming binary stochastic stream.
        
        Input: stochasticBit {0, 1}
        Output: uint8 [0, 255]
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
        
class Neuron(Module):

    def __init__(self):
        """
        Single Neuron from a neural network
        """
        self.pixelConverters = np.array([B2SUnipolar() for _ in range(401)])
        self.weightConverters = np.array([B2ISBipolar() for _ in range(401)])
        self.adders = np.array([BitwiseOperatorAND() for _ in range(401)])
        self.NSthan = NStanh()

    def tick(self, weights: NDArray[np.int8], pixels: NDArray[np.uint8]):
        """
        Neuron. Takes i=401 W1, W2,...,Wi weights and v1, v2,...,vi pixels.
        The pixels are an array of int8 and the weights too.

        Input: weights int8, pixels int8
        Output: {-(m1+m2+..+mi), +(m1+m2+...+mi)}
        """
        print("tick from the neurone")
        unipolarPixelsConverted = []
        for i in range(0, len(self.pixelConverters)):
            bit = self.pixelConverters[i].tick(pixels[i])
            unipolarPixelsConverted.append(bit)

        bipolarWeightsConverted = []
        for i in range(0, len(self.weightConverters)):
            integer = self.weightConverters[i].tick(weights[i])
            bipolarWeightsConverted.append(integer)

        bitwiseResults = []
        for i in range(0, len(self.adders)):
            bitwiseResult = self.adders[i].tick(bipolarWeightsConverted[i], unipolarPixelsConverted[i])
            bitwiseResults.append(bitwiseResult)

        si = 0
        for i in range(0, len(bitwiseResults)):
            si = si + bitwiseResults[i]

        return self.NSthan.tick(si, 4)
        

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

    def NSthanTest(self):
        print("### NSthan test")
        nstanh = NStanh()

test = Test()
# test.B2ISTest()
# test.B2STest()
test.BitwiseANDTest()
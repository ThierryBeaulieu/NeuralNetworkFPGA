from abc import ABC, abstractmethod
import numpy as np
import matplotlib.pyplot as plt
from numpy.typing import NDArray
import random
import math

enablePlot = False


class LFSR:
    def __init__(self, seed, taps):
        "Linear-Feedback shift register"
        self.state = seed
        self.taps = taps
        self.max_bits = (
            seed.bit_length()
        )  # Determine the bit length of the initial seed

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
    """
    Takes an integer in [0,255] and converts it to Stochastic Stream in Unipolar format
    """

    def __init__(self):
        "Binary 2 stochastic converter in unipolar format"
        self.seed = np.random.randint(0, 255, dtype=int)
        self.taps = [7, 5, 3, 1]
        self.lfsr = LFSR(self.seed, self.taps)

    def tick(self, value):
        """
        Converts a binary into a unipolar probability.

        Input : value [0, 255]
        Output : {0, 1}
        """
        generatedBits = self.lfsr.next_number(self.seed.bit_length())
        return int(value > generatedBits)

class B2ISBipolar(Module):

    def __init__(self, m: int):
        """
        Binary to integral stochastic convertor
        """
        self.m = m
        self.bpB2Ss = [B2SUnipolar() for _ in range(self.m)]

    def tick(self, weightValue):
        """
        Takes a weight [-128, 127] and converts it
        to an integral stochastic stream using self.m B2S.

        Input : weightValue [-127, 128]
        Output : [m, m]
        """
        x = weightValue + self.m
        res = 0
        for b2s in self.bpB2Ss:
            bit = b2s.tick(x)
            res += bit

        s = 2 * res - self.m
        return s


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
            res = self.sum / 4  # floating point
            self.sum = 0
            return res


class NStanh(Module):
    def __init__(self, offset):
        self.counter = offset
        self.offset = offset

    def tick(self, Si, mn):
        """
        NStanh function. Using a FSM, we simulate NStanh

        Input: Si {-m,...,m}, mn {1*n,2*n,4*n...}
        Output: {0, 1}
        """
        self.counter = self.counter + Si
        # print(f"NStanh counter {self.counter}")
        if self.counter > (mn - 1):
            self.counter = mn - 1
        if self.counter < 0:
            self.counter = 0
        if self.counter > self.offset:
            return 1
        else:
            return 0


class Neuron(Module):

    def __init__(self, weightIndex: int, weights=None, n=4, m=8):
        """
        Single Neuron from a neural network
        """
        self.weights = np.loadtxt("resources/weights.csv", delimiter=",")
        self.weights = self.weights[weightIndex]
        if weights is not None:
            self.weights = weights
        self.n = n
        self.m = m

        self.b2ISBipolar = B2ISBipolar(m=128)
        self.b2sUnipolar = B2SUnipolar()
        self.bitwiseAND = BitwiseOperatorAND()
        self.nstanh = NStanh(offset=(m * n / 2))
        self.lastSi = 0

    def tick(self, pixels):
        """
        Neuron. Takes i=4 W1, W2,...,Wi weights and v1, v2,...,vi pixels.
        The pixels are an array of int8 and the weights too.

        Input: pixels int8
        Output: {0, 1}
        """
        si = 0
        for i in range(0, len(pixels)):
            bipolar = self.b2ISBipolar.tick(self.weights[i])
            unipolar = self.b2sUnipolar.tick(pixels[i])
            bitwiseAND = self.bitwiseAND.tick(bipolar, unipolar)
            si += bitwiseAND
        self.lastSi = si
        sthanRes = self.nstanh.tick(si, self.m * self.n)
        # print(f"unipolar {unipolar} bipolar {bipolar} bitwiseAND {bitwiseAND} si {si} stanhRes {sthanRes}")
        return sthanRes

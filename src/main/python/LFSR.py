class LFSR:
    def __init__(self, seed, taps):
        self.state = seed
        self.taps = taps
        self.max_bits = seed.bit_length()  # Determine the bit length of the initial seed

    def next_bit(self):
        # XOR the tapped bits to get the new bit
        new_bit = 0
        for tap in self.taps:
            new_bit ^= (self.state >> (tap - 1)) & 1  # XOR bit values at tap positions

        # Shift left and add the new bit to the LSB
        self.state = ((self.state << 1) | new_bit) & ((1 << self.max_bits) - 1)
        return new_bit

    def next_number(self, bits=8):
        # Generate a number with the specified number of bits
        number = 0
        for _ in range(bits):
            number = (number << 1) | self.next_bit()
        return number

# Example usage
seed = 0b101  # A seed with at least one bit set
taps = [7, 5, 3, 1]  # Tap positions (for example, a 7-bit LFSR)
lfsr = LFSR(seed, taps)

# Generate a random number with 8 bits
for i in range(0, 100):
    random_number = lfsr.next_number(seed.bit_length())
    print(f"Random 8-bit number: {random_number}")
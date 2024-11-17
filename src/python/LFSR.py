

# Example usage
# seed = 0b101  # A seed with at least one bit set
# taps = [7, 5, 3, 1]  # Tap positions (for example, a 7-bit LFSR)
# lfsr = LFSR(seed, taps)
# 
# # Generate a random number with 8 bits
# for i in range(0, 100):
#     random_number = lfsr.next_number(seed.bit_length())
#     print(f"Random 8-bit number: {random_number & 1}")
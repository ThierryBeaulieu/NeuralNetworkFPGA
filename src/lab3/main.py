import numpy as np

# Converting the values of the pixels into Fixed-Point notation using two's complement

def to_fixed_point(value, bits=8, frac_bits=7):
    scale = 1 << frac_bits  # 2^frac_bits
    max_val = (1 << (bits - 1)) - 1  # Maximum value for Q0.7
    scaled_value = round(value * scale)
    fixed_point_value = min(max(scaled_value, 0), max_val)  # Clip to range
    return fixed_point_value

def to_signed_fixed_point(value, bits=8, frac_bits=7):
    scale = 1 << frac_bits  # 2^frac_bits
    min_val = -(1 << (bits - 1))  # Minimum value for Q1.7
    max_val = (1 << (bits - 1)) - 1  # Maximum value for Q1.7
    scaled_value = round(value * scale)
    fixed_point_value = min(max(scaled_value, min_val), max_val)  # Clip to range
    if fixed_point_value < 0:  # Two's complement for negatives
        fixed_point_value = (1 << bits) + fixed_point_value
    return fixed_point_value


# Step 1. Converts Pixel Values To Fixed Point Notation
images = np.load("x.npy")
imagesInt8 = np.zeros_like(images)
for i in range(0, len(images)):
    image = images[i]
    for j in range(0, len(image)):
        pixel = image[j]
        imagesInt8[i][j] = to_fixed_point(pixel)

print(np.min(imagesInt8))
print(np.max(imagesInt8))

# Example usage
values = [0.5, -0.5, 0.25, -0.25, -1, 1]
binary_representations = [to_signed_fixed_point(v) for v in values]
print(binary_representations)

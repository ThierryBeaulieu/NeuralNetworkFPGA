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


# Step 1. Converts Pixel Values To Fixed Point Notation [1:7]
images = np.load("x.npy")
imagesInt8 = np.zeros((5000, 401))
for i in range(0, len(images)):
    image = images[i]
    image = np.hstack((1, image))
    for j in range(0, len(image)):
        pixel = image[j]
        imagesInt8[i][j] = to_fixed_point(pixel)


# Step 2. Converts Weights And Saves Them In CSV [2:6]
# Hidden Layer 1 [-1.463356, 1.00899]
weightsHidden1 = np.load("theta_0.npy") # (25, 401)
weightsHidden1Int8 = np.zeros((26, 401))
for i in range(0, len(weightsHidden1)):
    weightRow = weightsHidden1[i]
    for j in range(0, len(weightRow)):
        weight = weightRow[j]
        weightsHidden1Int8[i][j] = to_signed_fixed_point(weight, 8, 6)

np.savetxt('theta_0_int8.csv', weightsHidden1Int8, delimiter=',', fmt='%d')

# Hidden Layer 2 [-4.0308, 3.2115848]
weightsHidden2 = np.load("theta_1.npy") # (25, 401)
weightsHidden2Int8 = np.zeros_like(weightsHidden2)
for i in range(0, len(weightsHidden2)):
    weightRow = weightsHidden2[i]
    for j in range(0, len(weightRow)):
        weight = weightRow[j]
        weightsHidden2Int8[i][j] = to_signed_fixed_point(weight, 8, 4)

np.savetxt('theta_1_int8.csv', weightsHidden2Int8, delimiter=',', fmt='%d')

# Step 3. Make The Multiplication W*X
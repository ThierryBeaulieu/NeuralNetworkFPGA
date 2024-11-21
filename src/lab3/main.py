import numpy as np

def sigmoid(x):
    return 1 / (1 + np.exp(-x))

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
imagesInt8 = np.zeros(401)
imgIndex = 0

image = images[imgIndex]
image = np.hstack((1, image))
for i in range(0, len(image)):
    pixel = image[i]
    imagesInt8[i] = to_signed_fixed_point(pixel, 8, 7)

# Step 2. Converts Weights And Saves Them In CSV [2:6]
# Hidden Layer 1 [-1.463356, 1.00899]
weightsHidden1 = np.load("theta_0.npy") # (25, 401)
weightsHidden1Int8 = np.zeros((25, 401))
for i in range(0, len(weightsHidden1)):
    weightRow = weightsHidden1[i]
    for j in range(0, len(weightRow)):
        weight = weightRow[j]
        weightsHidden1Int8[i][j] = to_signed_fixed_point(weight, 8, 6)

np.savetxt('theta_0_int8.csv', weightsHidden1Int8, delimiter=',', fmt='%d')

# Hidden Layer 2 [-4.0308, 3.2115848]
weightsHidden2 = np.load("theta_1.npy") # (10, 26)
weightsHidden2Int8 = np.zeros_like(weightsHidden2)
for i in range(0, len(weightsHidden2)):
    weightRow = weightsHidden2[i]
    for j in range(0, len(weightRow)):
        weight = weightRow[j]
        weightsHidden2Int8[i][j] = to_signed_fixed_point(weight, 8, 4)

np.savetxt('theta_1_int8.csv', weightsHidden2Int8, delimiter=',', fmt='%d')
# Step 3. Multiplication W*X First Hidden Layer
firstHiddenLayerResult = np.zeros(25)
# print(weightsHidden1Int8.shape) # (25, 401)
for i in range(0, weightsHidden1Int8.shape[0]):
    weights = weightsHidden1Int8[i]
    sum = 0
    for j in range(0, len(weights)):
        weight = int(weights[j])
        isNegative = False
        if weight > 127:
            isNegative = True
            weight = 2**8 - weight
        pixel = imagesInt8[j]
        tmp = pixel * weight
        if isNegative and tmp != 0:
            tmp = 2**16 - tmp
        # print(f"w {weights[j]} p {imagesInt8[j]} res {tmp}")
        # tmp is 16 bits (log_2(401 * 16 bits) = 24.64) donc 25 bits 
        # pour représenter
        sum = sum + tmp
    firstHiddenLayerResult[i] = sum
print(2**25)

print(firstHiddenLayerResult)
# w = 1/(2**5) + 1/(2**6)
# p = 1/(2**3) + 1/(2**4) + 1/(2**5) + 1/(2**7)
# expected = 1/(2**7) + 1/(2**9) + 1/(2**11) + 1/(2**12) + 1/(2**13)
# print(f"pos res {p * w} == {expected}")
# 
# 
# w = -1 + 1/(2**3) + 1/(2**4) + 1/(2**5)
# p = 1/(2**1) + 1/(2**3) + 1/(2**4) + 1/(2**5) + 1/(2**7)
# expected = -1 + 1/(2**2) + 1/(2**3) + 1/(2**6) + 1/(2**6) + 1/(2**7) + 1/(2**9) + 1/(2**11) + 1/(2**12)
# print(f"neg res {p * w} == {expected}")
# 
# w = -1 + 1/(2**1) + 1/(2**2) + 1/(2**3) + 1/(2**4)+ 1/(2**5)
# p = 1/(2**1) + 1/(2**3) + 1/(2**4) + 1/(2**7)
# expected = -1 + 1/(2**1) + 1/(2**2) + 1/(2**3) + 1/(2**4) + 1/(2**5) + 1/(2**7) + 1/(2**10) + 1/(2**11) +1/(2**12)
# print(f"neg res {p * w} == {expected}")


# Verification
# px = 2/(2**3) + 2/(2**4) + 2/(2**5) + 2/(2**7)
# wt = 2/(2**5) + 2/(2**6)
# app = 2/(2**6) + 2/(2**8) + 2/(2**10) + 2/(2**11) + 2/(2**12)
# print(f"pixel {px} weight {wt} res {px * wt}")
# print(f"res {px * wt} == {app}")


# Step 4. Create CSV containing Sigmoid Operations

# print("For positive")
# sum = 0
# for i in range(1, 14):
#     sum = sum + 1/(2**i)
# print(f"max value of {sum}")
# print(f"min value {1/(2**13)}")
# 
# print("For negative")
# sum = -1
# for i in range(1, 14):
#     sum = sum + 1/(2**i)
# print(f"max value of {sum}")
# print(f"min value {1/(2**13)}")

# Step 5. Create Sigmoid Approximation for the First Hidden Layer
# Le résultat obtenu précédemment est sur 416 bits
sigmoidApprox = np.zeros(65536)
sum = 0.0
for i in range(0, 65536): # [0 -> 65535]
    if i == 32768:
        sum = -4.0
    sigValue = sigmoid(sum)
    # print(f"sum {sum} sigmoid {sigValue}")
    sigmoidApprox[i] = to_signed_fixed_point(sigValue, 8, 7)
    sum += 1/2**13

np.save("sigmoid.npy", sigmoidApprox)

# Step 4. Sigmoid operation First Hidden Layer
sigmoidTmp = np.zeros(25)
for i in range(len(firstHiddenLayerResult)):
    sigmoidTmp[i] = sigmoidApprox[firstHiddenLayerResult[i]]

sigmoidHiddenLayer1 = np.hstack((1, sigmoidTmp))

print(sigmoidHiddenLayer1)




# Step 5. Add 1 to the result of the Sigmoid

# Step 6. Multiplication W*X Second Hidden Layer

# Step 7. Sigmoid operation Second Hidden Layer

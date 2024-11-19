import numpy as np

# test weights 14

def sigmoid(x):
    return 1 / (1 + np.exp(-x))

def convert_float_to_fix_point(floating_value: float, signed: int, integer: int, fractional: int):
    # With fixed precision, only certain values are possible
    # 
    # For instance, the possible values for 1 bit signed, 1 bit integer and 2 bits signed are :
    # 1111, 1110, 1101, 1100, 1011, 1010, 1001, 1000, 0000, 0001, 0010, 0011, 0100, 0101, 0110, 0111
    # {-1.75, -1.50, -1.25, -1.0, -0.75, -0.5, -0.25, -0.0, 0.0 , 0.25, 0.50, 0.75, 1.00, 1.25, 1.50, 1.75}

    # The fixed point notation only affects the uncertainty 
    # We therefore can keep "floating point values" by reducing there precision
    # This avoid converting floating points to binary and then converting a binary back
    # to the floating point notation
    if signed > 1:
        raise Exception("The signed bit can't be more than 1")
    if signed == 0 and floating_value < 0:
        raise Exception("Can't convert the negative floating number without a bit for the sign")
    if (signed + integer + fractional) not in [4, 6, 8, 10, 12, 14, 16]:
        raise Exception("The sum doesn't add up to a valid configuration")
    
    res = 0.0
    integer_part = abs(int(floating_value))
    fractional_part = abs(floating_value) - integer_part

    # This is the maximum value an integer can be according to precision
    max_integer = {n: (2 ** n) - 1 for n in range(17)}

    # This is the maximum value of fractional part according to precision
    max_fractional = {n: 1 / (2 ** n) for n in range(17)} 

    if integer_part > max_integer[integer]:
        integer_part = max_integer[integer]

        for i in range(1, fractional + 1):
            fractional_part = fractional_part + max_fractional[i]

        res = integer_part + fractional_part
        if signed == 1 and floating_value < 0.0:
            res = res * -1
        return res
    
    rest = fractional_part
    fractional_part = 0
    for i in range(1, fractional + 1):
        if rest > max_fractional[i]:
            rest -= max_fractional[i]
            fractional_part += max_fractional[i]
            
    res = integer_part + fractional_part

    if signed == 1 and floating_value < 0.0:
        res = res * -1
        
    return res

# Step 1. Load the data
x = np.load('x.npy') # 5000 images
y = np.load('y.npy')

theta_0 = np.load('theta_0.npy')
theta_1 = np.load('theta_1.npy')


sum = 0
for imgIndex in range(0, 5000):
    print(imgIndex)
    image = x[imgIndex]
    xp = np.hstack((1, image))

    image_fp = np.zeros_like(xp)

    for i in range(xp.shape[0]):
        image_fp[i] = convert_float_to_fix_point(xp[i], 0, 0, 4)

    ## Step 3. Convert Weight Precision Into Fixed Point Precision
    # {4, 4} [-1.463356, 1.00899]
    theta_0_fp = np.zeros_like(theta_0)
    for i in range(theta_0.shape[0]): # 25
        for j in range(theta_0.shape[1]): # 401
            theta_0_fp[i][j] = convert_float_to_fix_point(theta_0[i][j], 1, 1, 12)


    ## Step 4. Make The First Hidden Layer Multiplication X*W 
    # {4, 4} [-3.78125, 4.796875]
    # {8, 4} [-3.9677734375, 5.0673828125]
    hiddenLayer_fp = np.zeros(theta_0.shape[0])
    for i in range(0, len(theta_0_fp)): # [0, 24]
        row = theta_0_fp[i]
        product = 0
        for j in range(0, len(row)): # [0, 400]
            weight_fp = row[j]
            pixel_fp = image_fp[j]
            product = product + weight_fp * pixel_fp
        hiddenLayer_fp[i] = product

    ## Step 5. Apply the Sigmoid Function For First Hidden Layer
    sigmoid1_fp_tmp = np.zeros(hiddenLayer_fp.shape[0])

    for i in range(0, len(hiddenLayer_fp)):
        tmp = sigmoid(hiddenLayer_fp[i])
        # Range of a sigmoid is always [0, 1]
        sigmoid1_fp_tmp[i] = tmp
    sigmoid1_fp = np.hstack((1, sigmoid1_fp_tmp))

    ## Step 6. Convert Weight of Second Layer Precision Into Fixed Point Precision
    # weight [-4.0308, 3.2115848]
    # 4 bits [1 signed, 3 integer, 0 fractional]
    theta_1_fp = np.zeros_like(theta_1)
    for i in range(theta_1.shape[0]): # 10
        for j in range(theta_1.shape[1]): # 26
            theta_1_fp[i][j] = convert_float_to_fix_point(theta_1[i][j], 1, 3, 10)

    ## Step 7. Make The Second Hidden Layer Multiplication X*W 
    # Result [-6.9375, 0.3125]
    # 4 bits [1 signed, 3 integer]
    hiddenLayer2_fp = np.zeros(theta_1_fp.shape[0])

    for i in range(0, len(theta_1_fp)): # [0, 9]
        row = theta_1_fp[i]
        product = 0
        for j in range(0, len(row)): # [0, 26]
            weight_fp = row[j]
            pixel_fp = sigmoid1_fp[j]
            product = product + weight_fp * pixel_fp
        hiddenLayer2_fp[i] = product

    ## Step 8. Apply the Sigmoid Function For Second Hidden Layer
    sigmoid2_fp = np.zeros(hiddenLayer2_fp.shape[0])

    for i in range(0, len(hiddenLayer2_fp)):
        tmp = sigmoid(hiddenLayer2_fp[i])
        # Range of a sigmoid is always [0, 1]
        sigmoid2_fp[i] = tmp

    # Calculer dans le notebook
    pred = sigmoid2_fp.argmax()
    if pred == y[imgIndex]:
        sum += 1

# Get precentage
average = (sum / 5000) * 100
print(f"8, 14 is {average}%")

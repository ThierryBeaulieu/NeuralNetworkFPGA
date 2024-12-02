import numpy as np

debug = False

## Step 0. Define Sigmoid Function And Load Assets
def sigmoid(x):
    return 1 / (1 + np.exp(x))

imageMatched = 0
y = np.load("y.npy")
theta0 = np.load("theta_0.npy")
theta1 = np.load("theta_1.npy")


for i in range(0, 5000):
    if i % 100 == 0:
        print(i)
    ## Step 1. Represent the Theta_0 Weights In a Fixed Point Representation
    # [2, 2]
    if debug:
        print(f"theta0 min {np.min(theta0)}") # -1.463369
        print(f"theta0 max {np.max(theta0)}") # 1.0089920

    weightPrecision0 = 2 # 2, 4, 6, 8 ...
    theta0_Int8 = np.array([x * 2**weightPrecision0 for x in theta0]).astype(np.int8) # << 2


    ## Step 2. Represent the Images In a Fixed Point Representation
    images = np.load("x.npy")

    # [2, 2]
    if debug:    
        print(f"image min {np.min(images)}") # 0.0
        print(f"image max {np.max(images)}") # 1.0

    image = images[i]
    xp = np.hstack((1, image))

    imagePrecision = 2 # 2
    image_Int8 = np.array([x * 2**imagePrecision for x in xp]).astype(np.int8) # << 2


    ## Step 3. We Make the Dot Product Between Image Int8 and Weight Int8
    hiddenLayer0_Int8 = np.dot(image_Int8, theta0_Int8.T) # [4,4]


    ## Step 4. Apply the Sigmoid To the Result Hidden Layer 0
    hiddenLayer0Precision = 4
    hiddenLayer0_Float = np.array([x/2**hiddenLayer0Precision for x in hiddenLayer0_Int8]).astype(float) # << 2

    # [2, 14]
    sigmoid0Precision = 14
    sig0_Float_tmp = np.array([sigmoid(x) for x in hiddenLayer0_Float]).astype(float) # [0, 1]
    sig0_Float = np.hstack((1, sig0_Float_tmp))
    sig0_Int8 = np.array([x * 2**sigmoid0Precision for x in sig0_Float]).astype(np.int8)


    ## Step 5. Represent the Theta_1 Weights In a Fixed Point Representation

    # [3, 1]
    if debug:
        print(f"theta1 min {np.min(theta1)}") # -4.030847
        print(f"theta1 max {np.max(theta1)}") # 3.2115848

    weightPrecision1 = 1
    theta1_Int8 = np.array([x * 2**weightPrecision1 for x in theta1]).astype(np.int8) # << 2


    ## Step 6. We Make the Dot Product Between Image Int8 and Weight Int8
    hiddenLayer1_Int8 = np.dot(sig0_Int8, theta1_Int8.T) # [5,15]


    ## Step 7. Apply the Sigmoid To the Result
    hiddenLayer1Precision = 15
    hiddenLayer1_Float = np.array([x/2**hiddenLayer1Precision for x in hiddenLayer1_Int8]).astype(float) # << 2

    # [2, 14]
    sigmoid1Precision = 14
    sig1_Float = np.array([sigmoid(x) for x in hiddenLayer1_Float]).astype(float) # [0, 1]
    sig1_Int8 = np.array([x * 2**sigmoid1Precision for x in sig1_Float]).astype(np.int8)

    ## Step 8. Compare the result
    if debug:
        print("FPGA Output:", sig1_Int8)

    # Calculer dans le notebook
    pred = sig1_Int8.argmax()
    if pred == y[i]:
        imageMatched += 1

    if debug:
        print("Prediction:", pred, "Label:", y[i])

print(f"Success {(imageMatched/5000.0) * 100}%")
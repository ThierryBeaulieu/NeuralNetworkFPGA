import numpy as np

debug = False

## Step 0. Define Sigmoid Function And Load Assets
def sigmoid(x):
    return 1 / (1 + np.exp(-x))

y = np.load("y.npy")
theta0 = np.load("theta_0.npy")
theta1 = np.load("theta_1.npy")

def compute(imageIndex):
    ## Step 1. Represent the Theta_0 Weights In a Fixed Point Representation
    # [2, 6]
    if debug:
        print(f"theta0 min {np.min(theta0)}") # -1.463369
        print(f"theta0 max {np.max(theta0)}") # 1.0089920

    weightPrecision0 = 6 # 2, 4, 6, 8 ...
    theta0_Int8 = np.array([x * 2**weightPrecision0 for x in theta0]).astype(np.int8) # << 2


    ## Step 2. Represent the Images In a Fixed Point Representation
    images = np.load("x.npy")

    # [2, 6]
    if debug:    
        print(f"image min {np.min(images)}") # 0.0
        print(f"image max {np.max(images)}") # 1.0

    image = images[imageIndex]
    xp = np.hstack((1, image))

    imagePrecision = 6 # 2
    image_Int8 = np.array([x * 2**imagePrecision for x in xp]).astype(np.int16) # << 2


    ## Step 3. We Make the Dot Product Between Image Int8 and Weight Int8
    hiddenLayer0_Int8 = np.dot(image_Int8, theta0_Int8.T) # [4,12]


    ## Step 4. Apply the Sigmoid To the Result Hidden Layer 0
    hiddenLayer0Precision = 12
    hiddenLayer0_Float = np.array([x/2**hiddenLayer0Precision for x in hiddenLayer0_Int8]).astype(float) # << 2

    # print(f"hiddenLayer0 {hiddenLayer0_Float}")
    # [2, 14]
    sigmoid0Precision = 14
    sig0_Float_tmp = np.array([sigmoid(x) for x in hiddenLayer0_Float]).astype(float) # [0, 1]
    # print(f"sig0Float result {sig0_Float_tmp}")
    
    sig0_Float = np.hstack((1, sig0_Float_tmp))
    sig0_Int8 = np.array([x * 2**sigmoid0Precision for x in sig0_Float]).astype(np.int16)
    # print(f"sig0Int8 result {sig0_Int8}")

    ## Step 5. Represent the Theta_1 Weights In a Fixed Point Representation

    # [3, 5]
    if debug:
        print(f"theta1 min {np.min(theta1)}") # -4.030847
        print(f"theta1 max {np.max(theta1)}") # 3.2115848

    weightPrecision1 = 5
    theta1_Int8 = np.array([x * 2**weightPrecision1 for x in theta1]).astype(np.int32) # << 2


    ## Step 6. We Make the Dot Product Between Image Int8 and Weight Int8
    hiddenLayer1_Int8 = np.dot(sig0_Int8, theta1_Int8.T) # [5,19]
    # print(f"hiddenLayer1Float {hiddenLayer1_Int8/2**19}")

    ## Step 7. Apply the Sigmoid To the Result
    hiddenLayer1Precision = 19
    hiddenLayer1_Float = np.array([x/2**hiddenLayer1Precision for x in hiddenLayer1_Int8]).astype(float) # << 2
    # print(f"hiddenLayer1Float {hiddenLayer1_Float}")
    # [2, 14]
    sigmoid1Precision = 14
    sig1_Float = np.array([sigmoid(x) for x in hiddenLayer1_Float]).astype(float) # [0, 1]
    # print(f"sig1_Float {sig1_Float}")

    sig1_Int8 = np.array([x * 2**sigmoid1Precision for x in sig1_Float]).astype(np.int16)
    # print(f"sig1_Int8 {sig1_Int8}")

    ## Step 8. Compare the result
    if debug:
        print("FPGA Output:", sig1_Int8)

    pred = sig1_Int8.argmax()
    return pred == y[imageIndex]


if __name__ == "__main__":
    # compute(1)
    imageMatched = 0
    nb_images = 5000
    for i in range(0, nb_images):
        if i % 100 == 0:
            print(i)
        imageMatched += compute(i)

    print(f"Success {(imageMatched/float(nb_images)) * 100}%")



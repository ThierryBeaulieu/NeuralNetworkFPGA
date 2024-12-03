import numpy as np

debug = False


## Step 0. Define Sigmoid Function And Load Assets
def sigmoid(x):
    x = x / 2**5
    res = 1 / (1 + np.exp(-x))
    return int(res * 2**7) 


y = np.load("y.npy")
theta0 = np.load("theta_0.npy")
theta1 = np.load("theta_1.npy")


def compute(imageIndex, w_precision, i_precision):
    ## Step 1. Represent the Theta_0 Weights In a Fixed Point Representation
    # [2, 4]
    if debug:
        print(f"theta0 min {np.min(theta0)}")  # -1.463369
        print(f"theta0 max {np.max(theta0)}")  # 1.0089920

    ## WEIGHT PRECISION [2, 4, 6, 8, 10, 12, 14]
    weightPrecision0 = w_precision - 2
    theta0_Int8 = np.array([x * 2**weightPrecision0 for x in theta0]).astype(
        np.int32
    )  # << 2

    np.savetxt("theta0_Int8.csv", theta0_Int8.astype(int), fmt="%i", delimiter=",")
    # print(theta0_Int8.astype(np.uint8)[0])

    ## Step 2. Represent the Images In a Fixed Point Representation
    images = np.load("x.npy")

    # [2, 2]
    if debug:
        print(f"image min {np.min(images)}")  # 0.0
        print(f"image max {np.max(images)}")  # 1.0

    image = images[imageIndex]
    xp = np.hstack((1, image))

    ## IMAGE PRECISION
    imagePrecision = i_precision - 2
    image_Int8 = np.array([x * 2**imagePrecision for x in xp]).astype(np.int8)  # << 2
    np.save("image_Int8.npy", image_Int8)

    ## Step 3. We Make the Dot Product Between Image Int8 and Weight Int8
    # mul output [2, 6] * [2,6] = [4, 12]
    # add output [2, 6] + ... + [2,6] = [13, 12]
    hiddenLayer0_Int8 = np.dot(
        image_Int8.astype(np.int32), theta0_Int8.T.astype(np.int32)
    ).astype(
        np.int32
    )
    # print(hiddenLayer0_Int8.astype(np.uint32))

    ## Step 4. Apply the Sigmoid To the Result Hidden Layer 0
    # [13, 12] >> 7 = [13, 5]
    hiddenLayer0_shifted = (hiddenLayer0_Int8.astype(np.int32) >> 7).astype(np.int32)

    # print(hiddenLayer0_shifted)

    # [1, 7]
    sig0_Int8 = []
    for i in range(0, len(hiddenLayer0_shifted)):
        x = hiddenLayer0_shifted[i]
        if x > 2**7 - 1:
            sig0_Int8.append(2**7 - 1)
        elif x < -2**7:
            sig0_Int8.append(0)
        else:
            resSigmoid = sigmoid(x)
            sig0_Int8.append(resSigmoid)

    sig0_stacked = np.hstack((1 * (2**7 - 1), sig0_Int8))
    print(sig0_stacked)

    ## Step 5. Represent the Theta_1 Weights In a Fixed Point Representation
    # [4, 4]
    if debug:
        print(f"theta1 min {np.min(theta1)}")  # -4.030847
        print(f"theta1 max {np.max(theta1)}")  # 3.2115848

    ## WEIGHT PRECISION [1, 3, 5, 7, 9, 11, 13]
    weightPrecision1 = w_precision - 4
    theta1_Int8 = np.array([x * 2**weightPrecision1 for x in theta1]).astype(
        np.int32
    )  # << 2
    np.savetxt("theta1_Int8.csv", theta1_Int8.astype(int), fmt="%i", delimiter=",")

    ## Step 6. We Make the Dot Product Between Image Int8 and Weight Int8
    # mul [1, 7] * [4, 4] = [5, 11]
    # add [5,11] + ... + [5,11] = [10, 11]
    hiddenLayer1_Int8 = np.dot(
        sig0_stacked.astype(np.int32), theta1_Int8.T.astype(np.int32)
    ).astype(
        np.int32
    )  # [11,10]
    # print(f"hiddenLayer1Float {hiddenLayer1_Int8/2**27}")

    ## Step 7. Apply the Sigmoid To the Result
    ## WEIGHT PRECISION
    hiddenLayer1_Tronc = (hiddenLayer1_Int8.astype(np.int32) >> 6).astype(np.int32)

    # Decalage de 5 (10-5) bits vers la droite: [11, 5]
    # hiddenLayer1_Float = np.array([x / 2**5 for x in hiddenLayer1_Tronc]).astype(float)

    # hiddenLayer1Precision = sigmoid0Precision + weightPrecision1
    # hiddenLayer1_Float = np.array(
    #     [x / 2**hiddenLayer1Precision for x in hiddenLayer1_Int8]
    # ).astype(
    #     float
    # )  # << 2
    # print(f"hiddenLayer1Float {hiddenLayer1_Float}")
    # [2, 6]
    sigmoid1Precision = 6
    sig1_Float_tmp = []
    for x in hiddenLayer0_shifted:
        if x > 2**7 - 1:  # max representé sur [3,5] is 011,11111 = 3.96875
            sig1_Float_tmp.append(2**7 - 1)
        elif x < -2**7:
            sig1_Float_tmp.append(0)
        else:
            resSigmoid = sigmoid(x)
            sig1_Float_tmp.append(resSigmoid)
    # sig1_Float = np.array([sigmoid(x) for x in hiddenLayer1_Float]).astype(
    #     float
    # )  # [0, 1]
    # print(f"sig1_Float {sig1_Float}")

    # print(f"sig1_Int8 {sig1_Int8}")

    ## Step 8. Compare the result
    if debug:
        print("FPGA Output:", sig1_Float_tmp)

    pred = np.array(sig1_Float_tmp).argmax()
    return pred == y[imageIndex]


if __name__ == "__main__":

    weights_precision = [4, 6, 8, 10, 12, 14, 16]
    images_precision = [4, 8]
    weights_precision = [8]
    images_precision = [8]
    compute(1, 8, 8)

    # for image_precision in images_precision:
        # for weight_precision in weights_precision:
            # imageMatched = 0
            # nb_images = 5000
            # for i in range(0, nb_images):
                # imageMatched += compute(i, weight_precision, image_precision)

            # result = (imageMatched / float(nb_images)) * 100
            # print(
                # f"Weight {weight_precision} Image {image_precision} Precision {result}%"
            # )

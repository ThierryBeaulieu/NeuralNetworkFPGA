import numpy as np

debug = False


## Step 0. Define Sigmoid Function And Load Assets
def sigmoid(x):
    return 1 / (1 + np.exp(-x))


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
    )  # [4,6]
    # print(hiddenLayer0_Int8)

    ## Step 4. Apply the Sigmoid To the Result Hidden Layer 0
    # sigmoid input [3, 5] = 8
    offset = 9
    hiddenLayer0Precision = imagePrecision + weightPrecision0 - offset
    hiddenLayer0_Tronc = (np.copy(hiddenLayer0_Int8) // (2**7)).astype(
        np.int32
    )  # Decalage de 7 (12-5) bits vers la droite: [13, 5]
    hiddenLayer0_Float = np.array([x / 2**5 for x in hiddenLayer0_Tronc]).astype(float)

    # [2, 6]
    sigmoid0Precision = 6
    sig0_Float_tmp = []
    for x in hiddenLayer0_Float:
        if x > 3.96875:  # max representé sur [3,5] is 011,11111 = 3.96875
            sig0_Float_tmp.append(1)
        elif x < -3.96875:
            sig0_Float_tmp.append(0)
        else:
            sig0_Float_tmp.append(sigmoid(x))
    sig0_Float_tmp = np.array(sig0_Float_tmp).astype(float)  # [0, 1]
    # print(f"sig0Float result {sig0_Float_tmp}")

    sig0_Float = np.hstack((1, sig0_Float_tmp))
    sig0_Int8 = np.array([x * 2**sigmoid0Precision for x in sig0_Float]).astype(np.int8)
    np.savetxt("testing.csv", sig0_Int8.astype(int), fmt="%i", delimiter=",")
    # print(f"sig0Int8 result {sig0_Int8}")

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
    # mul [2, 6] * [4, 4] = [6, 10]
    # add [6,10] + ... + [6,10] = [11, 10]
    hiddenLayer1_Int8 = np.dot(
        sig0_Int8.astype(np.int32), theta1_Int8.T.astype(np.int32)
    ).astype(
        np.int32
    )  # [11,10]
    # print(f"hiddenLayer1Float {hiddenLayer1_Int8/2**27}")

    ## Step 7. Apply the Sigmoid To the Result
    ## WEIGHT PRECISION
    hiddenLayer1_Tronc = (np.copy(hiddenLayer1_Int8) // (2**5)).astype(
        np.int32
    )  # Decalage de 5 (10-5) bits vers la droite: [11, 5]
    hiddenLayer1_Float = np.array([x / 2**5 for x in hiddenLayer1_Tronc]).astype(float)

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
    for x in hiddenLayer1_Float:
        if x > 3.96875:  # max representé sur [3,5] is 011,11111 = 3.96875
            sig1_Float_tmp.append(1)
        elif x < -3.96875:
            sig1_Float_tmp.append(0)
        else:
            sig1_Float_tmp.append(sigmoid(x))
    sig1_Float = np.array(sig1_Float_tmp).astype(float)  # [0, 1]
    # sig1_Float = np.array([sigmoid(x) for x in hiddenLayer1_Float]).astype(
    #     float
    # )  # [0, 1]
    # print(f"sig1_Float {sig1_Float}")

    sig1_Int8 = np.array([x * 2**sigmoid1Precision for x in sig1_Float]).astype(
        np.int16
    )
    # print(f"sig1_Int8 {sig1_Int8}")

    ## Step 8. Compare the result
    if debug:
        print("FPGA Output:", sig1_Int8)

    pred = sig1_Int8.argmax()
    return pred == y[imageIndex]


if __name__ == "__main__":

    weights_precision = [4, 6, 8, 10, 12, 14, 16]
    images_precision = [4, 8]
    weights_precision = [8]
    images_precision = [8]
    compute(1, 8, 8)

    for image_precision in images_precision:
        for weight_precision in weights_precision:
            imageMatched = 0
            nb_images = 5000
            for i in range(0, nb_images):
                imageMatched += compute(i, weight_precision, image_precision)

            result = (imageMatched / float(nb_images)) * 100
            print(
                f"Weight {weight_precision} Image {image_precision} Precision {result}%"
            )

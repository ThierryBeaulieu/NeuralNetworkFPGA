import numpy as np

sigmoidValues = []

def sigmoid(x):
    res = 1 / (1 + np.exp(-x))
    resultInt8 = int(res * 2**7)  # Ensure this is an integer
    return resultInt8

def initSigmoid():
    for i in range(0, 256):
        j = i - 128
        sigmoidValues.append(sigmoid(j / 32))

initSigmoid()

# Convert to hexadecimal and write to a text file
with open('sigmoid_chisel.txt', 'w') as f:
    for value in sigmoidValues:
        f.write(f"{value & 0xFF:02X}\n")  # Format as 2-digit hexadecimal

# def scalaSigmoid(x: Double): UInt = {
# val result = (pow(E, x) / (1 + pow(E, x)))
# val resultSInt = (result * pow(2, 7)).toInt.asUInt(8.W)
# resultSInt
# }
# 
# def initSigmoid(sigMemory: SyncReadMem[UInt]) = {
# // [4:4] [-4.0, 3.9375] = 8 / (2*8)
# for (i <- -128 until 128) {
#     sigMemory.write(
#     (i.S).asUInt,
#     scalaSigmoid(i / 32.0)
#     )
# }
# }
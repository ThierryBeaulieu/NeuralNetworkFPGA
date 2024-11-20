import numpy as np

# Step 1: create a function that accepts 8 bits
# and return sigmoid values
def sigmoid(x):
    return 1 / (1 + np.exp(-x))

# Step 2: Create a function that converts
# floating point values into fixed-point with 
# two's complement

class FixedPoint():
    def __init__(self, value: str, coma: int):
        self.value = value
        self.coma = coma
    
    def getTwosComplement(self):

        return - int(self.value)
    
class FixedPointOp():
    def Add(value1: FixedPoint, value2: FixedPoint):

        # "01.01"
        # "100.1"
        tmp1 = value1.value
        tmp2 = value2.value
        comaDiff = abs(value1.coma - value2.coma)
        while comaDiff > 0:
            if value1.coma > value2.coma:
                tmp2 += "0"
            else:
                tmp1 += "0"
            comaDiff -= 1

        result = ""
        reserve = 0
        for i in range(len(tmp1) - 1, -1, -1):
            if tmp1[i] == '0' and tmp2[i] == '0':
                if reserve:
                    reserve = 0
                    result = "1" + result
                else:
                    result = "0" + result
                    reserve = 0
            elif tmp1[i] == '1' and tmp2[i] == '1':
                if reserve:
                    result = '1' + result
                else:
                    result = '0' + result
                reserve = 1
            else:
                if reserve:
                    reserve = 1
                    result = "0" + result
                else:
                    result = "1" + result

        if reserve == 0:
            result = "0" + result
        else:
            result = "1" + result

        return FixedPoint(value=result, coma=max(value1.coma, value2.coma))
        
    def Multiply(value1: FixedPoint, value2: FixedPoint):
        pass
        

# Test Addition of Two Fixed Point Numbers that uses Two's Complement
fixedPoint1 =   ["11010",   "10110", "0000", "1111"]
fixedPoint2 =   ["00100",   "10110", "0000", "1111"]
expectedResult = ["011110", "101100", "00000", "11110"]
for i in range(0, len(fixedPoint1)):
    fp1 = FixedPoint(value=fixedPoint1[i], coma=3)
    fp2 = FixedPoint(value=fixedPoint2[i], coma=3)
    result = FixedPointOp.Add(fp1, fp2)
    assert(result.value == expectedResult[i])
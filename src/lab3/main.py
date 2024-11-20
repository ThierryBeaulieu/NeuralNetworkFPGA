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
            elif tmp1[i] == '1' and tmp2[i] == '1':
                if reserve == '0':
                    result = '0' + result
                else:
                    result = '1' + result
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
        


# Example of multiplication : 0.5 (00.100) * 1.25 (01.010) = 0.625 (00.101)
fixedPoint1 = FixedPoint(value="11010", coma=3)
fixedPoint2 = FixedPoint(value="00100", coma=3)
result = FixedPointOp.Add(fixedPoint1, fixedPoint2)

assert(result.value == "011110")
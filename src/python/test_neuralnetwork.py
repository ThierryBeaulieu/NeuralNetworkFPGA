import unittest
from neuralnetwork import Neuron
import numpy as np
# python3 -m unittest test_neuralnetwork.py

class TestNeuron(unittest.TestCase):

    def test_neuron_weight_length(self):
        neuron = Neuron(0)
        self.assertEqual(len(neuron.weights), 401)

    def test_neuron_weight_content(self):
        neuron = Neuron(0)
        expected = [-50,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,-1,0,0,0,0,-2,-3,-3,0,0,0,0,0,0,0,-2,-8,-13,-18,-10,-11,0,4,-1,0,-7,-10,-15,-19,-18,0,0,0,0,0,-1,-5,-41,-32,0,19,29,0,-7,0,-16,-15,26,3,-17,-3,0,0,0,-1,22,2,-12,-21,-11,33,21,-28,-17,-3,-24,-23,54,13,-45,-10,0,0,0,-11,-4,-8,-21,-10,-14,-8,-16,-14,-10,-3,-14,-26,-27,-1,-26,-16,0,0,0,-22,-38,-13,-16,0,-11,10,-34,-34,-18,-32,19,32,-12,-1,0,-35,0,0,-3,-17,-9,-27,-20,3,2,-6,-33,-8,7,-13,-27,7,-2,-14,-22,-42,-6,0,-6,-3,-10,-13,-6,-4,2,7,8,16,10,13,10,1,-21,-3,-4,-30,-1,0,-3,9,-20,-34,4,-5,9,45,44,30,34,0,19,0,1,-3,-23,-23,0,0,-2,7,30,9,-7,-13,27,20,6,-13,2,-27,-17,9,13,12,-39,-26,0,0,-2,-32,-38,-20,-29,-7,0,1,2,12,15,-14,-12,-16,-16,31,9,-12,-1,0,-3,-13,20,10,27,-12,-18,16,-20,-19,-36,-19,18,23,-11,17,5,-10,0,0,0,-7,-8,-2,-7,-16,-12,-34,-30,-16,-28,-19,26,-12,-4,30,-15,-9,0,0,0,-2,-16,-15,13,-6,-21,-17,-13,-18,-23,-22,-19,-9,7,2,-18,0,0,0,0,1,9,-13,-14,-17,-14,-6,-4,-2,-5,-12,3,10,3,-7,-5,0,0,0,0,0,5,-13,-19,-10,0,0,-4,0,-1,-5,6,33,37,8,-3,0,0,0,0,0,-1,-5,-2,-2,8,6,-1,3,0,-1,0,7,23,6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,-2,0,0,0,0]
        self.assertEqual(neuron.weights.tolist(), expected)

    def test_neuron_weight_content_other_index(self):
        neuron = Neuron(1)
        expected = [-52,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,-1,-2,0,0,0,-1,-1,0,0,0,0,0,0,0,0,0,0,0,-5,-4,-6,-7,-4,1,7,10,0,-16,-6,2,-5,-1,0,0,0,0,0,1,1,4,8,9,1,-22,-4,17,3,-1,8,-3,-4,-11,-5,0,0,0,-1,7,-3,-9,1,-5,-31,-38,-4,9,16,5,0,4,10,-14,-16,0,0,-2,0,13,5,7,7,-3,-44,-33,22,14,-2,21,0,3,21,-1,-20,0,-1,-6,5,15,0,-3,12,5,-40,-25,8,6,-9,12,10,-1,-17,-12,-18,-1,0,-5,15,0,-6,6,6,-2,-30,-10,-15,11,0,1,7,15,-13,-15,-17,-2,0,4,15,-6,7,8,-8,6,-19,-10,6,16,11,14,10,11,-2,-13,-13,-1,0,5,7,12,1,-12,7,-10,-29,-9,-2,3,2,14,3,3,-1,-6,-12,-1,1,-18,-7,14,5,-5,7,-8,-10,-24,-6,8,3,7,4,-17,-3,-3,-15,0,4,-13,-14,2,6,-2,-13,0,5,-6,-8,4,6,0,6,-12,-8,-13,-18,-1,1,-10,-20,8,6,-18,11,-1,-16,2,4,-15,8,8,-6,6,12,3,-8,-1,0,-14,-21,-9,10,-9,16,-6,-20,-7,-11,5,0,-4,1,7,-7,13,-1,-1,0,-7,-1,-12,-1,-4,1,14,6,2,-5,9,-9,-7,14,10,-14,12,-5,0,0,-1,-4,-17,-7,11,-5,2,-3,-7,-14,-2,11,5,3,14,5,4,-4,0,0,0,-2,-15,-16,-8,-7,-6,-20,-5,-20,-20,21,4,0,15,11,1,-2,0,0,0,0,0,-8,-22,-21,5,10,8,-12,0,16,4,4,5,1,-1,0,0,0,0,0,1,0,-7,-8,0,11,8,4,27,19,9,3,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,2,6,4,0,0,0,0,0,0,0]
        self.assertEqual(neuron.weights.tolist(), expected)


    def test_integration_neuron_m_16_w_min(self):
        inputPixels = [255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255]
        weights = [-128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128]
        m = 16
        n = 4

        fpga_out = np.dot(np.array(inputPixels).astype(np.int32), np.array(weights).astype(np.int32) + 128).astype(np.int64) / ((2**8) * (2**8 - 1))
        fpga_out =  (2 * fpga_out) - m # le range se situe entre [-16, 16]

        # print(f"fpga_out = {fpga_out}")
        # print(f"tanh(s) {(np.tanh(fpga_out) + 1) / 2}") # output [-1.0, 1.0]

        result = 0
        neuron = Neuron(7, weights=weights, n=n, m=m)
        nbCycles = 1024
        for _ in range(0, nbCycles):
            res = neuron.tick(inputPixels)
            result += res

        Ex = result / nbCycles
        self.assertAlmostEqual(Ex, 0.000, places=2)


    def test_integration_neuron_m_16_w_max(self):
        inputPixels = [255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255]
        weights = [127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127]
        m = 16
        n = 4

        # weights = np.loadtxt("resources/weights.csv", delimiter=",").astype(np.int8)
        # min = 0.0, max = 2**8 * 2**8 = 2**16
      
        fpga_out = np.dot(np.array(inputPixels).astype(np.int32), np.array(weights).astype(np.int32) + 128).astype(np.int64) / ((2**8) * (2**8 - 1))
        fpga_out =  (2 * fpga_out) - m # le range se situe entre [-16, 16]
        
        # print(f"fpga_out = {fpga_out}")
        # print(f"tanh(s) {(np.tanh(fpga_out) + 1) / 2}") # output [-1.0, 1.0]

        result = 0
        neuron = Neuron(7, weights=weights, n=n, m=m)
        nbCycles = 1024
        for _ in range(0, nbCycles):
            res = neuron.tick(inputPixels)
            result += res

        Ex = result / nbCycles
        self.assertAlmostEqual(Ex, 0.999, places=2)

    def test_integration_neuron_m_16_average(self):
        inputPixels = [255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255]
        weights = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        m = 16
        n = 4

        fpga_out = np.dot(np.array(inputPixels).astype(np.int32), np.array(weights).astype(np.int32) + 128).astype(np.int64) / ((2**8) * (2**8 - 1))
        fpga_out =  (2 * fpga_out) - m # le range se situe entre [-16, 16]

        # print(f"fpga_out = {fpga_out}")
        # print(f"tanh(s) {(np.tanh(fpga_out) + 1) / 2}") # output [-1.0, 1.0]

        result = 0
        neuron = Neuron(7, weights=weights, n=n, m=m)
        nbCycles = 1024
        for _ in range(0, nbCycles):
            res = neuron.tick(inputPixels)
            result += res

        Ex = result / nbCycles
        self.assertAlmostEqual(Ex, 0.50, delta=1.0)

    def test_integration_neuron_m_16_real(self):
        inputPixels = [134, 128, 116, 128, 112, 115, 140, 128, 134, 128, 116, 128, 112, 115, 140, 128]
        weights = [1, 0, 3, -2, -3, 2, 0, 3, 1, 0, 3, -2, -3, 2, 0, 3]
        m = 16
        n = 4

        fpga_out = np.dot(np.array(inputPixels).astype(np.int32), np.array(weights).astype(np.int32) + 128).astype(np.int64) / ((2**8) * (2**8 - 1))
        fpga_out =  (2 * fpga_out) - m # le range se situe entre [-16, 16]

        # print(f"fpga_out = {fpga_out}")
        # print(f"tanh(s) {(np.tanh(fpga_out) + 1) / 2}") # output [-1.0, 1.0]

        result = 0
        neuron = Neuron(7, weights=weights, n=n, m=m)
        nbCycles = 1024
        for _ in range(0, nbCycles):
            res = neuron.tick(inputPixels)
            result += res

        Ex = result / nbCycles
        # print(Ex)
        self.assertAlmostEqual(Ex, 0.55, delta=0.5)

    def test_integration_neuron_m_8_w_min(self):
        inputPixels = [255, 255, 255, 255, 255, 255, 255, 255]
        weights = [-128, -128, -128, -128, -128, -128, -128, -128]
        m = 8
        n = 4

        fpga_out = np.dot(np.array(inputPixels).astype(np.int32), np.array(weights).astype(np.int32) + 128).astype(np.int64) / ((2**8) * (2**8 - 1))
        fpga_out =  (2 * fpga_out) - m # le range se situe entre [-16, 16]

        result = 0
        neuron = Neuron(7, weights=weights, n=n, m=m)
        nbCycles = 1024
        for _ in range(0, nbCycles):
            res = neuron.tick(inputPixels)
            result += res

        Ex = result / nbCycles
        self.assertAlmostEqual(Ex, 0.000, places=2)


    def test_integration_neuron_m_8_w_max(self):
        inputPixels = [255, 255, 255, 255, 255, 255, 255, 255]
        weights = [127, 127, 127, 127, 127, 127, 127, 127]
        m = 8
        n = 4

        fpga_out = np.dot(np.array(inputPixels).astype(np.int32), np.array(weights).astype(np.int32) + 128).astype(np.int64) / ((2**8) * (2**8 - 1))
        fpga_out =  (2 * fpga_out) - m # le range se situe entre [-16, 16]

        result = 0
        neuron = Neuron(7, weights=weights, n=n, m=m)
        nbCycles = 1024
        for _ in range(0, nbCycles):
            res = neuron.tick(inputPixels)
            result += res

        Ex = result / nbCycles
        self.assertAlmostEqual(Ex, 0.999, places=2)

    def test_integration_neuron_m_8_average(self):
        inputPixels = [255, 255, 255, 255, 255, 255, 255, 255]
        weights = [0, 0, 0, 0, 0, 0, 0, 0]
        m = 8
        n = 4

        fpga_out = np.dot(np.array(inputPixels).astype(np.int32), np.array(weights).astype(np.int32) + 128).astype(np.int64) / ((2**8) * (2**8 - 1))
        fpga_out =  (2 * fpga_out) - m # le range se situe entre [-16, 16]

        result = 0
        neuron = Neuron(7, weights=weights, n=n, m=m)
        nbCycles = 1024
        for _ in range(0, nbCycles):
            res = neuron.tick(inputPixels)
            result += res

        Ex = result / nbCycles
        self.assertAlmostEqual(Ex, 0.50, delta=1.0)

    def test_integration_neuron_m_8_average(self):
        inputPixels = [128, 128, 128, 128, 128, 128, 128, 128]
        weights = [0, 0, 0, 0, 0, 0, 0, 0]
        m = 8
        n = 4

        fpga_out = np.dot(np.array(inputPixels).astype(np.int32), np.array(weights).astype(np.int32) + 128).astype(np.int64) / ((2**8) * (2**8 - 1))
        fpga_out =  (2 * fpga_out) - m # le range se situe entre [-16, 16]

        result = 0
        neuron = Neuron(7, weights=weights
                        , n=n, m=m)
        nbCycles = 1024
        for _ in range(0, nbCycles):
            res = neuron.tick(inputPixels)
            result += res

        Ex = result / nbCycles
        self.assertAlmostEqual(Ex, 0.25, delta=1.0)


if __name__ == "__main__":
    unittest.main()
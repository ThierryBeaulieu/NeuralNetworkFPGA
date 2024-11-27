import unittest
from neuralnetwork import Neuron
# python3 -m unittest test_neuralnetwork.py

class TestNeuron(unittest.TestCase):
    def setUp(self):
        """Set up resources before each test"""
        self.neuron = Neuron(0)

    def test_add(self):
        """Test the add method"""
        self.assertEqual(len(self.neuron.weights), 401)

if __name__ == "__main__":
    unittest.main()
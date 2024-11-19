package lab3

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import lab3.NeuralNetwork

// testOnly lab3.NeuralNetworkSpec

class NeuralNetworkSpec extends AnyFreeSpec with Matchers {

  "NeuralNetwork should initialize an array of weights" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.weights.length.mustBe(10)
      dut.weights(0).length.mustBe(401)
    }
  }

  "NeuralNetwork should make proper calculations" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      val imageTest = Seq(64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -2, -2, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 12, 20, 20, 9, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 18, 55, 63, 63, 51, 10, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1,
        15, 50, 64, 67, 56, 59, 68, 46, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 14,
        54, 64, 28, 27, 11, 14, 48, 62, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 54,
        66, 37, -2, -3, -1, -4, 31, 65, 58, 6, -1, 0, 0, 0, 0, 0, -1, 7, 51, 65,
        48, 6, -1, 0, 0, -1, 17, 62, 63, 8, -1, 0, 0, 0, 0, 0, -1, 26, 64, 51,
        6, 0, 0, 0, 0, -1, 28, 65, 45, 2, 0, 0, 0, 0, 0, -1, 5, 56, 65, 56, 32,
        14, 0, 0, 0, -2, 35, 65, 29, 0, 0, 0, 0, 0, 0, 3, 32, 63, 64, 65, 56,
        11, -1, 0, -1, 9, 51, 47, 6, -1, 0, 0, 0, 0, 1, 33, 60, 60, 58, 52, 16,
        0, 0, -1, -1, 36, 61, 17, -1, 0, 0, 0, 0, -1, 9, 67, 64, 44, 20, 18, 3,
        -2, -2, 3, 32, 63, 31, -1, 0, 0, 0, 0, 0, 0, 2, 40, 63, 60, 54, 55, 48,
        36, 36, 48, 65, 43, 5, 0, 0, 0, 0, 0, 0, 0, -1, 7, 41, 49, 49, 49, 49,
        49, 49, 50, 39, 4, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

      // Provide the input data from the imageTest array

      dut.slaveIO.tready.expect(true.B)
      for (i <- 0 until imageTest.length) {
        dut.slaveIO.tvalid.poke(true.B)
        dut.slaveIO.tdata.poke(imageTest(i))
        dut.slaveIO.tlast.poke(
          if (i == imageTest.length - 1) true.B else false.B
        )
        if (i != imageTest.length - 1) {
          dut.clock.step(1)
        }
      }

      val expectedValues = Seq(-27052, -10381, -9413, -18813, -4725, -18994,
        -12393, -13929, -9571, 8892)

      dut.masterIO.tready.poke(true.B)
      dut.clock.step(1)
      for (i <- 0 until 10) {
        dut.masterIO.tvalid.expect(true.B)
        // dut.masterIO.tlast.expect((i == expectedValues.length - 1).B)
        dut.masterIO.tdata.expect(expectedValues(i))
        dut.clock.step(1)
      }
    }
  }
}

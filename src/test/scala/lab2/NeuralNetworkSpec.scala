package lab2

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import lab2.NeuralNetwork

class NeuralNetworkSpec extends AnyFreeSpec with Matchers {

  "NeuralNetwork should initialize an array of weights" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.weights.length.mustBe(10)
      dut.weights(0).length.mustBe(401)
      // TODO: Find a way to test the registers
    }
  }

  "NeuralNetwork should make proper calculations" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      val expectedResult = 10

      val imageTest = Seq(64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, -1, 5, 17, 17, 17, 17, 14, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5,
        24, 54, 64, 62, 59, 64, 61, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 6, 41,
        66, 54, 34, 21, 17, 42, 64, 57, 6, -1, 0, 0, 0, 0, 0, 0, 0, 11, 50, 61,
        29, 4, 0, -2, -3, 9, 57, 67, 9, -1, 0, 0, 0, 0, 0, -2, 17, 59, 66, 38,
        0, -1, 0, 0, 0, 9, 58, 58, 6, -1, 0, 0, 0, 0, -1, 8, 50, 65, 48, 18, 0,
        0, 0, 0, -2, 28, 66, 34, 0, 0, 0, 0, 0, 0, 10, 49, 66, 51, 10, 0, 0, 0,
        0, 0, 0, 46, 54, 9, -1, 0, 0, 0, 0, 2, 44, 65, 65, 56, 25, -1, 0, 0, 0,
        -1, 23, 59, 29, -2, 0, 0, 0, 0, -1, 8, 62, 60, 49, 55, 13, -1, 0, 0, -1,
        7, 53, 46, 3, 0, 0, 0, 0, 0, 0, 2, 44, 64, 38, 20, -2, -3, -2, -3, 6,
        46, 44, 9, 0, 0, 0, 0, 0, 0, 0, 0, 7, 27, 63, 56, 47, 46, 46, 46, 54,
        53, 4, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 24, 32, 49, 63, 64, 64, 47,
        13, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -2, 4, 18, 20, 19, 5, -1,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2, -2, -2, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

      dut.slaveIO.tvalid.poke(false.B)

      // Provide the input data from the imageTest array
      /*
      for (i <- 0 until imageTest.length) {
        dut.sAxis.data.tvalid.poke(true.B)
        dut.sAxis.data.tdata.
        dut.sAxis.
          .poke(if (i == imageTest.length - 1) true.B else false.B)
        dut.clock.step(1)
      }

      for (i <- 0 until 10) {
        dut.mAxis.data.tvalid.poke(true.B)
        dut.mAxis.data.tdata.expect(10.S)
        dut.clock.step(1)
      }
       */
    }
  }
}

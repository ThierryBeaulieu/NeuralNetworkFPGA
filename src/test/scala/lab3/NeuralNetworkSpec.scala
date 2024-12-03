package lab3

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import lab3.NeuralNetwork

// testOnly lab3.NeuralNetworkSpec

class NeuralNetworkSpec extends AnyFreeSpec with Matchers {

  "NeuralNetwork should have accessible weights for it's First Hidden Layer" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.clock.step(1)
      dut.theta0.length.mustBe(25)
      dut.theta0(0).length.mustBe(401)

      // todo create some data to test it
      val imageTest = Seq(64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 12, 20, 20, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 18, 55, 63, 63, 51, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15,
        50, 64, 64, 56, 59, 64, 46, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 54, 64,
        28, 27, 11, 14, 48, 62, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 54, 64, 37,
        0, 0, 0, 0, 31, 64, 58, 6, 0, 0, 0, 0, 0, 0, 0, 7, 51, 64, 48, 6, 0, 0,
        0, 0, 17, 62, 63, 8, 0, 0, 0, 0, 0, 0, 0, 26, 64, 51, 6, 0, 0, 0, 0, 0,
        28, 64, 45, 2, 0, 0, 0, 0, 0, 0, 5, 56, 64, 56, 32, 14, 0, 0, 0, 0, 35,
        64, 29, 0, 0, 0, 0, 0, 0, 3, 32, 63, 64, 64, 56, 11, 0, 0, 0, 9, 51, 47,
        6, 0, 0, 0, 0, 0, 1, 33, 60, 60, 58, 52, 16, 0, 0, 0, 0, 36, 61, 17, 0,
        0, 0, 0, 0, 0, 9, 64, 64, 44, 20, 18, 3, 0, 0, 3, 32, 63, 31, 0, 0, 0,
        0, 0, 0, 0, 2, 40, 63, 60, 54, 55, 48, 36, 36, 48, 64, 43, 5, 0, 0, 0,
        0, 0, 0, 0, 0, 7, 41, 49, 49, 49, 49, 49, 49, 50, 39, 4, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0)

      dut.io.slaveIO.tready.expect(true.B)
      for (i <- 0 until 401) {
        dut.io.slaveIO.tvalid.poke(true.B)
        dut.io.slaveIO.tdata.poke(imageTest(i))
        if (i == 400) {
          dut.io.slaveIO.tlast.poke(true.B)
        }
        dut.clock.step(1)
      }
      // processing
      dut.clock.step(30)

      val expectedValues = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 127)

      dut.io.masterIO.tready.poke(true.B)
      for (i <- 0 until 10) {
        dut.io.masterIO.tvalid.expect(true.B)
        dut.io.masterIO.tdata.expect(expectedValues(i))
        dut.clock.step(1)
      }
    }
  }
}

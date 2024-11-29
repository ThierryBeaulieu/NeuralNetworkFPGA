package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NStanhSpec

class NStanhSpec extends AnyFreeSpec with Matchers {

  "Should produce return an approximation of tanh for a stream [-128, -128]" in {
    simulate(new NStanh(n = 4, m = 128, nbData = 8)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // Equivalent of Weight [-128, -128] and Pixel [255, 255]
      val inputBipolarStream = Seq(
        1024.S(12.W),
        1024.S(12.W),
        1024.S(12.W),
        1024.S(12.W),
        1024.S(12.W),
        1024.S(12.W),
        1024.S(12.W),
        1024.S(12.W)
      )

      val expectedUnipolarStream =
        Seq(
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W)
        )

      print("output stream")
      for (i <- 0 until expectedUnipolarStream.length) {
        dut.io.inputSi.poke(inputBipolarStream(i))
        dut.clock.step(1)
        print(dut.io.outputStream.peek().litValue)
        // dut.io.outputStream.expect(expectedUnipolarStream(i))
      }
    }
  }

  "Should produce return an approximation of tanh with m =128" in {
    simulate(new NStanh(n = 8, m = 128, nbData = 8)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // Equivalent of Weight [-128, -128] and Pixel [255, 255]
      val inputBipolarStream = Seq(
        506.S(12.W),
        382.S(12.W),
        510.S(12.W),
        256.S(12.W),
        250.S(12.W),
        382.S(12.W),
        384.S(12.W),
        762.S(12.W)
      )

      val expectedUnipolarStream =
        Seq(
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W)
        )

      print("output stream")
      for (i <- 0 until expectedUnipolarStream.length) {
        dut.io.inputSi.poke(inputBipolarStream(i))
        dut.clock.step(1)
        print(dut.io.outputStream.peek().litValue)
        // dut.io.outputStream.expect(expectedUnipolarStream(i))
      }
    }
  }

}

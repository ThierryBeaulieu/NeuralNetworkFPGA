package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.B2SUnipolarSpec

class B2SUnipolarSpec extends AnyFreeSpec with Matchers {

  "Should produce a unipolar stream of 1 when using a pixel of 255" in {
    simulate(new B2SUnipolar) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val expectedUnipolarStream =
        Seq(
          1.U(1.W),
          1.U(1.W),
          1.U(1.W),
          1.U(1.W),
          1.U(1.W),
          1.U(1.W),
          1.U(1.W),
          1.U(1.W),
          1.U(1.W),
          1.U(1.W)
        )
      // send pixel value
      dut.io.inputPixel.poke(255.U)
      dut.clock.step(1)

      for (i <- 0 until expectedUnipolarStream.length) {
        dut.io.outputStream.expect(expectedUnipolarStream(i))
        dut.clock.step(1)
      }
    }
  }

  "Should produce a unipolar stream of 0 when using a pixel of 0" in {
    simulate(new B2SUnipolar) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val expectedUnipolarStream =
        Seq(
          0.U(1.W),
          0.U(1.W),
          0.U(1.W),
          0.U(1.W),
          0.U(1.W),
          0.U(1.W),
          0.U(1.W),
          0.U(1.W),
          0.U(1.W),
          0.U(1.W)
        )
      // send pixel value
      dut.io.inputPixel.poke(0.U)
      dut.clock.step(1)

      for (i <- 0 until expectedUnipolarStream.length) {
        dut.io.outputStream.expect(expectedUnipolarStream(i))
        dut.clock.step(1)
      }
    }
  }

  "Should produce a unipolar stream of 0 and 1 when using a pixel of 128" in {
    simulate(new B2SUnipolar) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val expectedUnipolarStream = Seq(
        0.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        0.U(1.W),
        1.U(1.W),
        0.U(1.W),
        1.U(1.W),
        1.U(1.W),
        0.U(1.W)
      )
      // send pixel value
      dut.io.inputPixel.poke(128.U)
      dut.clock.step(1)

      for (i <- 0 until expectedUnipolarStream.length) {
        dut.io.outputStream.expect(expectedUnipolarStream(i))
        // print(dut.io.outputStream.peek().litValue)
        dut.clock.step(1)
      }
    }
  }
}

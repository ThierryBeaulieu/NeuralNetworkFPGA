package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.B2ISBipolarSpec

class B2ISBipolarSpec extends AnyFreeSpec with Matchers {

  "Should produce a bipolar stream of 1 when using a weight of 127" in {
    simulate(new B2ISBipolar) { dut =>
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

      dut.io.inputWeight.poke(127.U)
      dut.clock.step(1)

      for (i <- 0 until expectedUnipolarStream.length) {
        dut.io.outputStream.expect(expectedUnipolarStream(i))
        dut.clock.step(1)
      }
    }
  }
}

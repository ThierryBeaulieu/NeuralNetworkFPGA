package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NStanhSpec

class NStanhSpec extends AnyFreeSpec with Matchers {

  "Should produce return an approximation of tanh" in {
    simulate(new NStanh(2.U)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val expectedUnipolarStream =
        Seq(
          1.U(2.W),
          0.U(2.W),
          1.U(2.W)
        )

      dut.io.inputInteger.poke(inputIntegers(i))
      dut.io.inputBit.poke(inputBit(i))
      dut.clock.step(1)

      for (i <- 0 until expectedUnipolarStream.length) {
        dut.io.outputStream.expect(expectedUnipolarStream(i))
        dut.clock.step(1)
      }
    }
  }
}

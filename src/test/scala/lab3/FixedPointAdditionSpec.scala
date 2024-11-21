package lab3

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import lab3.FixedPointAddition

// testOnly lab3.FixedPointAdditionSpec

class FixedPointAdditionSpec extends AnyFreeSpec with Matchers {

  "Should Add Two Binaries Together" in {
    simulate(new FixedPointAddition) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      dut.io.in1.poke(143.S)
      dut.io.in2.poke(423.S)

      dut.clock.step(1)

      dut.io.out.expect(566.S)
    }
  }

}

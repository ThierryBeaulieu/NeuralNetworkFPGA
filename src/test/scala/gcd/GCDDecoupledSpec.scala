package gcd

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class GCDDecoupledSpec extends AnyFreeSpec with Matchers {

  "GCD decoupled should calculate proper greatest common divisor" in {
    simulate(new DecoupledGcd(16)) { dut =>
      val testCases = Seq(
        (8, 12, 4),
        (18, 24, 6),
        (100, 25, 25),
        (7, 3, 1),
        (56, 98, 14)
      )

      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      // Run through each test case
      for ((x, y, expectedGcd) <- testCases) {
        // Input values
        dut.input.valid.poke(true.B)
        dut.input.bits.value1.poke(x.U)
        dut.input.bits.value2.poke(y.U)

        // Wait until input is accepted
        while (!dut.input.ready.peek().litToBoolean) {
          dut.clock.step(1)
        }

        // Output validation
        dut.output.ready.poke(true.B)
        while (!dut.output.valid.peek().litToBoolean) {
          dut.clock.step(1)
        }
        dut.output.bits.gcd.expect(expectedGcd.U)
      }
    }
  }
}

package gcd

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class GCDSpec extends AnyFreeSpec with Matchers {

  "GCD module should compute the greatest common divisor" in {
    simulate(new GCD) { dut =>
      // Helper function to perform a single GCD computation
      def computeGCD(a: Int, b: Int): Int = {
        if (b == 0) a else computeGCD(b, a % b)
      }

      // Test cases with known GCD results
      val testCases = Seq(
        (27, 15), // GCD = 3
        (21, 14), // GCD = 7
        (32, 12), // GCD = 4
        (9, 28), // GCD = 1
        (100, 25) // GCD = 25
      )

      for ((a, b) <- testCases) {
        val expectedGCD = computeGCD(a, b)

        // Load the values into the DUT
        dut.io.value1.poke(a.U)
        dut.io.value2.poke(b.U)
        dut.io.loadingValues.poke(true.B)
        dut.clock.step(1)
        dut.io.loadingValues.poke(false.B)

        // Step through the calculation until output is valid
        while (!dut.io.outputValid.peek().litToBoolean) {
          dut.clock.step(1)
        }

        // Check that the output matches the expected GCD
        dut.io.outputGCD.expect(expectedGCD.U)
      }
    }
  }
}

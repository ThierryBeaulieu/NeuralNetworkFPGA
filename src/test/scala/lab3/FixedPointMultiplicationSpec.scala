package lab3

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import lab3.FixedPointMultiplication

// testOnly lab3.FixedPointMultiplicationSpec

class FixedPointMultiplicationSpec extends AnyFreeSpec with Matchers {

  "Should Add Two Binaries Together" in {
    simulate(new FixedPointMultiplication) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)
      // val value1 = ("b00001011".U(8.W)).asSInt

      dut.io.in1.poke("b00001011".U(8.W)) // 8 + 2 + 1 = 11
      dut.io.in2.poke("b11011111".U(8.W)) // -64 + 1 + 2 + 4 + 8 + 16 = -33

      dut.clock.step(1)
      print(dut.io.out.peek().litValue)
      dut.io.out.expect(-363) // 1111111010010101
    }
  }

}

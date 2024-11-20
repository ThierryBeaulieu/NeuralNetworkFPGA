package lab3

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import lab3.TwoComplement

// testOnly lab3.TwoComplementSpec

class TwoComplementSpec extends AnyFreeSpec with Matchers {

  "Should produce two's complement" in {
    simulate(new TwoComplement) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      dut.io.in.poke("b00001011".U(8.W)) // 11

      dut.clock.step(1)

      dut.io.out.expect("b11110101".U(8.W)) // -16 + 4 + 1 = -11
    }
  }

}

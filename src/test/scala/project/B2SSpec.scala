package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import project.B2S

class B2SSpec extends AnyFreeSpec with Matchers {

  "Should produce a random bit between 1 and 0" in {
    simulate(new B2S) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      dut.io.inputComparator.poke(512.U)
      dut.clock.step(1)
      dut.io.outputStream.expect(0.U)
      dut.clock.step(1)
      dut.io.outputStream.expect(0.U)
    }
  }
}

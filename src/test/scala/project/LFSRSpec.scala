package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import project.LFSR

class LFSRSpec extends AnyFreeSpec with Matchers {

  "LFSR should not return the same number twice" in {
    simulate(new LFSR) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      // Input parameters
      dut.io.inputSeed.poke("b11001101".U)

      dut.clock.step(1)
      dut.io.outputRandomNumber.expect(0.U)
      
      dut.clock.step(1)
      dut.io.outputRandomNumber.expect(0.U)
    }
  }
}

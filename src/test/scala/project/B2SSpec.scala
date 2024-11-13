package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import project.B2S

class BS2Spec extends AnyFreeSpec with Matchers {

  "Binary 2 Stochastic should produce stochastic string" in {
    simulate(new B2S) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      dut.io.inputComparator.poke(512.U)
      for (i <- 0 until 100) {
        println(dut.io.outputStream.peek())
        dut.clock.step(1)
      }
      dut.io.outputStream.expect(255.U)

    }
  }

}

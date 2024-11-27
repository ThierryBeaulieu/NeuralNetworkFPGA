package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NeuronWrapperSpec

class NeuronWrapperSpec extends AnyFreeSpec with Matchers {

  "NeuronWrapperSpec should have accessible weights" in {
    simulate(new NeuronWrapper) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.weights.length.mustBe(1)
      dut.weights(0).length.mustBe(8)
    }
  }
}

package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NeuronSpec

class NeuronSpec extends AnyFreeSpec with Matchers {
  "Neuron should produce a unipolar stream for a pixel=255" in {
    simulate(new Neuron(2, 128)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      for (i <- 0 until 2) {
        dut.io.inputPixels(i).poke(255.U)
        dut.io.inputWeights(i).poke(127.S)
      }
      dut.clock.step(1)
      dut.clock.step(1)
      dut.clock.step(1)
      print(
        f"Values obtained by tree Adder${dut.io.outputTreeAdder.peek().litValue}"
      )

    }
  }

}

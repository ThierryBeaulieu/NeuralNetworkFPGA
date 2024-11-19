package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NeuronSpec

class NeuronSpec extends AnyFreeSpec with Matchers {

  "Neuron should generate stream with B2S" in {
    simulate(new Neuron(nbData = 1)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // incoming bipolar stochastic stream
      dut.io.inputPixels(0).poke(255.U)
      dut.io.inputWeights(0).poke(127.S)
      dut.clock.step(1)

      for (i <- 0 until 1024) {
        print(dut.io.outputStream(0).peek().litValue)
      }
    }
  }
}

package lab2

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import lab2.NeuralNetwork

class NeuralNetworkSpec extends AnyFreeSpec with Matchers {

  "NeuralNetwork should initialize an array of weights" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.weights.length.mustBe(10)
      dut.weights(0).length.mustBe(401)
    }
  }
}

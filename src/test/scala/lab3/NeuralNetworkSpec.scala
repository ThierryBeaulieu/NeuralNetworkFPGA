package lab3

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import lab3.NeuralNetwork

// testOnly lab3.NeuralNetworkSpec

class NeuralNetworkSpec extends AnyFreeSpec with Matchers {

  "NeuralNetwork should have accessible weights for it's First Hidden Layer" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.clock.step(1)
      dut.theta0.length.mustBe(25)
      dut.theta0(0).length.mustBe(401)

      // todo create some data to test it
    }
  }

}

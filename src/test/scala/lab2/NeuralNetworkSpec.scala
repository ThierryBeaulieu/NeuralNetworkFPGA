package lab2

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import lab2.NeuralNetwork

class NeuralNetworkSpec extends AnyFreeSpec with Matchers {

  "NeuralNetwork should process inputs and produce correct outputs" in {
    simulate(new NeuralNetwork) { dut =>
      def sendInputData(inputData: Seq[Int], valid: Boolean = true) = {
        dut.sAxis.data.tvalid.poke(valid.B)
        dut.sAxis.data.tdata.poke(inputData.head.S(16.W))
        dut.sAxis.data.tlast.poke(inputData.size == 1)
      }

      val testInput = Seq(1, 2, 3, 4, 5)
      val expectedOutput = Seq(10, 20, 30, 40, 50)

      dut.weights(0)(0).poke(1.S(16.W))
      dut.weights(1)(0).poke(2.S(16.W))
      dut.weights(2)(0).poke(3.S(16.W))

      // Reset all signals to start
      dut.sAxis.tready.poke(true.B)
      dut.mAxis.data.tvalid.poke(false.B)

      sendInputData(testInput)

      dut.clock.step(1)

      dut.mAxis.data.tvalid.poke(true.B)
      dut.mAxis.data.tdata.expect(expectedOutput.head.S(16.W))
      dut.mAxis.data.tvalid.poke(true.B)

      for (i <- 0 until testInput.length) {
        dut.mAxis.data.tvalid.poke(true.B)
        dut.mAxis.data.tdata.expect(expectedOutput(i).S(16.W))
        dut.clock.step(1)
      }
    }
  }
}

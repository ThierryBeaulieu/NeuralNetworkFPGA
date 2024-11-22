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

      dut.weights_hidden_layer1.length.mustBe(25)
      dut.weights_hidden_layer1(0).length.mustBe(401)
    }
  }

  "NeuralNetwork should a sigmoid approximation" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      val imageTest = Seq(127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 10, 34, 35, 36, 35, 29, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10,
        49, 110, 127, 124, 119, 127, 123, 58, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13,
        82, 127, 109, 70, 44, 34, 86, 127, 116, 13, 0, 0, 0, 0, 0, 0, 0, 1, 23,
        101, 124, 59, 9, 0, 0, 0, 20, 115, 127, 19, 0, 0, 0, 0, 0, 0, 0, 36,
        120, 127, 77, 0, 0, 0, 0, 0, 20, 117, 118, 14, 0, 0, 0, 0, 0, 0, 17,
        100, 127, 97, 36, 1, 0, 0, 0, 0, 58, 127, 69, 0, 0, 0, 0, 0, 0, 21, 100,
        127, 103, 21, 0, 0, 0, 0, 0, 0, 92, 109, 19, 0, 0, 0, 0, 0, 5, 89, 127,
        127, 113, 50, 0, 0, 0, 0, 0, 47, 120, 59, 0, 0, 0, 0, 0, 0, 17, 125,
        121, 99, 112, 27, 0, 0, 0, 0, 16, 106, 93, 7, 0, 0, 0, 0, 0, 0, 5, 89,
        127, 78, 42, 0, 0, 0, 0, 12, 93, 89, 19, 0, 0, 0, 0, 0, 0, 0, 0, 15, 54,
        127, 113, 95, 93, 93, 92, 108, 106, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        50, 65, 99, 127, 127, 127, 94, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 8, 37, 41, 39, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0)

      dut.slaveIO.tready.expect(true.B)
      for (i <- 0 until imageTest.length) {
        dut.slaveIO.tvalid.poke(true.B)
        dut.slaveIO.tdata.poke(imageTest(i))
        dut.slaveIO.tlast.poke(
          if (i == imageTest.length - 1) true.B else false.B
        )
        if (i != imageTest.length - 1) {
          dut.clock.step(1)
        }
      }

      dut.masterIO.tready.poke(true.B)
      dut.clock.step(1)

      for (_ <- 0 until (401 * 25)) {
        // print(f"[${dut.io.outputState.peek().litValue}]")
        dut.clock.step(1)
      }

      // (-3).asUInt = (101).asUInt = 5
      // println(dut.io.testValue.peek().litValue)
      // println(f"weights ${dut.io.outputWeight.peek().litValue}")
      // println(f"sending ${dut.io.outputState.peek().litValue}")
      // println(f"layer0 int  ${dut.io.outputMultiplication.peek().litValue}")
      // println(f"layer0 uint ${dut.io.outputUMultiplication.peek().litValue}")
      // dut.masterIO.tvalid.expect(true.B)
      // dut.masterIO.tdata.expect(10)
      // println(f"sigmoid ${dut.io.outputSigmoid.peek().litValue}")
      println(f"sigmoid(0) ${dut.io.outputSigmoid0.peek().litValue}")
      println(f"state ${dut.io.outputState.peek().litValue}")
      dut.clock.step(1)
      println(f"sigmoid(0) ${dut.io.outputSigmoid0.peek().litValue}")
      println(f"sigmoid(1) ${dut.io.outputSigmoid1.peek().litValue}")
      println(f"sigmoid(2) ${dut.io.outputSigmoid2.peek().litValue}")
      println(f"sigmoid(3) ${dut.io.outputSigmoid3.peek().litValue}")
      println(f"sigmoid(4) ${dut.io.outputSigmoid4.peek().litValue}")
      println(f"sigmoid(5) ${dut.io.outputSigmoid5.peek().litValue}")
      println(f"sigmoid(6) ${dut.io.outputSigmoid6.peek().litValue}")
      println(f"sigmoid(7) ${dut.io.outputSigmoid7.peek().litValue}")
      dut.clock.step(1)
      println(f"state ${dut.io.outputState.peek().litValue}")
    }
  }

}

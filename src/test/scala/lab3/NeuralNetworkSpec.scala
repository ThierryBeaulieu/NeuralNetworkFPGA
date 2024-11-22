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
      dut.io.outputTestWeight.expect(255.U)
    }
  }

  "NeuralNetwork should a sigmoid approximation" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      val imageTest = Seq(255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 255, 255, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 254, 253, 255, 3, 8, 5, 3, 2, 255, 250, 250, 0, 254, 251,
        0, 0, 0, 0, 0, 1, 3, 4, 1, 6, 10, 7, 4, 4, 255, 249, 243, 253, 252, 246,
        0, 1, 0, 0, 0, 4, 8, 12, 1, 254, 4, 254, 254, 5, 250, 247, 240, 238,
        248, 251, 2, 1, 0, 0, 3, 5, 7, 2, 3, 255, 251, 255, 15, 17, 0, 234, 225,
        239, 1, 8, 7, 1, 0, 1, 7, 15, 7, 255, 1, 5, 8, 5, 5, 9, 8, 244, 231,
        248, 250, 1, 10, 2, 0, 1, 10, 22, 4, 1, 2, 0, 249, 254, 242, 244, 251,
        253, 237, 244, 234, 247, 15, 2, 0, 1, 9, 15, 1, 0, 255, 250, 239, 253,
        238, 246, 16, 25, 1, 247, 231, 248, 18, 4, 0, 2, 4, 2, 244, 254, 5, 247,
        242, 232, 230, 254, 36, 27, 9, 4, 223, 242, 9, 4, 0, 3, 254, 255, 246,
        243, 254, 236, 232, 222, 1, 22, 39, 15, 13, 248, 229, 2, 16, 3, 0, 4, 2,
        242, 239, 253, 248, 244, 7, 15, 30, 40, 16, 9, 22, 248, 237, 3, 14, 1,
        0, 4, 8, 250, 242, 254, 241, 255, 20, 24, 17, 26, 6, 2, 255, 244, 233,
        2, 10, 2, 0, 3, 9, 252, 237, 237, 240, 5, 8, 15, 13, 15, 12, 247, 249,
        246, 235, 250, 7, 2, 0, 1, 12, 2, 241, 246, 1, 11, 29, 24, 24, 10, 6,
        252, 1, 252, 244, 254, 3, 0, 0, 1, 8, 255, 232, 239, 239, 251, 22, 21,
        20, 7, 246, 250, 253, 253, 254, 2, 5, 5, 0, 0, 2, 251, 237, 228, 232,
        239, 250, 253, 253, 2, 6, 5, 2, 253, 254, 1, 2, 2, 0, 0, 255, 253, 245,
        238, 231, 240, 251, 255, 251, 252, 8, 12, 0, 253, 1, 1, 0, 0, 0, 0, 0,
        255, 252, 248, 247, 248, 253, 1, 2, 3, 5, 3, 0, 254, 0, 0, 0, 0, 0, 0,
        0, 0, 255, 254, 255, 0, 255, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0)

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

        dut.clock.step(1)
      }

      print(dut.io.layer1Value.peek().litValue)
      // dut.masterIO.tvalid.expect(true.B)
      // dut.masterIO.tdata.expect(10)
      // dut.clock.step(1)

    }
  }

}

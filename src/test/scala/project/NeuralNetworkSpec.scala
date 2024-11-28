package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NeuralNetworkSpec

class NeuralNetworkSpec extends AnyFreeSpec with Matchers {

  "NeuralNetwork should have accessible weights" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.weights.length.mustBe(10)
      dut.weights(0).length.mustBe(401)
    }
  }

  "NeuralNetwork should a sigmoid approximation" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      val imageTest = Seq(255, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 138, 162, 163, 164,
        163, 157, 132, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        138, 177, 238, 255, 252, 247, 255, 251, 186, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 141, 210, 255, 237, 198, 172, 162, 214, 255,
        244, 141, 128, 128, 128, 128, 128, 128, 128, 129, 151, 229, 252, 187,
        137, 128, 128, 128, 148, 243, 255, 147, 128, 128, 128, 128, 128, 128,
        128, 164, 248, 255, 205, 128, 128, 128, 128, 128, 148, 245, 246, 142,
        128, 128, 128, 128, 128, 128, 145, 228, 255, 225, 164, 129, 128, 128,
        128, 128, 186, 255, 197, 128, 128, 128, 128, 128, 128, 149, 228, 255,
        231, 149, 128, 128, 128, 128, 128, 128, 220, 237, 147, 128, 128, 128,
        128, 128, 133, 217, 255, 255, 241, 178, 128, 128, 128, 128, 128, 175,
        248, 187, 128, 128, 128, 128, 128, 128, 145, 253, 249, 227, 240, 155,
        128, 128, 128, 128, 144, 234, 221, 135, 128, 128, 128, 128, 128, 128,
        133, 217, 255, 206, 170, 128, 128, 128, 128, 140, 221, 217, 147, 128,
        128, 128, 128, 128, 128, 128, 128, 143, 182, 255, 241, 223, 221, 221,
        220, 236, 234, 137, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 178, 193, 227, 255, 255, 255, 222, 156, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 136, 165, 169, 167, 139,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128)

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

      // handling
      for (_ <- 0 until 1024) {
        // print(f"res[${dut.io.outputStream.peek().litValue}]")
        // print(f"[${dut.io.outputState.peek().litValue}]")
        dut.clock.step(1)
      }

      val expectedValues = Seq(37, 61, 101, 32, 23, 67, 61, 79, 73, 121)

      // sending
      for (i <- 0 until 10) {
        dut.masterIO.tvalid.expect(true.B)
        // print(f"[${dut.masterIO.tdata.peek().litValue}]")
        // print(f"${i} ${dut.masterIO.tlast.peek().litValue}")
        dut.masterIO.tlast.expect((i == expectedValues.length - 1).B)
        dut.masterIO.tdata.expect(expectedValues(i))
        dut.clock.step(1)
      }
      // print(f" after ${dut.masterIO.tlast.peek().litValue}")

    }
  }
}

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

  "NeuralNetwork should a sigmoid approximation" in {
    simulate(new NeuralNetwork) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      val imageTest = Seq(255, 255, 255, 255, 255, 255, 255, 255)

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
        // io.outputState := 3.U
        // print(f"[${dut.io.outputState.peek().litValue}]")
        dut.clock.step(1)
      }

      // val expectedValues = Seq(37, 61, 101, 32, 23, 67, 61, 79, 73, 121)
      print(f"[${dut.io.outputState.peek().litValue}]")
      dut.clock.step(1)
      dut.masterIO.tvalid.expect(true.B)
      // sending
      print(f"[${dut.masterIO.tdata.peek().litValue}]")
      print(f"{${dut.masterIO.tlast.peek().litValue}")
      dut.clock.step(1)
      print(f"data : ${dut.masterIO.tdata.peek().litValue}, ")
      print(f"tlast : ${dut.masterIO.tlast.peek().litValue}")
      // dut.masterIO.tlast.expect((i == expectedValues.length - 1).B)
      // dut.masterIO.tdata.expect(expectedValues(i))
      // print(f" after ${dut.masterIO.tlast.peek().litValue}")

    }
  }
}

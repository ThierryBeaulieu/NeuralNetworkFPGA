package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NeuronWrapperSpec

class NeuronWrapperSpec extends AnyFreeSpec with Matchers {

  "NeuronWrapper should be able to make Stanh of 0.5 : 0 < x < 1" in {
    simulate(new NeuronWrapper(8, 128, "weights_reduce.csv")) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.weights.length.mustBe(10)
      dut.weights(0).length.mustBe(8)

      // sending
      val imageTest = Seq(255, 255, 0, 0, 0, 0, 0, 0)

      dut.slaveIO.tready.expect(true.B)
      for (i <- 0 until imageTest.length) {
        dut.slaveIO.tvalid.poke(true.B)
        dut.slaveIO.tdata.poke(imageTest(i))
        dut.slaveIO.tlast.poke(
          if (i == imageTest.length - 1) true.B else false.B
        )
        if (i == imageTest.length - 1) {
          dut.masterIO.tready.poke(true.B)
        }
        dut.clock.step(1)
      }

      // processing each 10 elements
      for (_ <- 0 until 10) {
        println("")
        print("pixels")
        for (i <- 0 until 8) {
          print(f"[${dut.io.outputPixels(i).peek().litValue}]")
        }
        println("")
        print("weights")
        for (i <- 0 until 8) {
          print(f"[${dut.io.outputWeights(i).peek().litValue}]")
        }
        println("")
        print("B2IS")
        for (i <- 0 until 8) {
          print(f"[${dut.io.outputB2ISValues(i).peek().litValue}]")
        }
        println("")
        print("B2S")
        for (i <- 0 until 8) {
          print(f"[${dut.io.outputB2SValues(i).peek().litValue}]")
        }
        println("")
        print("AND")
        for (i <- 0 until 8) {
          print(f"[${dut.io.outputANDValues(i).peek().litValue}]")
        }
        println("")
        print("tree adder")
        print(f"[${dut.io.outputTreeAdder.peek().litValue}]")
        dut.clock.step(1)
      }

      // handling
      println("")
      print("Result : ")
      for (_ <- 0 until 10) {
        // print(f"[${dut.io.outputTreeAdder.peek().litValue}]")
        print(f"(${dut.masterIO.tdata.peek().litValue})")
        dut.clock.step(1)
      }
    }

  }
}

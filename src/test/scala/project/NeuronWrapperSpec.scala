package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NeuronWrapperSpec

class NeuronWrapperSpec extends AnyFreeSpec with Matchers {

  "NeuronWrapperSpec should have accessible weights" in {
    simulate(new NeuronWrapper(8, 128, "weights.csv")) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.weights.length.mustBe(1)
      dut.weights(0).length.mustBe(8)
    }
  }
  /*
  "NeuronWrapper should be able to make Stanh of 1 : 0.9999" in {
    simulate(new NeuronWrapper(8, 128, "test_weights_1.csv")) { dut =>
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

      // handling
      // println("255 * 127 should be 1.0 * 1.0 = 1024")
      for (_ <- 0 until 1024) {
        // All equal to 1024
        // print(f"[${dut.io.outputTreeAdder.peek().litValue}]")
        dut.clock.step(1)
      }

      // sending
      dut.masterIO.tready.poke(true.B)
      dut.clock.step(1)

      dut.masterIO.tdata.expect(1024.U)
      dut.masterIO.tlast.expect(true.B)

      // print(f"[${dut.masterIO.tdata.peek().litValue}]")
      // print(f"{${dut.masterIO.tlast.peek().litValue}}")
    }
  }
   */

  "NeuronWrapper should be able to make Stanh of 0.5 : 0 < x < 1" in {
    simulate(new NeuronWrapper(8, 128, "weights.csv")) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      // 0.5 * 0.5 = 0.25
      val imageTest = Seq(255, 0, 0, 0, 0, 0, 0, 0)

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

      /*
        val outputB2SValues = Output(Vec(nbData, UInt(1.W)))
        val outputB2ISValues = Output(Vec(nbData, SInt(2.W)))
        val outputANDValues = Output(Vec(nbData, SInt(2.W)))
        val outputTreeAdder = Output(SInt((nbData + 1).W))
       */

      // handling
      dut.clock.step(2)
      print(f"[${dut.io.outputTreeAdder.peek().litValue}]")

      // sending
      dut.masterIO.tready.poke(true.B)
      dut.clock.step(1)

      dut.masterIO.tdata.expect(511.S)
      dut.masterIO.tlast.expect(true.B)

      // print(f"[${dut.masterIO.tdata.peek().litValue}]")
      // print(f"{${dut.masterIO.tlast.peek().litValue}}")
    }
  }
}

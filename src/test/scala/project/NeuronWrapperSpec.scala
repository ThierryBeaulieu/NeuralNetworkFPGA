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

  "NeuronWrapper should a sigmoid approximation" in {
    simulate(new NeuronWrapper) { dut =>
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
      for (_ <- 0 until 1024) {
        dut.clock.step(1)
      }

      // sending
      dut.masterIO.tready.poke(true.B)
      dut.clock.step(1)

      dut.masterIO.tdata.expect(1023.U)
      dut.masterIO.tlast.expect(true.B)

      // print(f"[${dut.masterIO.tdata.peek().litValue}]")
      // print(f"{${dut.masterIO.tlast.peek().litValue}}")
    }
  }

  "NeuronWrapper should work with pixels averaging 128" in {
    simulate(new NeuronWrapper) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      val imageTest = Seq(128, 128, 128, 128, 128, 128, 128, 128)

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
      for (_ <- 0 until 1024) {
        dut.clock.step(1)
      }

      // sending
      dut.masterIO.tready.poke(true.B)
      dut.clock.step(1)

      dut.masterIO.tdata.expect(511.U)
      dut.masterIO.tlast.expect(true.B)

      // print(f"[${dut.masterIO.tdata.peek().litValue}]")
      // print(f"{${dut.masterIO.tlast.peek().litValue}}")
    }
  }
}

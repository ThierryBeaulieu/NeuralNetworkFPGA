package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NeuronWrapperSpec

class NeuronWrapperSpec extends AnyFreeSpec with Matchers {
  /*
  "NeuronWrapper should a sigmoid approximation" in {
    simulate(new NeuronWrapper) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      val imageTest = Seq(255, 255, 255, 255, 255, 255, 255, 255)

      dut.slaveIO1.tready.expect(true.B)
      for (i <- 0 until imageTest.length) {
        dut.slaveIO1.tvalid.poke(true.B)
        dut.slaveIO1.tdata.poke(imageTest(i))
        dut.slaveIO1.tlast.poke(
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

      dut.masterIO.tdata.expect(1024.U)
      dut.masterIO.tlast.expect(true.B)

      // print(f"[${dut.masterIO.tdata.peek().litValue}]")
      // print(f"{${dut.masterIO.tlast.peek().litValue}}")
    }
  }
   */
  "NeuronWrapper should work with pixels averaging 128" in {
    simulate(new NeuronWrapper) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      val imageTest = Seq(128, 128, 128, 128, 128, 128, 128, 128)
      val weightTest = Seq(127, 127, 127, 127, 127, 127, 127, 127)

      dut.slaveIO1.tready.expect(true.B)
      dut.slaveIO2.tready.expect(true.B)

      for (i <- 0 until imageTest.length) {
        dut.slaveIO1.tvalid.poke(true.B)
        dut.slaveIO1.tdata.poke(imageTest(i))
        dut.slaveIO1.tlast.poke(
          if (i == imageTest.length - 1) true.B else false.B
        )
        println("image")
        for (i <- 0 until 8) {
          print(f"[${dut.io.image(i).peek().litValue}]")
        }
        if (i !== (imageTest.length - 1)) {
          dut.clock.step(1)
        }
      }

      for (i <- 0 until weightTest.length) {
        dut.slaveIO2.tvalid.poke(true.B)
        dut.slaveIO2.tdata.poke(weightTest(i))
        dut.slaveIO2.tlast.poke(
          if (i == weightTest.length - 1) true.B else false.B
        )
        println("weights")
        for (i <- 0 until 8) {
          print(f"[${dut.io.weights(i).peek().litValue}]")
        }
        if (i !== (weightTest.length - 1)) {
          dut.clock.step(1)
        }
      }

      // todo: Vérifier valeurs de l'image Test et du weight Test
      print("image")
      for (i <- 0 until 8) {
        print(f"[${dut.io.image(i).peek().litValue}]")
      }
      print("weights")
      for (i <- 0 until 8) {
        print(f"[${dut.io.weights(i).peek().litValue}]")
      }

      /*
        val outputB2SValues = Output(Vec(nbData, UInt(1.W)))
        val outputB2ISValues = Output(Vec(nbData, SInt(2.W)))
        val outputANDValues = Output(Vec(nbData, SInt(2.W)))
        val outputTreeAdder = Output(SInt((nbData + 1).W))
       */

      // handling
      for (_ <- 0 until 1024) {
        // println(f"output state ${dut.io.outputState.peek().litValue}")
        // print(f"[${dut.io.outputStream.peek().litValue}]")
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

package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NeuronWrapperSpec

class NeuronWrapperSpec extends AnyFreeSpec with Matchers {

  "NeuronWrapperSpec should have accessible weights" in {
    simulate(new NeuronWrapper(8, 128, "weights_reduce_test.csv")) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.weights.length.mustBe(10)
      dut.weights(0).length.mustBe(8)

      print(dut.weights(0)(0).peek().litValue)
    }
  }

  "NeuronWrapper should be able to make Stanh of 0.5 : 0 < x < 1" in {
    simulate(new NeuronWrapper(8, 128, "weights_reduce_test.csv")) { dut =>
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

      println("valeur weights")
      for (i <- 0 until 8) {
        print(dut.io.outputWeight(i).peek().litValue)
      }

      dut.clock.step(1)

      println("valeur pixels")
      for (i <- 0 until 8) {
        print(dut.io.outputImage(i).peek().litValue)
      }

      /*
        val outputB2SValues = Output(Vec(nbData, UInt(1.W)))
        val outputB2ISValues = Output(Vec(nbData, SInt(2.W)))
        val outputANDValues = Output(Vec(nbData, SInt(2.W)))
        val outputTreeAdder = Output(SInt((nbData + 1).W))
       */

      // handling
      // print(f"B2S[${dut.io.outputB2SValues(1).peek().litValue}]")
      // print(f"B2IS[${dut.io.outputB2ISValues(1).peek().litValue}]")
      // print(f"AND[${dut.io.outputANDValues(1).peek().litValue}]")
      // ça ne devrait pas être le cas, on devriat attend 10? avant de lae faire?
      for (_ <- 0 until 20) {
        println("valeur weights")
        for (i <- 0 until 8) {
          print(dut.io.outputWeight(i).peek().litValue)
        }
        // Pourquoi les wieghts n'affichent rien

        println(f"Tree Adder[${dut.io.outputTreeAdder.peek().litValue}]")
        dut.clock.step(1)
      }

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

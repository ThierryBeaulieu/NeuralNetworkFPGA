package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.B2ISBipolarSpec

class B2ISBipolarSpec extends AnyFreeSpec with Matchers {

  "Should produce a bipolar stream m=2, S=1, x1=x2=0.75" in {
    simulate(new B2ISBipolar(2)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      dut.io.inputWeight.poke(1.S)
      dut.clock.step(1)

      // print(dut.io.outputVal.peek().litValue)
      var sum = 0
      val nb_cycles = 1024
      for (_ <- 0 until nb_cycles) {
        sum += dut.io.outputStream.peek().litValue.toInt
        dut.clock.step(1)
      }
      sum = sum / nb_cycles

      assert(sum - 1.0 < 0.1)
    }
  }

  "Should produce a bipolar stream m=2, S=-1, x1=x2=0.25" in {
    simulate(new B2ISBipolar(2)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      dut.io.inputWeight.poke(-1.S)
      dut.clock.step(1)

      // print(dut.io.outputVal.peek().litValue)
      var sum = 0
      val nb_cycles = 1024
      for (_ <- 0 until nb_cycles) {
        sum += dut.io.outputStream.peek().litValue.toInt
        dut.clock.step(1)
      }
      sum = sum / nb_cycles

      assert(sum + 1.0 < 0.1)
    }
  }
}

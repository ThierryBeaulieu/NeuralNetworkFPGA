package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NStanhSpec

class NStanhSpec extends AnyFreeSpec with Matchers {

  "Should produce return an approximation of tanh" in {
    simulate(new NStanh(2.S, 6.S)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val expectedUnipolarStream =
        Seq(
          0.U(2.W),
          0.U(2.W),
          0.U(2.W),
          0.U(2.W)
        )
      val inputBipolarStream = Seq(
        -4.S(3.W),
        -4.S(3.W),
        -4.S(3.W),
        -4.S(3.W)
      )

      val cycle = 1024

      for (i <- 0 until cycle) {
        dut.io.inputSi.poke(inputBipolarStream(i))
        dut.clock.step(1)
        print(dut.io.outputStream.peek().litValue)
        dut.io.outputStream.expect(expectedUnipolarStream(i))
      }
    }
  }
}

package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.OperationsSpec

class OperationsSpec extends AnyFreeSpec with Matchers {

  "Should produce return a stream of bipolar values with bit = 1" in {
    simulate(new Operations) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val inputIntegers = Seq(
        1.S(2.W),
        0.S(2.W),
        -1.S(2.W)
      )
      val inputBit = Seq(
        1.U(1.W),
        1.U(1.W),
        1.U(1.W)
      )
      val expectedBipolarStream =
        Seq(
          1.S(2.W),
          0.S(2.W),
          -1.S(2.W)
        )

      for (i <- 0 until expectedBipolarStream.length) {
        dut.io.inputInteger.poke(inputIntegers(i))
        dut.io.inputBit.poke(inputBit(i))
        dut.clock.step(1)
        dut.io.outputStream.expect(expectedBipolarStream(i))
      }
    }
  }

  "Should produce return a stream of bipolar values with bit = 0" in {
    simulate(new BitwiseAND) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val inputIntegers = Seq(
        1.S(2.W),
        0.S(2.W),
        -1.S(2.W)
      )
      val inputBit = Seq(
        0.U(1.W),
        0.U(1.W),
        0.U(1.W)
      )
      val expectedBipolarStream =
        Seq(
          0.S(2.W),
          0.S(2.W),
          0.S(2.W)
        )

      for (i <- 0 until expectedBipolarStream.length) {
        dut.io.inputInteger.poke(inputIntegers(i))
        dut.io.inputBit.poke(inputBit(i))
        dut.clock.step(1)
        dut.io.outputStream.expect(expectedBipolarStream(i))
      }
    }
  }

  "Should produce return a stream of bipolar values with bit {0, 1}" in {
    simulate(new BitwiseAND) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val inputIntegers = Seq(
        1.S(2.W),
        0.S(2.W),
        -1.S(2.W),
        1.S(2.W),
        0.S(2.W),
        -1.S(2.W)
      )
      val inputBit = Seq(
        0.U(1.W),
        0.U(1.W),
        0.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W)
      )
      val expectedBipolarStream =
        Seq(
          0.S(2.W),
          0.S(2.W),
          0.S(2.W),
          1.S(2.W),
          0.S(2.W),
          -1.S(2.W)
        )

      for (i <- 0 until expectedBipolarStream.length) {
        dut.io.inputInteger.poke(inputIntegers(i))
        dut.io.inputBit.poke(inputBit(i))
        dut.clock.step(1)
        dut.io.outputStream.expect(expectedBipolarStream(i))
      }
    }
  }
}

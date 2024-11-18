package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NStanhSpec

class NStanhSpec extends AnyFreeSpec with Matchers {

  "Should produce return an approximation of tanh for a stream [-128, -128]" in {
    simulate(new NStanh(2.S, 6.S)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // Equivalent of Weight [-128, -128] and Pixel [255, 255]
      val inputBipolarStream = Seq(
        -2.S(3.W),
        -2.S(3.W),
        -2.S(3.W),
        -2.S(3.W),
        -2.S(3.W),
        -2.S(3.W),
        -2.S(3.W),
        -2.S(3.W)
      )

      val expectedUnipolarStream =
        Seq(
          0.U(2.W),
          0.U(2.W),
          0.U(2.W),
          0.U(2.W),
          0.U(2.W),
          0.U(2.W),
          0.U(2.W),
          0.U(2.W)
        )

      for (i <- 0 until expectedUnipolarStream.length) {
        dut.io.inputSi.poke(inputBipolarStream(i))
        dut.clock.step(1)
        // print(dut.io.outputStream.peek().litValue)
        dut.io.outputStream.expect(expectedUnipolarStream(i))
      }
    }
  }

  "Should produce return an approximation of tanh for a stream [127, 127]" in {
    simulate(new NStanh(2.S, 6.S)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // Equivalent of Weight [127, 127] and Pixel [255, 255]
      val inputBipolarStream = Seq(
        2.S(3.W),
        2.S(3.W),
        2.S(3.W),
        2.S(3.W),
        2.S(3.W),
        2.S(3.W),
        2.S(3.W),
        2.S(3.W),
        2.S(3.W),
        2.S(3.W)
      )

      val expectedUnipolarStream =
        Seq(
          0.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W)
        )

      val cycle = 1024

      for (i <- 0 until expectedUnipolarStream.length) {
        dut.io.inputSi.poke(inputBipolarStream(i))
        dut.clock.step(1)
        // print(dut.io.outputStream.peek().litValue)
        dut.io.outputStream.expect(expectedUnipolarStream(i))
      }
    }
  }

  "Should produce return an approximation of tanh for a stream [0, 0]" in {
    simulate(new NStanh(2.S, 6.S)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // Equivalent of Weight [0, 0] and Pixel [255, 255]
      val inputBipolarStream = Seq(
        0.S(3.W),
        -2.S(3.W),
        2.S(3.W),
        0.S(3.W),
        2.S(3.W),
        0.S(3.W),
        0.S(3.W),
        0.S(3.W),
        0.S(3.W),
        -2.S(3.W)
      )

      val expectedUnipolarStream =
        Seq(
          0.U(2.W),
          0.U(2.W),
          0.U(2.W),
          0.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          1.U(2.W),
          0.U(2.W)
        )

      val cycle = 1024

      for (i <- 0 until inputBipolarStream.length) {
        dut.io.inputSi.poke(inputBipolarStream(i))
        dut.clock.step(1)
        print(dut.io.outputStream.peek().litValue)
        // dut.io.outputStream.expect(expectedUnipolarStream(i))
      }
    }
  }
}

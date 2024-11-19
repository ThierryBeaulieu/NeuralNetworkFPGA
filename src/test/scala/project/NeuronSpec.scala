package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NeuronSpec

class NeuronSpec extends AnyFreeSpec with Matchers {

  "Neuron should generate stream with a single B2S" in {
    simulate(new Neuron(nbData = 1)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // incoming bipolar stochastic stream
      dut.io.inputPixels(0).poke(127.U)
      dut.io.inputWeights(0).poke(127.S)

      val expectedUnipolarStream = Seq(
        1.U(1.W),
        0.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        0.U(1.W),
        1.U(1.W),
        0.U(1.W),
        1.U(1.W),
        1.U(1.W),
        0.U(1.W),
        1.U(1.W)
      )
      for (i <- 0 until expectedUnipolarStream.length) {
        dut.clock.step(1)
        // print(dut.io.outputB2SValues(0).peek().litValue)
        dut.io.outputB2SValues(0).expect(expectedUnipolarStream(i))
      }
    }
  }

  "Neuron should generate stream with multiple B2S" in {
    simulate(new Neuron(nbData = 2)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // incoming bipolar stochastic stream
      dut.io.inputPixels(0).poke(127.U)
      dut.io.inputWeights(0).poke(127.S)

      dut.io.inputPixels(1).poke(255.U)
      dut.io.inputWeights(1).poke(127.S)

      val expectedUnipolarStream1 = Seq(
        1.U(1.W),
        0.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        0.U(1.W),
        1.U(1.W),
        0.U(1.W),
        1.U(1.W),
        1.U(1.W),
        0.U(1.W),
        1.U(1.W)
      )

      val expectedUnipolarStream2 = Seq(
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W),
        1.U(1.W)
      )

      for (i <- 0 until expectedUnipolarStream1.length) {
        dut.clock.step(1)
        // print(dut.io.outputB2SValues(0).peek().litValue)
        dut.io.outputB2SValues(0).expect(expectedUnipolarStream1(i))
      }

      for (i <- 0 until expectedUnipolarStream2.length) {
        dut.clock.step(1)
        // print(dut.io.outputB2SValues(1).peek().litValue)
        dut.io.outputB2SValues(1).expect(expectedUnipolarStream2(i))
      }
    }
  }

  "Neuron should generate bipolar stream with a single B2ISBipolar" in {
    simulate(new Neuron(nbData = 1)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // incoming bipolar stochastic stream
      dut.io.inputPixels(0).poke(127.U)
      dut.io.inputWeights(0).poke(0.S)

      val expectedBipolarStream1 = Seq(
        1.S(2.W),
        -1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        -1.S(2.W),
        1.S(2.W),
        -1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        -1.S(2.W),
        1.S(2.W)
      )

      for (i <- 0 until expectedBipolarStream1.length) {
        dut.clock.step(1)
        // print(dut.io.outputB2ISValues(0).peek().litValue)
        dut.io.outputB2ISValues(0).expect(expectedBipolarStream1(i))
      }
    }
  }

  "Neuron should generate bipolar stream with multiple B2ISBipolar" in {
    simulate(new Neuron(nbData = 3)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // incoming bipolar stochastic stream
      dut.io.inputPixels(0).poke(127.U)
      dut.io.inputWeights(0).poke(0.S)

      dut.io.inputPixels(1).poke(127.U)
      dut.io.inputWeights(1).poke(127.S)

      dut.io.inputPixels(2).poke(127.U)
      dut.io.inputWeights(2).poke(-127.S)

      val expectedBipolarStream1 = Seq(
        1.S(2.W),
        -1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        -1.S(2.W),
        1.S(2.W),
        -1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        -1.S(2.W),
        1.S(2.W)
      )

      val expectedBipolarStream2 = Seq(
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W)
      )

      val expectedBipolarStream3 = Seq(
        -1.S(2.W),
        -1.S(2.W),
        -1.S(2.W),
        -1.S(2.W),
        -1.S(2.W),
        -1.S(2.W),
        -1.S(2.W),
        -1.S(2.W),
        -1.S(2.W),
        -1.S(2.W),
        -1.S(2.W),
        -1.S(2.W)
      )

      for (i <- 0 until expectedBipolarStream1.length) {
        dut.clock.step(1)
        // print(dut.io.outputB2ISValues(0).peek().litValue)
        dut.io.outputB2ISValues(0).expect(expectedBipolarStream1(i))
      }

      for (i <- 0 until expectedBipolarStream2.length) {
        dut.clock.step(1)
        // print(dut.io.outputB2ISValues(1).peek().litValue)
        dut.io.outputB2ISValues(1).expect(expectedBipolarStream2(i))
      }

      for (i <- 0 until expectedBipolarStream3.length) {
        dut.clock.step(1)
        // print(dut.io.outputB2ISValues(2).peek().litValue)
        dut.io.outputB2ISValues(2).expect(expectedBipolarStream3(i))
      }
    }
  }

  "Neuron should apply the Bitwise AND operator on the streams" in {
    simulate(new Neuron(nbData = 3)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // incoming bipolar stochastic stream
      dut.io.inputPixels(0).poke(127.U)
      dut.io.inputWeights(0).poke(-32.S)

      dut.io.inputPixels(1).poke(255.U)
      dut.io.inputWeights(1).poke(32.S)

      dut.io.inputPixels(2).poke(0.U)
      dut.io.inputWeights(2).poke(127.S)

      val expectedBipolarStream1 = Seq(
        0.S(2.W),
        1.S(2.W),
        0.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        0.S(2.W),
        1.S(2.W),
        0.S(2.W),
        1.S(2.W)
      )

      val expectedBipolarStream2 = Seq(
        1.S(2.W),
        -1.S(2.W),
        1.S(2.W),
        -1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        1.S(2.W),
        -1.S(2.W),
        -1.S(2.W)
      )

      val expectedBipolarStream3 = Seq(
        0.S(2.W),
        0.S(2.W),
        0.S(2.W),
        0.S(2.W),
        0.S(2.W),
        0.S(2.W),
        0.S(2.W),
        0.S(2.W),
        0.S(2.W),
        0.S(2.W)
      )

      for (i <- 0 until expectedBipolarStream1.length) {
        dut.clock.step(1)
        // print(dut.io.outputANDValues(0).peek().litValue)
        dut.io.outputANDValues(0).expect(expectedBipolarStream1(i))
      }

      for (i <- 0 until expectedBipolarStream1.length) {
        dut.clock.step(1)
        // print(dut.io.outputANDValues(1).peek().litValue)
        dut.io.outputANDValues(1).expect(expectedBipolarStream2(i))
      }

      for (i <- 0 until expectedBipolarStream1.length) {
        dut.clock.step(1)
        // print(dut.io.outputANDValues(2).peek().litValue)
        dut.io.outputANDValues(2).expect(expectedBipolarStream3(i))
      }
    }
  }

  "Neuron should have the TreeAdder reduce a set of streams" in {
    simulate(new Neuron(nbData = 4)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      // incoming bipolar stochastic stream
      dut.io.inputPixels(0).poke(220.U)
      dut.io.inputWeights(0).poke(0.S)

      dut.io.inputPixels(1).poke(220.U)
      dut.io.inputWeights(1).poke(32.S)

      dut.io.inputPixels(2).poke(220.U)
      dut.io.inputWeights(2).poke(-32.S)

      dut.io.inputPixels(3).poke(220.U)
      dut.io.inputWeights(3).poke(0.S)

      val expectedStream = Seq(
        0.S(3.W),
        4.S(3.W),
        -2.S(3.W),
        4.S(3.W),
        4.S(3.W),
        4.S(3.W),
        -4.S(3.W),
        4.S(3.W),
        -2.S(3.W),
        4.S(3.W),
        4.S(3.W),
        -4.S(3.W)
      )

      for (i <- 0 until expectedStream.length) {
        dut.clock.step(1)
        // print(dut.io.outputTreeAdder.peek().litValue)
        dut.io.outputTreeAdder.expect(expectedStream(i))
      }
    }
  }

}

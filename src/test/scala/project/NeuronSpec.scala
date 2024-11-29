package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.NeuronSpec

class NeuronSpec extends AnyFreeSpec with Matchers {
  "Neuron should produce a unipolar stream for a pixel=255" in {
    simulate(new Neuron(2)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      for (i <- 0 until 2) {
        dut.io.inputPixels(i).poke(255.U)
        dut.io.inputWeights(i).poke(255.S)
      }
      dut.clock.step(1)

      for (_ <- 0 until 10) {
        dut.io.outputB2SValues(0).expect(1)
        dut.io.outputB2SValues(1).expect(1)
        dut.clock.step(1)
      }
    }
  }

  "Neuron should produce a unipolar stream for a pixel=0" in {
    simulate(new Neuron(2)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      for (i <- 0 until 2) {
        dut.io.inputPixels(i).poke(0.U)
        dut.io.inputWeights(i).poke(255.S)
      }
      dut.clock.step(1)

      for (_ <- 0 until 10) {
        dut.io.outputB2SValues(0).expect(0)
        dut.io.outputB2SValues(1).expect(0)
        dut.clock.step(1)
      }
    }
  }

  "Neuron should produce a unipolar stream for a pixel=128" in {
    simulate(new Neuron(2)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      for (i <- 0 until 2) {
        dut.io.inputPixels(i).poke(128.U)
        dut.io.inputWeights(i).poke(255.S)
      }
      dut.clock.step(1)
      print("stream should contain 50% 0 50% 1 : ")
      for (_ <- 0 until 10) {
        print(dut.io.outputB2SValues(0).peek().litValue)
        print(dut.io.outputB2SValues(1).peek().litValue)
        dut.clock.step(1)
      }
      println("")
    }
  }

  "Neuron should produce a bipolar stream for a m=8" in {
    simulate(new Neuron(8)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      for (i <- 0 until 8) {
        dut.io.inputPixels(i).poke(128.U)
        dut.io.inputWeights(i).poke(7.S)
      }
      dut.clock.step(1)
      print("The average of the stream should be 7 : ")
      for (i <- 0 until 8) {
        print(dut.io.outputB2ISValues(i).peek().litValue)
        dut.clock.step(1)
      }
      println("")
    }
  }

  "Neuron should produce a multiplied values that averaged Weights * pixel" in {
    simulate(new Neuron(8)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      for (i <- 0 until 8) {
        dut.io.inputPixels(i).poke(128.U)
        dut.io.inputWeights(i).poke(7.S)
      }
      dut.clock.step(1)
      print("The average of the stream should be 3.5 : ")
      for (i <- 0 until 8) {
        print(dut.io.outputANDValues(i).peek().litValue)
        dut.clock.step(1)
      }
      println("")
    }
  }

  "Neuron should have a tree adder that generates a value summing all the values" in {
    simulate(new Neuron(8)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      for (i <- 0 until 8) {
        dut.io.inputPixels(i).poke(128.U)
        dut.io.inputWeights(i).poke(0.S)
      }
      dut.clock.step(2)

      // print(dut.io.outputTreeAdder.peek().litValue)
      // println("")
    }
  }

}

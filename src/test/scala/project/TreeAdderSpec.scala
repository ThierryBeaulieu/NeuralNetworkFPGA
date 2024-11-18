package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.TreeAdderSpec

class TreeAdderSpec extends AnyFreeSpec with Matchers {

  "Should add 4 {1} bipolar stochastic stream to get 4" in {
    simulate(new TreeAdder(nbStream = 4)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val expectedUnipolarStream = 4.S(3.W)
      // incoming bipolar stochastic stream
      dut.io.inputStream(0).poke(1.S)
      dut.io.inputStream(1).poke(1.S)
      dut.io.inputStream(2).poke(1.S)
      dut.io.inputStream(3).poke(1.S)
      dut.clock.step(1)

      dut.io.outputStream.expect(expectedUnipolarStream)
    }
  }

  "Should add 4 {-1} bipolar stochastic stream to get -4" in {
    simulate(new TreeAdder(nbStream = 4)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val expectedUnipolarStream = -4.S(3.W)
      // incoming bipolar stochastic stream
      dut.io.inputStream(0).poke(-1.S)
      dut.io.inputStream(1).poke(-1.S)
      dut.io.inputStream(2).poke(-1.S)
      dut.io.inputStream(3).poke(-1.S)
      dut.clock.step(1)

      dut.io.outputStream.expect(expectedUnipolarStream)
    }
  }

  "Should add 6 bipolar stochastic stream together" in {
    simulate(new TreeAdder(nbStream = 6)) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      val expectedUnipolarStream = -2.S(3.W)
      // incoming bipolar stochastic stream
      dut.io.inputStream(0).poke(-1.S)
      dut.io.inputStream(1).poke(-1.S)
      dut.io.inputStream(2).poke(1.S)
      dut.io.inputStream(3).poke(-1.S)
      dut.io.inputStream(4).poke(1.S)
      dut.io.inputStream(5).poke(-1.S)
      dut.clock.step(1)

      dut.io.outputStream.expect(expectedUnipolarStream)
    }
  }
}

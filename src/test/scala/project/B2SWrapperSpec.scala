package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import project.B2SWrapper

class B2SWrapperSpec extends AnyFreeSpec with Matchers {

  "Should produce a string a 1024 random bits" in {
    simulate(new B2SWrapper) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      dut.io.inputComparator.poke(256.U) // 0 -> 1024 (E(512) = 0.5)
      while (!dut.io.outputValid.peek().litToBoolean) {
        dut.clock.step(1)
      }
      var zeros: Int = 0
      var ones: Int = 0
      for (i <- 0 until 1024) {
        if (dut.io.outputStream(i).peek().litValue == 0) {
          zeros = zeros + 1
        } else {
          ones = ones + 1
        }
      }

      assert(ones == 768, s"Expected 768 ones, but found $ones ones")
      assert(zeros == 256, s"Expected 256 zeros, but found $zeros zeros")
    }
  }

}

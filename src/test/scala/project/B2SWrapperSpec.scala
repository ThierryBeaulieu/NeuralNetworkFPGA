package project

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import project.B2SWrapper

class BS2SWrapperSpec extends AnyFreeSpec with Matchers {

  "should produce a string a 1024 random bits" in {
    simulate(new B2SWrapper) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      dut.io.inputComparator.poke(256.U)
      while (!dut.io.outputValid.peek().litToBoolean) {
        dut.clock.step(1)
      }
      dut.io.outputStream.expect(100.U)
    }
  }

}

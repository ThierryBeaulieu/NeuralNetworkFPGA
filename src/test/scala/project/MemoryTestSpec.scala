package project

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// testOnly project.MemoryTestSpec

class MemoryTestSpec extends AnyFreeSpec with Matchers {

  "Should have the weights stored in memory" in {
    simulate(new MemoryTest("weights_reduce_test.csv")) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      dut.weights.length.mustBe(10)
      dut.weights(0).length.mustBe(8)

      print(dut.io.outputWeight.peek().litValue)

    }
  }

  "content of the memory should be correct" in {
    simulate(new MemoryTest("weights_reduce_test.csv")) { dut =>
      // Reset the DUT
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(1)

      //   val expected = [-50, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      //     0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      //     0, 0, 0, 0, 0, 1, 1, 0, -1, 0, 0, 0, 0, -2, -3, -3, 0, 0, 0, 0, 0, 0, 0,
      //     -2, -8, -13, -18, -10, -11, 0, 4, -1, 0, -7, -10, -15, -19, -18, 0, 0,
      //     0, 0, 0, -1, -5, -41, -32, 0, 19, 29, 0, -7, 0, -16, -15, 26, 3, -17,
      //     -3, 0, 0, 0, -1, 22, 2, -12, -21, -11, 33, 21, -28, -17, -3, -24, -23,
      //     54, 13, -45, -10, 0, 0, 0, -11, -4, -8, -21, -10, -14, -8, -16, -14,
      //     -10, -3, -14, -26, -27, -1, -26, -16, 0, 0, 0, -22, -38, -13, -16, 0,
      //     -11, 10, -34, -34, -18, -32, 19, 32, -12, -1, 0, -35, 0, 0, -3, -17, -9,
      //     -27, -20, 3, 2, -6, -33, -8, 7, -13, -27, 7, -2, -14, -22, -42, -6, 0,
      //     -6, -3, -10, -13, -6, -4, 2, 7, 8, 16, 10, 13, 10, 1, -21, -3, -4, -30,
      //     -1, 0, -3, 9, -20, -34, 4, -5, 9, 45, 44, 30, 34, 0, 19, 0, 1, -3, -23,
      //     -23, 0, 0, -2, 7, 30, 9, -7, -13, 27, 20, 6, -13, 2, -27, -17, 9, 13,
      //     12, -39, -26, 0, 0, -2, -32, -38, -20, -29, -7, 0, 1, 2, 12, 15, -14,
      //     -12, -16, -16, 31, 9, -12, -1, 0, -3, -13, 20, 10, 27, -12, -18, 16,
      //     -20, -19, -36, -19, 18, 23, -11, 17, 5, -10, 0, 0, 0, -7, -8, -2, -7,
      //     -16, -12, -34, -30, -16, -28, -19, 26, -12, -4, 30, -15, -9, 0, 0, 0,
      //     -2, -16, -15, 13, -6, -21, -17, -13, -18, -23, -22, -19, -9, 7, 2, -18,
      //     0, 0, 0, 0, 1, 9, -13, -14, -17, -14, -6, -4, -2, -5, -12, 3, 10, 3, -7,
      //     -5, 0, 0, 0, 0, 0, 5, -13, -19, -10, 0, 0, -4, 0, -1, -5, 6, 33, 37, 8,
      //     -3, 0, 0, 0, 0, 0, -1, -5, -2, -2, 8, 6, -1, 3, 0, -1, 0, 7, 23, 6, 0,
      //     0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, -2, 0, 0, 0, 0]

      //   for (i <- 0 until 401) {
      //     dut.weights(0)(i).expect()
      //   }

    }
  }

}

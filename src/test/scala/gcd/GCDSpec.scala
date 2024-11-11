// See README.md for license details.
package gcd

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly gcd.GCDSpec
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly gcd.GCDSpec'
  * }}}
  * Testing from mill:
  * {{{
  * mill NeuralNetworkFPGA.test.testOnly gcd.GCDSpec
  * }}}
  */
class GCDSpec extends AnyFlatSpec {

  behavior of "GCD"

  it should "calculate the GCD of two numbers" in {
    simulate(new GCD) { c =>
      // Test case 1: GCD(20, 12)
      c.io.value1.poke(20.U)
      c.io.value2.poke(12.U)
      c.io.loadingValues.poke(true.B) // Set loadingValues to true to load inputs
      c.clock.step() // Step the clock once to load values into the registers
      c.io.loadingValues.poke(false.B) // Set loadingValues to false after loading inputs
      c.clock.step() // Step the clock again to compute the GCD
      // Check if the GCD is calculated correctly (GCD(20, 12) should be 4)
      println("GCD(20, 12): " + c.io.outputGCD.peek().litValue)
      assert(c.io.outputGCD.peek().litValue == 4)

      // Test case 2: GCD(42, 56)
      c.io.value1.poke(42.U)
      c.io.value2.poke(56.U)
      c.io.loadingValues.poke(true.B)
      c.clock.step()
      c.io.loadingValues.poke(false.B)
      c.clock.step()
      // Check if the GCD is calculated correctly (GCD(42, 56) should be 14)
      println("GCD(42, 56): " + c.io.outputGCD.peek().litValue)
      assert(c.io.outputGCD.peek().litValue == 14)

      // Test case 3: GCD(7, 13), prime numbers
      c.io.value1.poke(7.U)
      c.io.value2.poke(13.U)
      c.io.loadingValues.poke(true.B)
      c.clock.step()
      c.io.loadingValues.poke(false.B)
      c.clock.step()
      // Check if the GCD is calculated correctly (GCD(7, 13) should be 1)
      println("GCD(7, 13): " + c.io.outputGCD.peek().litValue)
      assert(c.io.outputGCD.peek().litValue == 1)

      // Test case 4: GCD(100, 100)
      c.io.value1.poke(100.U)
      c.io.value2.poke(100.U)
      c.io.loadingValues.poke(true.B)
      c.clock.step()
      c.io.loadingValues.poke(false.B)
      c.clock.step()
      // Check if the GCD is calculated correctly (GCD(100, 100) should be 100)
      println("GCD(100, 100): " + c.io.outputGCD.peek().litValue)
      assert(c.io.outputGCD.peek().litValue == 100)
    }
  }
}
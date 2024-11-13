package project

import chisel3._
import chisel3.util.random.PRNG
import chisel3.util.random.LFSR

/** Linear-Feedback shift register. Returns a random generated number.
  */
class B2S extends Module {
  val io = IO(new Bundle {
    val outputStream = Output(UInt(8.W))
    val outputValid = Output(Bool())
  })

  // Initialize the LFSR with a given width
  val randomNumber: UInt = 

  // Connect the LFSR output to the output of the module
  io.out := randomNumber
  io.outputValid := true.B
}

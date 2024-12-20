package project

import chisel3._
import chisel3.util.log2Ceil

/** Takes a value in the integral stream and returns a 0 or the integral value
  * according to the bit
  *
  * @param inputStream
  *   bipolar stochastic stream {-m, m}
  * @param inputBit
  *   a bit {0, 1}
  * @param outputStream
  *   stochastic stream {-m, 0, m}
  */
class BitwiseAND(m: Int) extends Module {
  val io = IO(new Bundle {
    val inputInteger = Input(SInt((log2Ceil(m) + 1).W))
    val inputBit = Input(UInt(1.W))
    val outputStream = Output(SInt((log2Ceil(m) + 1).W))
  })

  // Using a Mux
  io.outputStream := Mux(io.inputBit === 1.U, io.inputInteger, 0.S)

  // Using a Bitwise AND operator
  // io.outputStream := io.inputBit & io.inputInteger
}

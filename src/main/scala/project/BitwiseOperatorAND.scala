package project

import chisel3._
import chisel3.util.random.LFSR

/** Takes a value in the integral stream and returns a 0 or the integral value
  * according to the bit
  *
  * @param inputStream
  *   the pixel {-m, m}
  * @param inputBit
  *   a bit {0, 1}
  * @param outputStream
  *   the unipolar value {-m, 0, m}
  */
class BitwiseOperatorAND extends Module {
  val io = IO(new Bundle {
    val inputInteger = Input(UInt(8.W))
    val inputBit = Input(UInt(1.W))
    val outputStream = Output(UInt(8.W))
  })

  // Using a Mux
  // io.outputStream := Mux(io.inputBit === 1.U, io.inputInteger, 0.U)

  // Using a Bitwise AND operator
  io.outputStream := io.inputBit & io.inputInteger
}

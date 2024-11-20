package lab3

import chisel3._
import chisel3.util.random.LFSR

/** Converts a binary into a unipolar probability.
  * @param inputStream
  *   the pixel [0, 255]
  * @param outputStream
  *   the unipolar value {0, 1}
  */
class FixedPointAddition extends Module {
  val io = IO(new Bundle {
    val in1 = Input(UInt(8.W))
    val in2 = Input(UInt(8.W))
    val out = Output(UInt(16.W))
  })
  io.out = 
}

package project

import chisel3._
import chisel3.util.random.LFSR

/** Converts a binary into a unipolar probability.
  * @param inputStream
  *   the pixel [0, 255]
  * @param outputStream
  *   the unipolar value {0, 1}
  */
class B2SUnipolar extends Module {
  val io = IO(new Bundle {
    val inputPixel = Input(UInt(8.W))
    val outputStream = Output(UInt(1.W))
  })
  val randomNumber: UInt = LFSR(8, true.B, Some(34))

  when(randomNumber < io.inputPixel) {
    io.outputStream := 1.U
  }.otherwise {
    io.outputStream := 0.U
  }
}

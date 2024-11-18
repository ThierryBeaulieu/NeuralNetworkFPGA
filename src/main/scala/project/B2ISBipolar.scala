package project

import chisel3._
import chisel3.util.random.LFSR

/** Converts a binary into a probability.
  * @param inputStream
  *   the weight [-128, 127]
  * @param outputStream
  *   the bipolar value {-1, 1}
  */
class B2ISBipolar extends Module {
  val io = IO(new Bundle {
    val inputComparator = Input(UInt(8.W))
    val outputStream = Output(UInt(1.W))
  })
  val randomNumber: UInt = LFSR(8, true.B, Some(34)) - 128.U

  when(randomNumber > io.inputComparator) {
    io.outputStream := 1.U
  }.otherwise {
    io.outputStream := -1.U
  }
}
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
    val inputWeight = Input(SInt(8.W))
    val outputStream = Output(SInt(2.W))
  })
  val randomNumber: SInt = (LFSR(8, true.B, Some(34)).asUInt - 128.U).asSInt

  when(randomNumber < io.inputWeight) {
    io.outputStream := 1.S
  }.otherwise {
    io.outputStream := -1.S
  }
}

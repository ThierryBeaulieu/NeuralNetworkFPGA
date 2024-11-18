package project

import chisel3._
import project.B2SUnipolar

/** Converts a binary into a probability.
  * @param inputStream
  *   the weight [-128, 127]
  * @param outputStream
  *   the bipolar value {-1, 1}
  */
class B2ISBipolar extends Module {
  val io = IO(new Bundle {
    val inputStream = Input(UInt(10.W))
    val outputStream = Output(UInt(1.W))
  })

  val b2S: B2SUnipolar = new B2SUnipolar()

}

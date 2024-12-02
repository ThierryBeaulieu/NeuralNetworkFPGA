package project

import chisel3._
import chisel3.util.random.GaloisLFSR

/** Converts a binary into a unipolar probability.
  * @param inputStream
  *   the pixel [0, 255]
  * @param outputStream
  *   the unipolar value {0, 1}
  */
class B2SUnipolar(seed: Int) extends Module {
  val io = IO(new Bundle {
    val inputValue = Input(UInt(8.W))
    val outputStream = Output(UInt(1.W))
  })

  seed + 1

  val randomNumber: UInt = GaloisLFSR.maxPeriod(8)

  when(randomNumber < io.inputValue) {
    io.outputStream := 1.U
  }.otherwise {
    io.outputStream := 0.U
  }
}

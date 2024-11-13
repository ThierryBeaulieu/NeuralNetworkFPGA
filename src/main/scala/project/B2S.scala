package project

import chisel3._
import chisel3.util.random.LFSR

class B2S extends Module {
  val io = IO(new Bundle {
    val inputComparator = Input(UInt(10.W)) // max(weight) = 1024
    val outputStream = Output(UInt(1024.W)) //
  })
  val randomNumber: UInt = LFSR(10, true.B, Some(34))

  when(randomNumber > io.inputComparator) {
    io.outputStream := 1.U
  }.otherwise {
    io.outputStream := 0.U
  }
}

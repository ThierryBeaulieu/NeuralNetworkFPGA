package lab3

import chisel3._

class TwoComplement extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })

  // io.out := (~io.in).asSInt + 1.S
  io.out := ~io.in + 1.U
}

package lab3

import chisel3._

class FixedPointAddition extends Module {
  val io = IO(new Bundle {
    val in1 = Input(SInt(8.W))
    val in2 = Input(SInt(8.W))
    val out = Output(SInt(9.W))
  })
  io.out := io.in1 + io.in2
}

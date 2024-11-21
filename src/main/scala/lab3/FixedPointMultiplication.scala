package lab3

import chisel3._

class FixedPointMultiplication extends Module {
  val io = IO(new Bundle {
    val in1 = Input(SInt(8.W))
    val in2 = Input(SInt(8.W))
    val out = Output(SInt(18.W))
  })
  io.out := (io.in1).asSInt * (io.in2).asSInt
}

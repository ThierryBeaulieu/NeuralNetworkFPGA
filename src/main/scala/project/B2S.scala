package project

import chisel3._
import chisel3.util.random.LFSR

class B2S extends Module {
  val io = IO(new Bundle {
    val inputComparator = Input(UInt(10.W)) // max(weight) = 1024
    val outputStream = Output(UInt(1.W)) // random bit generated
  })
  val randomNumber: UInt = LFSR(10, true.B, Some(34))

  when(randomNumber > io.inputComparator) {
    io.outputStream := 1.U
  }.otherwise {
    io.outputStream := 0.U
  }
}

class B2SWrapper extends Module {
  val io = IO(new Bundle {
    val inputComparator = Input(UInt(10.W)) // max(weight) = 1024
    val outputStream = Output(UInt(1024.W)) // length of 1024 for precision
    val outputValid = Output(Bool())
  })
  val b2sModule = Module(new B2S())
  b2sModule.io.inputComparator := io.inputComparator
  val counter = RegInit(0.U(10.W))
  val maxSize = RegInit(1024.U(11.W))
  val stream = RegInit(0.U(1024.W))

  b2sModule.io.inputComparator := io.inputComparator
  stream(counter) := b2sModule.io.outputStream
  counter := counter + 1.U

  io.outputStream := counter

  when(counter === maxSize - 1.U) {
    io.outputValid := true.B
  }.otherwise {
    io.outputValid := false.B
  }
}

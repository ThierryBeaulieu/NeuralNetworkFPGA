package project

import chisel3._

/** Linear-Feedback shift register. Returns a random generated number.
  */
class LFSR extends Module {
  val io = IO(new Bundle {
    val inputSeed = Input(UInt(8.W)) // change size here
    val outputRandomNumber = Output(UInt(8.W))
  })

  val regSeed = RegInit(0.U(8.W))
  regSeed := io.inputSeed

  val taps = RegInit(VecInit(7.U(8.W), 5.U(8.W), 3.U(8.W), 1.U(8.W)))
  val maxBit = RegInit(8.U(8.W))
  maxBit := io.inputSeed.getWidth.U

  io.outputRandomNumber := 8.U

  def nextBif(): Unit = {}

  def next_number(bits: UInt): UInt = {
    8.U
  }
}

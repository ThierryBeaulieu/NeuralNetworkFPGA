package project

import chisel3._

/** Linear-Feedback shift register. Returns a random generated number.
  */
class LFSR extends Module {
  val io = IO(new Bundle {
    val inputSeed = Input(UInt(8.W)) // change size here
    val inputTap = Input(Vec(4, UInt(8.W)))
    val outputRandomNumber = Output(UInt(8.W))
  })
// Exemple of regSeed = "b10101010".U

  val regSeed = RegInit(0.U(8.W))
  regSeed := io.inputSeed

  val taps = Reg(
    Vec(4, UInt(8.W))
  )
  // pas certain que ce soit nécessaire, si ce sont des wires, alors les wires vont être là de manière constante
  taps := io.inputTap

  val maxBit = RegInit(8.U(8.W))
  maxBit := io.inputSeed.getWidth.U

  io.outputRandomNumber := 8.U

}

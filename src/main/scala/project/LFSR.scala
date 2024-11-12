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

  def nextBit(): UInt = {
    var newBit = 0.U(8.W)
    for (tap <- taps) {
      newBit = newBit ^ (regSeed >> (tap - 1.U)) & 1.U
    }

    // Shift left and add the new bit to the LSB
    regSeed := ((regSeed << 1.U) | newBit) & ((1.U << maxBit) - 1.U)
    newBit
  }

  def nextNumber(): UInt = {
    var number = 0.U(8.W)
    for (i <- 0 until 8) {
      number = (number << 1.U) | nextBit()
    }
    number
  }

  io.outputRandomNumber := nextNumber()
}

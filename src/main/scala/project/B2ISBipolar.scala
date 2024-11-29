package project

import chisel3._

/** Converts a binary into a probability.
  * @param inputStream
  *   the weight [-128, 127]
  * @param outputStream
  *   the bipolar value {-1, 1}
  */
class B2ISBipolar(m: Int) extends Module {
  val io = IO(new Bundle {
    val inputWeight = Input(SInt(8.W))
    val outputStream = Output(SInt(3.W))
    val outputVal = Output(SInt(8.W))
  })

  val x = ((128.S * io.inputWeight) / m.S) + 128.S
  io.outputVal := x

  val randomSeeds = Seq.fill(m)(scala.util.Random.nextInt(256))
  val b2SUnipolar = randomSeeds.map(seed => Module(new B2SUnipolar(seed)))

  for (i <- 0 until m) {
    b2SUnipolar(i).io.inputValue := x.asUInt
  }

  val outputs = b2SUnipolar.map(_.io.outputStream)

  io.outputStream := ((2.S * outputs.reduce(_ +& _)) - m.S)
}

package project

import chisel3._

/** Single Neuron from a neural network
  *
  * @param inputPixels
  *   pixel with a value of [0, 255]
  * @param inputWeights
  *   weight with a value of [-128, 127]
  * @param outputB2SValues
  *   unipolar stream {0, 1}
  * @param outputB2ISValues
  *   bipolar stream {-m, m}
  * @param outputANDValues
  *   bipolar stream {-m, 0, m}
  * @param outputTreeAdder
  *   bipolar stream {-(m*m'),...,+(m+m')}
  * @param outputStream
  *   unipolar stream {0, 1}
  */
class Neuron(nbPixels: Int, m: Int) extends Module {
  val randomSeeds = Seq.fill(nbPixels)(scala.util.Random.nextInt(255) + 1)

  private val b2SUnipolar =
    randomSeeds.map(seed => Module(new B2SUnipolar(seed)))
  private val b2ISBipolar =
    Seq.fill(nbPixels)(Module(new B2ISBipolar(m)))
  private val bitwiseAND = Seq.fill(nbPixels)(Module(new BitwiseAND(m)))
  private val treeAdder = Module(new TreeAdder(nbPixels = nbPixels))

  val io = IO(new Bundle {
    val inputPixels = Input(Vec(nbPixels, UInt(8.W)))
    val inputWeights = Input(Vec(nbPixels, SInt(8.W)))

    val outputB2is = Input(Vec())
    val outputTreeAdder = Output(SInt((16).W))
  })

  // Step 1. Pixel Unipolar Conversion
  val regB2S = RegInit(VecInit(Seq.fill(nbPixels)(0.U(1.W))))
  for (i <- 0 until regB2S.length) {
    b2SUnipolar(i).io.inputValue := io.inputPixels(i)
    regB2S(i) := b2SUnipolar(i).io.outputStream
  }

  // Step 2. Weight Bipolar Conversion
  val regB2IS = RegInit(VecInit(Seq.fill(nbPixels)(0.S(9.W))))
  for (i <- 0 until regB2IS.length) {
    b2ISBipolar(i).io.inputWeight := io.inputWeights(i)
    regB2IS(i) := b2ISBipolar(i).io.outputStream
  }

  // Step 3. Pixel & Weight
  val regAND = RegInit(VecInit(Seq.fill(nbPixels)(0.S(9.W))))
  for (i <- 0 until regAND.length) {
    bitwiseAND(i).io.inputInteger := regB2IS(i)
    bitwiseAND(i).io.inputBit := regB2S(i)
    regAND(i) := bitwiseAND(i).io.outputStream
  }

  // Step 4. TreeAdder All Streams
  treeAdder.io.inputStream := regAND
  io.outputTreeAdder := treeAdder.io.outputStream
}

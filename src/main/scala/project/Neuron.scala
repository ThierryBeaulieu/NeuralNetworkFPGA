package project

import chisel3._
import chisel3.util.log2Ceil

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

    // debugging purposes
    val outputB2SValues = Output(Vec(nbPixels, UInt(1.W)))
    val outputB2ISValues = Output(Vec(nbPixels, SInt(9.W)))
    val outputANDValues = Output(Vec(nbPixels, SInt(9.W)))
    val outputTreeAdder = Output(SInt((9 + log2Ceil(nbPixels)).W))
    // end of debugging purposes
  })

  // Step 1. Pixel Unipolar Conversion
  for (i <- 0 until nbPixels) {
    b2SUnipolar(i).io.inputValue := io.inputPixels(i)
    // debugging
    io.outputB2SValues(i) := b2SUnipolar(i).io.outputStream
  }

  // Step 2. Weight Bipolar Conversion
  for (i <- 0 until nbPixels) {
    b2ISBipolar(i).io.inputWeight := io.inputWeights(i)
    // debugging
    io.outputB2ISValues(i) := b2ISBipolar(i).io.outputStream
  }

  // Step 3. Pixel & Weight
  for (i <- 0 until nbPixels) {
    bitwiseAND(i).io.inputInteger := b2ISBipolar(i).io.outputStream
    bitwiseAND(i).io.inputBit := b2SUnipolar(i).io.outputStream
    // debugging
    io.outputANDValues(i) := bitwiseAND(i).io.outputStream
  }

  // Step 4. TreeAdder All Streams
  treeAdder.io.inputStream := bitwiseAND.map(_.io.outputStream)

  // debugging
  io.outputTreeAdder := treeAdder.io.outputStream
}

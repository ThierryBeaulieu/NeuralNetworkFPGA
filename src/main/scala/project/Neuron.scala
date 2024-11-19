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
  *   bipolar stream {-1, 1}
  * @param outputStream
  *   unipolar stream {0, 1}
  */
class Neuron(nbData: Int) extends Module {
  private val b2SUnipolar = Seq.fill(nbData)(Module(new B2SUnipolar))
  private val b2ISBipolar = Seq.fill(nbData)(Module(new B2ISBipolar))
  // private val treeAdder = Module(new TreeAdder(nbStream = nbData))
  // private val nStanh = Module(new NStanh(offset = 2.S, mn = 6.S))

  val io = IO(new Bundle {
    val inputPixels = Input(Vec(nbData, UInt(8.W)))
    val inputWeights = Input(Vec(nbData, SInt(8.W)))
    val outputB2SValues = Output(Vec(nbData, UInt(1.W)))
    val outputB2ISValues = Output(Vec(nbData, SInt(2.W)))
    val outputStream = Output(Vec(nbData, UInt(1.W)))
  })

  // Step 1. Pixel Unipolar Conversion
  val regB2S = RegInit(VecInit(Seq.fill(nbData)(0.U(1.W))))
  for (i <- 0 until regB2S.length) {
    b2SUnipolar(i).io.inputPixel := io.inputPixels(i)
    regB2S(i) := b2SUnipolar(i).io.outputStream
    io.outputB2SValues(i) := regB2S(i)
    io.outputStream(i) := regB2S(i) // todo assign last value at the end
  }

  // Step 2. Weight Bipolar Conversion
  val regB2IS = RegInit(VecInit(Seq.fill(nbData)(0.S(2.W))))
  for (i <- 0 until regB2IS.length) {
    b2ISBipolar(i).io.inputWeight := io.inputWeights(i)
    regB2IS(i) := b2ISBipolar(i).io.outputStream
    io.outputB2ISValues(i) := regB2IS(i)
  }

  // Step 3. Pixel & Weight

  // Step 4. TreeAdder All Streams

  // Step 5. Passing Stream to NStanh

}

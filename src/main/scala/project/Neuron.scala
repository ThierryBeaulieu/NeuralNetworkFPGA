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
class Neuron(nbData: Int) extends Module {
  private val b2SUnipolar = Seq.fill(nbData)(Module(new B2SUnipolar))
  private val b2ISBipolar = Seq.fill(nbData)(Module(new B2ISBipolar))
  private val bitwiseAND = Seq.fill(nbData)(Module(new BitwiseAND))
  private val treeAdder = Module(new TreeAdder(nbStream = nbData))
  private val nStanh = Module(new NStanh(n = 4, m = nbData))

  val io = IO(new Bundle {
    val inputPixels = Input(Vec(nbData, UInt(8.W)))
    val inputWeights = Input(Vec(nbData, SInt(8.W)))
    
    val outputB2SValues = Output(Vec(nbData, UInt(1.W)))
    val outputB2ISValues = Output(Vec(nbData, SInt(2.W)))
    val outputANDValues = Output(Vec(nbData, SInt(2.W)))
    val outputTreeAdder = Output(SInt((nbData + 1).W))

    // end of debugging purposes
    val outputStream = Output(UInt(1.W))
  })

  // Step 1. Pixel Unipolar Conversion
  val regB2S = RegInit(VecInit(Seq.fill(nbData)(0.U(1.W))))
  for (i <- 0 until regB2S.length) {
    b2SUnipolar(i).io.inputPixel := io.inputPixels(i)
    regB2S(i) := b2SUnipolar(i).io.outputStream
    io.outputB2SValues(i) := regB2S(i)
  }

  // Step 2. Weight Bipolar Conversion
  val regB2IS = RegInit(VecInit(Seq.fill(nbData)(0.S(2.W))))
  for (i <- 0 until regB2IS.length) {
    b2ISBipolar(i).io.inputWeight := io.inputWeights(i)
    regB2IS(i) := b2ISBipolar(i).io.outputStream
    io.outputB2ISValues(i) := regB2IS(i)
  }

  // Step 3. Pixel & Weight
  val regAND = RegInit(VecInit(Seq.fill(nbData)(0.S(2.W))))
  for (i <- 0 until regAND.length) {
    bitwiseAND(i).io.inputInteger := regB2IS(i)
    bitwiseAND(i).io.inputBit := regB2S(i)
    regAND(i) := bitwiseAND(i).io.outputStream
    io.outputANDValues(i) := regAND(i)
  }

  // Step 4. TreeAdder All Streams
  treeAdder.io.inputStream := regAND
  io.outputTreeAdder := treeAdder.io.outputStream

  // Step 5. Passing Stream to NStanh
  nStanh.io.inputSi := treeAdder.io.outputStream
  io.outputStream := nStanh.io.outputStream
}

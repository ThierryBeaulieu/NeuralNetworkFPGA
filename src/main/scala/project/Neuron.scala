package project

import chisel3._

/** Single Neuron from a neural network
  *
  * @param inputPixels
  *   pixel with a value of [0, 255]
  * @param inputWeights
  *   weight with a value of [-128, 127]
  * @param outputB2SValues
  *   bipolar stream {0, 1}
  * @param outputStream
  *   bipolar stream {0, 1}
  */
class Neuron(nbData: Int) extends Module {
  private val b2SUnipolar = Module(new B2SUnipolar())
  // private val b2ISBipolar = Module(new B2ISBipolar())
  // private val treeAdder = Module(new TreeAdder(nbStream = nbData))
  // private val nStanh = Module(new NStanh(offset = 2.S, mn = 6.S))

  val io = IO(new Bundle {
    val inputPixels = Input(Vec(nbData, UInt(8.W)))
    val inputWeights = Input(Vec(nbData, SInt(8.W)))
    val outputB2SValues = Output(Vec(nbData, UInt(1.W)))
    val outputStream = Output(Vec(nbData, UInt(1.W)))
  })

  // Step 1. Pixel Unipolar Conversion
  val regStochastic = RegInit(VecInit(Seq.fill(nbData)(0.U(1.W))))
  b2SUnipolar.io.inputPixel := io.inputPixels(0)
  regStochastic(0) := b2SUnipolar.io.outputStream
  io.outputB2SValues(0) := regStochastic(0)
  io.outputStream(0) := regStochastic(0)

  // Step 2. Weight Bipolar Conversion

  // Step 3. Pixel & Weight

  // Step 4. TreeAdder All Streams

  // Step 5. Passing Stream to NStanh

}

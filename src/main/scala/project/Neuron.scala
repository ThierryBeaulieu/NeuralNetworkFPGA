package project

import chisel3._

/** Single Neuron from a neural network
  *
  * @param inputPixels
  *   pixel with a value of [0, 255]
  * @param inputWeights
  *   weight with a value of [-128, 127]
  * @param outputStream
  *   bipolar stream {0, 1}
  */
class Neuron(nbData: Int) extends Module {
  private val b2SUnipolar = new B2SUnipolar()
  private val b2ISBipolar = new B2ISBipolar()
  private val treeAdder = new TreeAdder(nbStream = nbData)
  private val nStanh = new NStanh(offset = 2.S, mn = 6.S)

  val io = IO(new Bundle {
    val inputPixels = Input(Vec(nbData, UInt(8.W)))
    val inputWeights = Input(Vec(nbData, SInt(8.W)))
    val outputStream = Output(UInt(1.W))
  })

  // Step 1. Pixel Unipolar Conversion
  val regStochastic = RegInit(VecInit(Seq.fill(nbData)(0.U(8.W))))
  for (i <- 0 until io.inputPixels.length) {
    b2SUnipolar.io.inputPixel := io.inputPixels(i)
    regStochastic(i) := b2SUnipolar.io.outputStream
  }

  // Step 2. Weight Bipolar Conversion

  // Step 3. Pixel & Weight

  // Step 4. TreeAdder All Streams

  // Step 5. Passing Stream to NStanh

}

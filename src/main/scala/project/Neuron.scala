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
  val io = IO(new Bundle {
    val inputPixels = Input(Vec(nbData, UInt(8.W)))
    val inputWeights = Input(Vec(nbData, SInt(8.W)))
    val outputStream = Output(UInt(1.W))
  })

  // Step 1. Pixel Unipolar Conversion

  // Step 2. Weight Bipolar Conversion

  // Step 3. Pixel & Weight

  // Step 4. TreeAdder All Streams

  // Step 5. Passing Stream to NStanh

}

package project

import chisel3._
import _root_.circt.stage.ChiselStage
import chisel3.util._

/** NeuronWrapper
  *
  * @param slaveIO
  *   AxiStreamSlaveIf
  * @param masterIO
  *   AxiStreamExternalIf
  */
class NeuronWrapper extends Module {
  val sAxis1 = Wire(new AxiStreamSlaveIf(8))
  val slaveIO1 =
    IO(new AxiStreamExternalIf(8))
  slaveIO1.suggestName("s_axis1").connect(sAxis1)

  val sAxis2 = Wire(new AxiStreamSlaveIf(8))
  val slaveIO2 =
    IO(new AxiStreamExternalIf(8))
  slaveIO2.suggestName("s_axis2").connect(sAxis2)

  val mAxis = Wire(new AxiStreamMasterIf(16))
  val masterIO = IO(Flipped(new AxiStreamExternalIf(16)))
  masterIO
    .suggestName("m_axis")
    .connect(mAxis)

  val neuron = Module(new Neuron(8))

  val io = IO(new Bundle {
    val outputB2SValues = Output(Vec(8, UInt(1.W)))
    val outputB2ISValues = Output(Vec(8, SInt(2.W)))
    val outputANDValues = Output(Vec(8, SInt(2.W)))
    val outputTreeAdder = Output(SInt((8 + 1).W))
    val outputStream = Output(UInt(1.W))

    val image = Output(Vec(8, UInt(8.W)))
    val weights = Output(Vec(8, SInt(8.W)))
  })

  for (i <- 0 until 8) {
    io.outputB2SValues(i) := 0.U
    io.outputB2ISValues(i) := 0.S
    io.outputANDValues(i) := 0.S
    io.image(i) := 0.U
    io.weights(i) := 0.S
  }

  io.outputTreeAdder := 0.S
  io.outputStream := 0.U

  sAxis1.tready := RegInit(true.B)
  sAxis2.tready := RegInit(true.B)
  mAxis.data.tvalid := RegInit(false.B)
  mAxis.data.tlast := RegInit(false.B)
  mAxis.data.tdata := RegInit(0.U(16.W))
  mAxis.data.tkeep := RegInit("b1".U)

  object State extends ChiselEnum {
    val receiving, handling, sending = Value
  }

  val state = RegInit(State.receiving)

  val image = RegInit(VecInit(Seq.fill(8)(0.U(8.W))))
  val weights = RegInit(VecInit(Seq.fill(8)(0.S(8.W))))

  val indexImage = RegInit(0.U(3.W))
  val indexWeight = RegInit(0.U(3.W))

  val counter = RegInit(0.U(16.W))

  val minCycles = RegInit(0.U(10.W))

  def setImageAndWeight() = {
    neuron.io.inputPixels := image
    neuron.io.inputWeights := weights
  }
  setImageAndWeight()

  val weightReady = RegInit(false.B)
  val imageReady = RegInit(false.B)

  switch(state) {
    // Step 1. Fill the image with 401 pixels
    is(State.receiving) {
      when(sAxis1.data.tvalid) {
        image(indexImage) := sAxis1.data.tdata
        indexImage := indexImage + 1.U
        when(sAxis1.data.tlast) {
          imageReady := true.B
          sAxis1.tready := false.B
        }
      }
      when(sAxis2.data.tvalid) {
        weights(indexWeight) := (sAxis1.data.tdata).asSInt
        indexWeight := indexWeight + 1.U
        when(sAxis1.data.tlast) {
          weightReady := true.B
          sAxis1.tready := false.B
        }
      }
      when(imageReady === true.B && weightReady === true.B) {
        state := State.handling
      }
    }
    // Step 2. Process the information for 1024 cycles
    is(State.handling) {
      io.outputB2SValues := neuron.io.outputB2SValues
      io.outputB2ISValues := neuron.io.outputB2ISValues
      io.outputANDValues := neuron.io.outputANDValues
      io.outputTreeAdder := neuron.io.outputTreeAdder
      io.outputStream := neuron.io.outputStream
      io.image := image
      io.weights := weights

      counter := counter + neuron.io.outputStream
      minCycles := (minCycles + 1.U)
      when(minCycles === (1024.U - 1.U)) {
        state := State.sending
      }
    }
    // State 3. Return the information
    is(State.sending) {
      when(mAxis.tready) {
        mAxis.data.tlast := true.B
        mAxis.data.tvalid := true.B
        mAxis.data.tdata := counter
        // reinitialize everything
        image := VecInit(Seq.fill(8)(0.U(8.W)))
        imageReady := false.B
        weightReady := false.B
        indexImage := 0.U
        indexWeight := 0.U
        counter := RegInit(0.U(16.W))

        minCycles := 0.U
        state := State.receiving
      }
    }
  }
}

object NeuronWrapper extends App {
  ChiselStage.emitSystemVerilogFile(
    new NeuronWrapper,
    args = Array(
      "--target-dir",
      "generated/project/"
    ),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info"
    )
  )
}

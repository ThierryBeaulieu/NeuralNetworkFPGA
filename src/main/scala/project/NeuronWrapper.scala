package project

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil
import _root_.circt.stage.ChiselStage
import scala.io.Source

/** NeuronWrapper
  *
  * @param slaveIO
  *   AxiStreamSlaveIf
  * @param masterIO
  *   AxiStreamExternalIf
  */
class NeuronWrapper(nbData: Int, m: Int, csvSelected: String) extends Module {
  val sAxis = Wire(new AxiStreamSlaveIf(16))
  val slaveIO =
    IO(new AxiStreamExternalIf(16))
  slaveIO.suggestName("s_axis").connect(sAxis)

  val mAxis = Wire(new AxiStreamMasterIf(16))
  val masterIO = IO(Flipped(new AxiStreamExternalIf(16)))
  masterIO
    .suggestName("m_axis")
    .connect(mAxis)

  val neuron = Module(new Neuron(nbData, m))
  private val weightsCSV = readCSV(csvSelected)
  val weights = RegInit(
    VecInit.tabulate(1, nbData) { (x, y) =>
      weightsCSV(x)(y).S(8.W)
    }
  )

  val io = IO(new Bundle {
    val outputB2SValues = Output(Vec(nbData, UInt(1.W)))
    val outputB2ISValues = Output(Vec(nbData, SInt(9.W)))
    val outputANDValues = Output(Vec(nbData, SInt(9.W)))
    val outputTreeAdder = Output(SInt((9 + log2Ceil(nbData)).W))
    val outputStream = Output(UInt(1.W))
  })

  for (i <- 0 until 8) {
    io.outputB2SValues(i) := 0.U
    io.outputB2ISValues(i) := 0.S
    io.outputANDValues(i) := 0.S
  }
  io.outputTreeAdder := 0.S
  io.outputStream := 0.U

  def readCSV(filePath: String): Array[Array[Int]] = {
    val source = Source.fromResource(filePath)
    val data = source
      .getLines()
      .map { line =>
        line.split(",").map(_.trim.toInt)
      }
      .toArray
    source.close()
    data
  }

  sAxis.tready := RegInit(true.B)
  mAxis.data.tvalid := RegInit(false.B)
  mAxis.data.tlast := RegInit(false.B)
  mAxis.data.tdata := RegInit(0.S(16.W))
  mAxis.data.tkeep := RegInit("b11".U)

  object State extends ChiselEnum {
    val receiving, handling, sending = Value
  }

  val state = RegInit(State.receiving)

  val image = RegInit(VecInit(Seq.fill(nbData)(0.U(8.W))))
  val index = RegInit(0.U(3.W))

  def setImageAndWeight() = {
    neuron.io.inputPixels := image
    neuron.io.inputWeights := weights(0)
  }
  setImageAndWeight()

  switch(state) {
    // Step 1. Fill the image with 401 pixels
    is(State.receiving) {
      when(sAxis.data.tvalid) {
        image(index) := sAxis.data.tdata.asUInt(7, 0)
        index := index + 1.U
        when(sAxis.data.tlast) {
          state := State.handling
          sAxis.tready := false.B
        }
      }
    }
    // Step 2. Process the information for 1024 cycles
    is(State.handling) {
      io.outputB2SValues := neuron.io.outputB2SValues
      io.outputB2ISValues := neuron.io.outputB2ISValues
      io.outputANDValues := neuron.io.outputANDValues
      io.outputTreeAdder := neuron.io.outputTreeAdder
      io.outputStream := neuron.io.outputStream

      state := State.sending
    }
    // State 3. Return the information
    is(State.sending) {
      when(mAxis.tready) {
        mAxis.data.tlast := true.B
        mAxis.data.tvalid := true.B
        mAxis.data.tdata := neuron.io.outputTreeAdder
        // reinitialize everything
        image := VecInit(Seq.fill(8)(0.U(8.W)))
        index := 0.U

        state := State.receiving
      }
    }
  }
}

object NeuronWrapper extends App {
  ChiselStage.emitSystemVerilogFile(
    new NeuronWrapper(401, 128, "weights.csv"),
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

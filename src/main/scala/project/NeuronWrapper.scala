package project

import chisel3._
import scala.io.Source
import chisel3.util._

/** NeuronWrapper
  *
  * @param slaveIO
  *   AxiStreamSlaveIf
  * @param masterIO
  *   AxiStreamExternalIf
  */
class NeuronWrapper extends Module {

  val io = IO(new Bundle {
    val outputState = Output(UInt(2.W))

    // debugging
    val outputStream = Output(UInt(1.W))
  })

  io.outputState := 0.U
  io.outputStream := 0.U

  val neuron = Module(new Neuron(8))
  private val weightsCSV = readCSV("weights.csv")
  val weights = RegInit(
    VecInit.tabulate(1, 8) { (x, y) =>
      weightsCSV(x)(y).S(8.W)
    }
  )

  val sAxis = Wire(new AxiStreamSlaveIf(8))
  val slaveIO =
    IO(new AxiStreamExternalIf(8))
  slaveIO.suggestName("s_axis").connect(sAxis)

  val mAxis = Wire(new AxiStreamMasterIf(10))
  val masterIO = IO(Flipped(new AxiStreamExternalIf(10)))
  masterIO
    .suggestName("m_axis")
    .connect(mAxis)

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
  mAxis.data.tdata := RegInit(0.U(10.W))
  mAxis.data.tkeep := RegInit("b1".U)

  object State extends ChiselEnum {
    val receiving, handling, sending = Value
  }

  val state = RegInit(State.receiving)

  val image = RegInit(VecInit(Seq.fill(8)(0.U(8.W))))
  val index = RegInit(0.U(3.W))

  val counter = RegInit(0.U(10.W))
  val transferCount = RegInit(0.U(4.W))

  val minCycles = RegInit(0.U(10.W))
  val dataSent = RegInit(false.B)

  def setImageAndWeight() = {
    neuron.io.inputPixels := image
    neuron.io.inputWeights := weights(0)
  }
  setImageAndWeight()

  switch(state) {
    // Step 1. Fill the image with 401 pixels
    is(State.receiving) {
      io.outputState := 1.U
      when(sAxis.data.tvalid) {
        io.outputState := 5.U
        image(index) := sAxis.data.tdata
        index := index + 1.U
        when(sAxis.data.tlast) {
          state := State.handling
          sAxis.tready := false.B
        }
      }
    }
    // Step 2. Process the information for 1024 cycles
    is(State.handling) {
      io.outputState := 2.U

      counter := counter + neuron.io.outputStream

      minCycles := (minCycles + 1.U)
      when(minCycles === (1024.U - 1.U)) {
        state := State.sending
      }
    }
    // State 3. Return the information
    is(State.sending) {
      io.outputState := 3.U
      when(mAxis.tready) {
        when(dataSent) {
          mAxis.data.tlast := true.B
          mAxis.data.tvalid := false.B

          // reinitialize everything
          image := VecInit(Seq.fill(8)(0.U(8.W)))
          index := 0.U
          counter := RegInit(0.U(10.W))

          minCycles := 0.U
          state := State.receiving
          transferCount := 0.U
        }.otherwise {
          mAxis.data.tlast := false.B
          mAxis.data.tvalid := true.B
          mAxis.data.tdata := counter
          dataSent := true.B
        }
      }
    }
  }
}

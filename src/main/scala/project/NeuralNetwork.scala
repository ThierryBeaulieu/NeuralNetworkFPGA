package project

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source
import chisel3.util._

/** Neural Network Composed of 10 Neurons
  *
  * @param slaveIO
  *   AxiStreamSlaveIf
  * @param masterIO
  *   AxiStreamExternalIf
  */
class NeuralNetwork extends Module {

  val io = IO(new Bundle {
    val outputState = Output(UInt(2.W))

    // debugging
    val outputStream = Output(UInt(1.W))
  })

  io.outputState := 0.U
  io.outputStream := 0.U

  val neurons = Seq.fill(10)(Module(new Neuron(401)))
  private val weightsCSV = readCSV("weights.csv")
  val weights = RegInit(
    VecInit.tabulate(10, 401) { (x, y) =>
      weightsCSV(x)(y).S(8.W)
    }
  )

  val sAxis = Wire(new AxiStreamSlaveIf(8))
  val slaveIO =
    IO(new AxiStreamExternalIf(8))
  slaveIO.suggestName("s_axis").connect(sAxis)

  val mAxis = Wire(new AxiStreamMasterIf(8))
  val masterIO = IO(Flipped(new AxiStreamExternalIf(8)))
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
  mAxis.data.tdata := RegInit(0.U(8.W))
  mAxis.data.tkeep := RegInit("b1".U)

  object State extends ChiselEnum {
    val receiving, handling, sending = Value
  }

  val state = RegInit(State.receiving)

  val image = RegInit(VecInit(Seq.fill(401)(0.U(8.W))))
  val index = RegInit(0.U(9.W))

  val counter = RegInit(VecInit(Seq.fill(10)(0.U(10.W))))
  val transferCount = RegInit(0.U(4.W))

  val minCycles = RegInit(0.U(10.W))

  def setImageAndWeight() = {
    for (i <- 0 until 10) {
      neurons(i).io.inputPixels := image
      neurons(i).io.inputWeights := weights(i)
    }
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

      io.outputStream := neurons(2).io.outputStream
      for (i <- 0 until 10) {
        counter(i) := counter(i) + neurons(i).io.outputStream
      }

      minCycles := (minCycles + 1.U)
      when(minCycles === (1024.U - 1.U)) {
        state := State.sending
      }
    }
    // State 3. Return the information
    is(State.sending) {
      io.outputState := 3.U
      when(mAxis.tready) {
        when(transferCount === (counter.length.U)) {
          mAxis.data.tlast := false.B
          mAxis.data.tvalid := false.B
          // reinitialize everything
          image := VecInit(Seq.fill(401)(0.U(8.W)))
          index := 0.U
          counter := VecInit(Seq.fill(10)(0.U(10.W)))

          minCycles := 0.U
          state := State.receiving
          transferCount := 0.U

        }.otherwise {
          when(transferCount === (counter.length.U - 1.U)) {
            mAxis.data.tlast := true.B
          }.otherwise {
            mAxis.data.tlast := false.B
          }
          mAxis.data.tvalid := true.B
          mAxis.data.tdata := counter(transferCount)
          transferCount := transferCount + 1.U
        }
      }
    }
  }
}

object NeuralNetwork extends App {
  ChiselStage.emitSystemVerilogFile(
    new NeuralNetwork,
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

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

  def setNeuronData() = {
    for (i <- 0 until 10) {
      neurons(i).io.inputPixels := image
      neurons(i).io.inputWeights := weights(i)
    }
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

  switch(state) {
    // Step 1. Fill the image with 401 pixels
    is(State.receiving) {
      when(sAxis.data.tvalid) {
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
      setNeuronData()
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
      when(mAxis.tready) {
        when(transferCount === counter.length.U) {
          mAxis.data.tlast := true.B
          mAxis.data.tvalid := false.B
          // reinitialize everything
          image := VecInit(Seq.fill(401)(0.U(8.W)))
          index := 0.U
          counter := VecInit(Seq.fill(10)(0.U(10.W)))

          minCycles := 0.U
          state := State.receiving
          transferCount := 0.U

        }.otherwise {
          mAxis.data.tlast := false.B
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

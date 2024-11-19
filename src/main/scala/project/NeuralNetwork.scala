package project

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source

/** Neural Network Composed of 10 Neurons
  *
  * @param slaveIO
  *   AxiStreamSlaveIf
  * @param masterIO
  *   AxiStreamExternalIf
  */
class NeuralNetwork extends Module {
  private val nbNeurons = 10
  private val nbPixels = 401
  private val neurons = Seq.fill(nbNeurons)(Module(new Neuron(nbPixels)))
  private val weights = RegInit(
    VecInit.tabulate(nbNeurons, nbPixels) { (x, y) =>
      readCSV("weights.csv")(x)(y).S(16.W)
    }
  )

  // AXI-Stream Connection
  val sAxis = Wire(new AxiStreamSlaveIf(16))
  val slaveIO =
    IO(new AxiStreamExternalIf(16))
  slaveIO.suggestName("s_axis").connect(sAxis)

  val mAxis = Wire(new AxiStreamMasterIf(16))
  val masterIO = IO(Flipped(new AxiStreamExternalIf(16)))
  masterIO
    .suggestName("m_axis")
    .connect(mAxis)

  /** Fetch the weights from a CSV
    *
    * @param filePath
    *   Location of the file containing the weights
    * @param data
    *   The weights Array[Array[Int]]
    */
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

  val output_data = RegInit(VecInit(Seq.fill(nbNeurons)(0.S(16.W))))
  val inputPixel = RegInit(VecInit(Seq.fill(nbPixels)(0.U(8.W))))

  val transferCount = RegInit(0.U(4.W)) // [0, 9]
  val pixelIndex = RegInit(0.U(9.W)) // [0, 400]
  val sending = RegInit(false.B)
  val processing = RegInit(false.B)
  val initialize = RegInit(false.B)

  sAxis.tready := RegInit(true.B)
  mAxis.data.tvalid := RegInit(false.B)
  mAxis.data.tlast := RegInit(false.B)
  mAxis.data.tdata := RegInit(0.S(16.W))
  mAxis.data.tkeep := RegInit("b11".U)

  when(sAxis.data.tvalid) {
    inputPixel(pixelIndex) := sAxis.data.tdata

    pixelIndex := pixelIndex + 1.U
    when(sAxis.data.tlast) {
      initialize := true.B
    }
  }

  when(initialize) {
    for (i <- 0 until nbPixels) {
      for (j <- 0 until nbNeurons) {
        neurons(j).io.inputPixels := inputPixel(i)
      }
    }
    for (i <- 0 until nbNeurons) {
      val row: Vec[SInt] = weights(i)
      for (j <- 0 until nbPixels) {
        neurons(i).io.inputWeights := row(j)
      }
    }
    initialize := false.B
    processing := true.B
  }

  val processing_cycle = RegInit(0.U(10.W))
  when(processing) {
    processing_cycle := processing_cycle + 1.U
    when(processing_cycle === 1024.U) {
      processing_cycle := 0.U
      processing := false.B
      sending := true.B
    }
  }

  when(sending && mAxis.tready) {
    when(transferCount === output_data.length.U) {
      mAxis.data.tlast := true.B
      mAxis.data.tvalid := false.B
      output_data := VecInit(Seq.fill(nbNeurons)(0.S(16.W)))
      transferCount := 0.U
      pixelIndex := 0.U
      sending := false.B
    }.otherwise {
      mAxis.data.tlast := false.B
      mAxis.data.tvalid := true.B
      mAxis.data.tdata := output_data(transferCount)
      transferCount := transferCount + 1.U
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

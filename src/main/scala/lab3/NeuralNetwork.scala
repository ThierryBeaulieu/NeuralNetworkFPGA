package lab3

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source

class NeuralNetwork extends Module {
  val io = IO(new Bundle {
    val outputTestWeight = Output(UInt(8.W))
    val layer1Value = Output(SInt(25.W))
    // val testValue = Output(UInt(8.W))
  })

  // AXI-Stream Connection
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

  val rawData = readCSV("lab3/theta_0_int8.csv")
  val weights_hidden_layer1 = RegInit(
    VecInit.tabulate(25, 401) { (x, y) =>
      rawData(x)(y).U(8.W)
    }
  )

  io.layer1Value := 0.S

  io.outputTestWeight := weights_hidden_layer1(0)(0)

  sAxis.tready := RegInit(true.B)
  mAxis.data.tvalid := RegInit(false.B)
  mAxis.data.tlast := RegInit(false.B)
  mAxis.data.tdata := RegInit(0.U(8.W))
  mAxis.data.tkeep := RegInit("b1".U)

  val image = RegInit(VecInit(Seq.fill(401)(0.S(8.W))))
  val index = RegInit(0.U(9.W))

  val sending = RegInit(false.B)
  val handling = RegInit(false.B)

  when(sAxis.data.tvalid) {
    image(index) := (sAxis.data.tdata).asSInt
    index := index + 1.U
    when(sAxis.data.tlast) {
      handling := true.B
      sAxis.tready := false.B
    }
  }

  // [2:6] * [2:6] = [4:12] [13:12]
  val layer1 = RegInit(VecInit(Seq.fill(25)(0.S(25.W))))
  val pixelIndex = RegInit(0.U(9.W))
  val row = RegInit(0.U(5.W))
  when(handling) {
    layer1(row) := (layer1(row) + weights_hidden_layer1(row)(
      pixelIndex
    ) * image(pixelIndex))

    pixelIndex := (pixelIndex + 1.U)

    // sur 25 bits, on va de [-33554432, 33554431]
    // Mais ça doit être mappé sur quelles valeurs?

    when(pixelIndex === (401.U - 1.U)) {
      row := row + 1.U
      pixelIndex := 0.U
    }

    when(row === (25.U - 1.U)) {
      sending := true.B
      handling := false.B
    }
  }

  when(sending) {
    io.layer1Value := layer1(0)
  }
}

/*
  firstHiddenLayerResult = np.zeros(25)
# print(weightsHidden1Int8.shape) # (25, 401)
for i in range(0, weightsHidden1Int8.shape[0]):
    weights = weightsHidden1Int8[i]
    sum = 0
    for j in range(0, len(weights)):
        weight = int(weights[j])
        isNegative = False
        if weight > 127:
            isNegative = True
            weight = 2**8 - weight
        pixel = imagesInt8[j]
        tmp = pixel * weight
        if isNegative and tmp != 0:
            tmp = 2**16 - tmp
        # print(f"w {weights[j]} p {imagesInt8[j]} res {tmp}")
        # tmp is 16 bits (log_2(401 * 16 bits) = 24.64) donc 25 bits
        # pour représenter
        sum = sum + tmp
    firstHiddenLayerResult[i] = sum
 */

object NeuralNetwork extends App {
  ChiselStage.emitSystemVerilogFile(
    new NeuralNetwork,
    args = Array(
      "--target-dir",
      "generated/lab3/"
    ),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info"
    )
  )
}

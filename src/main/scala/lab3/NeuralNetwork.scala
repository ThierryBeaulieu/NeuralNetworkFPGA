package lab3

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source
import chisel3.util._

object Utility {
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

}

object State extends ChiselEnum {
  val receiving, firstSigmoid, secondSigmoid, sending =
    Value
}

object NetworkHelper {
  def initializeIO(
      sAxis: AxiStreamSlaveIf,
      mAxis: AxiStreamMasterIf,
      outputWidth: Int
  ) = {
    sAxis.tready := RegInit(true.B)
    mAxis.data.tvalid := RegInit(false.B)
    mAxis.data.tlast := RegInit(false.B)
    mAxis.data.tdata := RegInit(0.S(outputWidth.W))
    mAxis.data.tkeep := RegInit("b1".U)
  }

  def connectMaster(masterIO: AxiStreamExternalIf, mAxis: AxiStreamMasterIf) = {
    masterIO
      .suggestName("m_axis")
      .connect(mAxis)
  }

  def connectSlave(slaveIO: AxiStreamExternalIf, sAxis: AxiStreamSlaveIf) = {
    slaveIO.suggestName("s_axis").connect(sAxis)
  }
}

class HiddenLayer0(theta0: Vec[Vec[SInt]], sum: Vec[SInt]) extends Module {

  def handlePixel(pixel: SInt, col: UInt) = {
    for (row <- 0 until 25) {
      sum(row) := (sum(row) + (pixel * theta0(row)(col)))
    }
    if (col == (401.U - 1.U)) {
      col := 0.U
    } else {
      col := col + 1.U
    }
  }
}

class HiddenLayer1(
    theta1: Vec[Vec[SInt]],
    result: Vec[SInt],
    sigmoidRes: Vec[SInt]
) extends Module {

  def handleDotProduct(row: UInt, col: UInt) = {
    val res = (sigmoidRes(col) * theta1(row)(col))
    result(row) := res
  }
}

class Sigmoid0(
) extends Module {
  def handleSigmoid(
      hiddenLayerRes: Vec[SInt],
      memory: SyncReadMem[SInt],
      sigmoidResult: Vec[SInt]
  ) = {
    for (i <- 0 until hiddenLayerRes.length) {
      // printf(p"${hiddenLayerRes(i)}")
      when((hiddenLayerRes(i) >> 7) > 128.S) {
        sigmoidResult(i + 1) := 1.S
      }.elsewhen((hiddenLayerRes(i) >> 7) < -128.S) {
        sigmoidResult(i + 1) := 0.S
      }.otherwise {
        // [3, 5]
        // printf(p"\n[h : ${hiddenLayerRes(i).asUInt(14, 7)}]")
        val tmp = (memory
          .read(hiddenLayerRes(i).asUInt(14, 7))) * math
          .pow(2, 5)
          .toInt
          .asSInt
        sigmoidResult(i + 1) := tmp

        // printf(p"[s : ${tmp}]")
      }
    }
  }
}

class MemoryManager extends Module {
  def initSigmoid(memory: SyncReadMem[SInt]) = {
    val baseMax = (math.pow(2, 8)).toInt
    memory.write(0.U, 1.S)
    for (i <- -(baseMax / 2) until (baseMax / 2).toInt) {
      val x: Double = i / math.pow(2, 5).toDouble
      memory.write(
        (i.S).asUInt,
        sigmoid(x)
      )
    }
  }

  def sigmoid(x: Double): SInt = {
    val result = (1 / (1 + math.exp(-x)))
    val resultSInt = (result * math.pow(2, 6)).toInt.asSInt(8.W)
    resultSInt
  }
}

class NeuralNetwork(inputWidth: Int = 8, outputWidth: Int = 8) extends Module {
  val io = IO(new Bundle {
    val slaveIO = new AxiStreamExternalIf(inputWidth)
    val masterIO = Flipped(new AxiStreamExternalIf(inputWidth))
  })

  val sAxis: AxiStreamSlaveIf = Wire(new AxiStreamSlaveIf(outputWidth))
  val mAxis: AxiStreamMasterIf = Wire(new AxiStreamMasterIf(outputWidth))

  NetworkHelper.connectMaster(io.masterIO, mAxis)
  NetworkHelper.connectSlave(io.slaveIO, sAxis)
  NetworkHelper.initializeIO(sAxis, mAxis, outputWidth)

  val theta0_Int8_csv = Utility.readCSV("lab3/theta0_Int8.csv")
  val theta0: Vec[Vec[SInt]] = RegInit(VecInit.tabulate(25, 401) { (x, y) =>
    theta0_Int8_csv(x)(y).S(8.W)
  })

  val theta1_Int8_csv = Utility.readCSV("lab3/theta1_Int8.csv")
  val theta1: Vec[Vec[SInt]] = RegInit(VecInit.tabulate(10, 26) { (x, y) =>
    theta1_Int8_csv(x)(y).S(8.W)
  })

  // HiddenLayer1
  val state = RegInit(State.receiving)
  val hiddenLayer0_result: Vec[SInt] = RegInit(VecInit(Seq.fill(25)(0.S(32.W))))
  val sigmoid0_result: Vec[SInt] = RegInit(VecInit(Seq.fill(26)(0.S(8.W))))
  sigmoid0_result(0) := (1 * math.pow(2, 6)).toInt.asSInt
  var col = RegInit(0.U(9.W))
  val hiddenLayer0 = Module(new HiddenLayer0(theta0, hiddenLayer0_result))
  val sigmoidMemory = SyncReadMem((math.pow(2, 8)).toInt, SInt(8.W))
  val sigmoid0 = Module(
    new Sigmoid0()
  )
  val memoryManager = Module(new MemoryManager())
  memoryManager.initSigmoid(sigmoidMemory)
  val hiddenLayer1_result: Vec[SInt] = RegInit(VecInit(Seq.fill(10)(0.S(32.W))))
  val hiddenLayerRow: UInt = RegInit(0.U(4.W)) // 10
  val hiddenLayerCol: UInt = RegInit(0.U(5.W)) // 26
  val hiddenLayer1 = Module(
    new HiddenLayer1(theta1, hiddenLayer1_result, sigmoid0_result)
  )

  val fetchingData = RegInit(true.B)

  switch(state) {
    is(State.receiving) {
      when(sAxis.data.tvalid) {
        val pixel: SInt = sAxis.data.tdata
        hiddenLayer0.handlePixel(pixel, col)
        when(sAxis.data.tlast) {
          state := State.firstSigmoid
        }
      }
    }
    is(State.firstSigmoid) {
      // printf(p"Begin Sigmoid")
      sigmoid0.handleSigmoid(
        hiddenLayer0_result,
        sigmoidMemory,
        sigmoid0_result
      )
      fetchingData := false.B
      when(!fetchingData) {
        state := State.secondSigmoid
      }
    }
    is(State.secondSigmoid) {

      for (i <- 0 until 26) {
        printf(p"[${sigmoid0_result(i)}]")
      }
      state := State.sending
      // hiddenLayer1.handleDotProduct(hiddenLayerRow, hiddenLayerCol)
      // // printf(p"[r${hiddenLayerRow},c${hiddenLayerCol}] ")
      // hiddenLayerCol := (hiddenLayerCol + 1.U)
      // when(hiddenLayerCol === (26.U - 1.U)) {
      //   hiddenLayerCol := 0.U
      //   hiddenLayerRow := (hiddenLayerRow + 1.U)
      //   when(hiddenLayerRow === (10.U - 1.U)) {
      //     state := State.sending
      //   }
      // }
    }
    is(State.sending) {
      // for (i <- 0 until 10) {
      //   printf(p"${hiddenLayer1_result(i)}")
      // }
    }
  }

}

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

package lab2

import chisel3._

// AXI-Stream Definition
class AxiStreamData(width: Int) extends Bundle {
  val tdata  = UInt(width.W)
  val tkeep  = UInt((width/8).W)
  val tvalid = Bool()
  val tlast  = Bool()
}

class AxiStreamMasterIf(width: Int) extends Bundle {
  val data   = Output(new AxiStreamData(width))
  val tready = Input(Bool())
}

class AxiStreamSlaveIf(width: Int) extends Bundle {
  val data   = Input(new AxiStreamData(width))
  val tready = Output(Bool())
}

class AxiStreamExternalIf(width: Int) extends Bundle {
  val tdata  = Input(UInt(width.W))
  val tkeep  = Input(UInt((width/4).W))
  val tvalid = Input(Bool())
  val tlast  = Input(Bool())
  val tready = Output(Bool())

  def connect(in: AxiStreamMasterIf): Unit = {
    tdata     := in.data.tdata
    tkeep     := in.data.tkeep
    tvalid    := in.data.tvalid
    tlast     := in.data.tlast
    in.tready := tready
  }

  def connect(in: AxiStreamSlaveIf): Unit = {
    in.data.tdata  := tdata
    in.data.tkeep  := tkeep
    in.data.tvalid := tvalid
    in.data.tlast  := tlast
    tready         := in.tready
  }
}

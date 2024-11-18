package project

import chisel3._

/** NStanh function. Using a FSM, we simulate NStanh
  *
  * @param inputSi
  *   the bipolar stochastic stream Si {-m, m}. 8 bits + log2(weights)
  * @param inputMN
  *   m*n {1*n, 2*n, 3*n}
  * @param outputStream
  *   the unipolar value {0, 1}
  */
class NStanh(offset: UInt) extends Module {
  private val m_offset = RegInit(offset)
  private val m_counter = RegInit(0.U(10.W))

  val io = IO(new Bundle {
    val inputSi = Input(UInt(10.W))
    val inputMN = Input(UInt(4.W))
    val outputStream = Output(UInt(1.W))
  })

  m_counter := m_counter + io.inputSi
  when(m_counter > (io.inputMN - 1.U)) {
    m_counter := io.inputMN - 1.U
  }
  when(m_counter < 0.U) {
    m_counter := 0.U
  }
  when(m_counter > m_offset) {
    io.outputStream := 1.U
  }.otherwise {
    io.outputStream := 0.U
  }

}

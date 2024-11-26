package project

import chisel3._

/** NStanh function. Using a FSM, we simulate NStanh
  *
  * @param inputSi
  *   the bipolar stochastic stream Si {-m, m}. 2 bits + log2(weights)
  * @param inputMN
  *   m*n {1*n, 2*n, 3*n}
  * @param nbData
  *   nbCombinedStreams int
  * @param outputStream
  *   the unipolar value {0, 1}
  */
class NStanh(n: Int, m: Int) extends Module {
  private val m_offset = RegInit((128).S)
  private val m_MN = RegInit((n * m).S)
  private val m_counter = RegInit(0.S(10.W))

  val io = IO(new Bundle {
    val inputSi = Input(SInt((m + 1).W))
    val outputStream = Output(UInt(1.W))
  })

  m_counter := m_counter + io.inputSi
  when(m_counter > (m_MN - 1.S)) {
    m_counter := m_MN - 1.S
  }
  when(m_counter < 0.S) {
    m_counter := 0.S
  }
  when(m_counter > m_offset) {
    io.outputStream := 1.U
  }.otherwise {
    io.outputStream := 0.U
  }

}

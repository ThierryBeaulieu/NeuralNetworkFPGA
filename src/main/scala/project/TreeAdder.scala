package project

import chisel3._
import chisel3.Vec._

/** Takes a value in the integral stream and returns a 0 or the integral value
  * according to the bit
  *
  * @param inputStream
  *   vector of stochastic stream [{-m, m},..., {-m, m}]
  *
  * @param outputStream
  *   stochastic stream {-(m1+...+m4), +(m1+...+m4)}
  */
class TreeAdder() extends Module {
  val io = IO(new Bundle {
    val inputStream = Input(Vec(4, SInt(2.W)))
    val outputStream = Output(SInt(4.W))
  })

  io.outputStream := io.inputStream.reduceTree((a, b) => (a +& b))
}

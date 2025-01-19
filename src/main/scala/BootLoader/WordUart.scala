import chisel3._
import chisel3.util._
import chisel.lib.uart._

class WordUart(freq: Int, baud: Int) extends Module {
  val io = IO(new Bundle {
    val rx = Input(Bool())
    val word = Output(UInt(32.W))
    val wordReady = Output(Bool())
    // Debug
    val tx = Output(Bool())
  })

  val uart = Module(new Rx(freq, baud))
  uart.io.rxd := io.rx
  uart.io.channel.ready := true.B
  val update = uart.io.channel.valid

  val buffer = RegInit(VecInit(Seq.fill(3)(0.U(8.W))))
  val count = RegInit(0.U(2.W))
  when (update) {
    buffer(0) := uart.io.channel.bits
    buffer(1) := buffer(0)
    buffer(2) := buffer(1)
    count := count + 1.U
  }

  val wordReady = update && (count === 3.U)
  val word = Mux(wordReady, uart.io.channel.bits ## buffer(0) ## buffer(1) ## buffer(2), 0.U(32.W))

  io.word := RegNext(word)
  io.wordReady := RegNext(wordReady)

  // Debug (echo):
  val echo = Module(new Tx(freq, baud))
  echo.io.channel <> uart.io.channel
  io.tx := echo.io.txd
}
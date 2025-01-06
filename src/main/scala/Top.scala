import chisel3._
import chisel3.util._

class Top extends Module {
  val io = IO(new Bundle {
    val led = Output(Vec(16, Bool()))
  })

  io.led := Seq.fill(16)(false.B)

  val CPU = Module(new CPU())

  CPU.io.readData := 0.U
  CPU.io.instData := 0x07b00093.U

  io.led(0) := CPU.io.debug
}

object Top extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Top())
}
import chisel3._
import chisel3.util._

class Top extends Module {
  val io = IO(new Bundle {
    val led = Output(Vec(16, Bool()))
  })

  val CPU = Module(new CPU())
  val instMem = Module(new Memory(0x1000, 0x0000))
  val dataMem = Module(new Memory(0x1000, 0x1000))
  instMem.io <> CPU.io.inst
  dataMem.io <> CPU.io.data

  // Debug
  io.led := Seq.fill(16)(false.B)
  io.led(0) := CPU.io.debug

  
  when (reset.asBool) {
    instMem.io.writeData := 0x07b00093.U
    instMem.io.write := true.B
    instMem.io.addr := 8.U
  }
}

object Top extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Top())
}
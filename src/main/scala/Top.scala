import chisel3._
import chisel3.util._

class Top extends Module {
  val io = IO(new Bundle {
    val led = Output(Vec(16, Bool()))
  })

  val CPU = Module(new CPU())
  val instMem = Module(new Memory(0x1000, 0x0000))
  val dataMem = Module(new Memory(0x1000, 0x1000))
  val ledController = Module(new LEDController(0x1000, 0x2000))
  instMem.io <> CPU.io.inst
  dataMem.io <> CPU.io.data
  ledController.io.bus <> CPU.io.data

  // dataMem.io.addr := CPU.io.data.addr
  // dataMem.io.writeData := CPU.io.writeData
  // dataMem.io.write := CPU.io.write

  when (dataMem.io.readValid) {
    CPU.io.data.readData := dataMem.io.readData
  } .elsewhen (ledController.io.bus.readValid) {
    CPU.io.data.readData := ledController.io.bus.readData
  } .otherwise {
    CPU.io.data.readData := 0.U
  }

  io.led := ledController.io.led

  // // Debug
  // io.led := Seq.fill(16)(false.B)
  // io.led(0) := CPU.io.debug
}

object Top extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Top())
}
import chisel3._
import chisel3.util._

class Top(freq: Int, baud: Int) extends Module {
  val io = IO(new Bundle {
    val led = Output(Vec(16, Bool()))
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  val CPUreset = WireDefault(reset) // Needed for boot loader
  val CPU = withReset(RegNext(CPUreset)) { Module(new CPU()) }
  val instMem = Module(new Memory(0x0000, 0x1000))
  val dataMem = Module(new Memory(0x1000, 0x1000))
  val led = withReset(RegNext(CPUreset)) { Module(new LEDController(0x2000, 0x1000)) }
  val uart = withReset(RegNext(CPUreset)) { Module(new UARTController(0x3000, 0x1000, freq, baud)) }
  instMem.io <> CPU.io.inst
  dataMem.io <> CPU.io.data
  led.io.bus <> CPU.io.data
  uart.io.bus <> CPU.io.data

  when (dataMem.io.readValid) {
    CPU.io.data.readData := dataMem.io.readData
  } .elsewhen (led.io.bus.readValid) {
    CPU.io.data.readData := led.io.bus.readData
  } .elsewhen (uart.io.bus.readValid) {
    CPU.io.data.readData := uart.io.bus.readData
  } .otherwise {
    CPU.io.data.readData := 0.U
  }

  io.led := led.io.led
  uart.io.rx := io.rx
  io.tx := uart.io.tx

  // Boot loader
  val bootLoaderUart = Module(new WordUart(freq, baud))
  bootLoaderUart.io.rx := io.rx
  val bootLoader = Module(new BootLoader)
  bootLoader.io.word := bootLoaderUart.io.word
  bootLoader.io.wordReady := bootLoaderUart.io.wordReady
  when (bootLoader.io.write) {
    instMem.io.addr := bootLoader.io.addr
    instMem.io.writeData := bootLoader.io.writeData
    instMem.io.writeWord := true.B
  }
  when (bootLoader.io.loading) {
    CPUreset := true.B
  }
}

object Top extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Top(100000000, 115200))
}
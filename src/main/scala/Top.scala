import chisel3._
import chisel3.util._

class TopIO extends Bundle {
  val led = Output(Vec(16, Bool()))
  val rx = Input(Bool())
  val tx = Output(Bool())
  val btn = Input(Vec(8, Bool()))
  val vga = new VGA()
}

class TopSlow(freq: Int, baud: Int) extends Module {
  val io = IO(new TopIO())

  val CPUreset = WireDefault(reset) // Needed for boot loader
  val CPU = withReset(RegNext(CPUreset)) { Module(new CPU()) }
  val instMem = Module(new Memory(0x0000, 0x1000))
  val dataMem = Module(new Memory(0x1000, 0x1000))
  val led = withReset(RegNext(CPUreset)) { Module(new LEDController(0x2000, 0x1000)) }
  val uart = withReset(RegNext(CPUreset)) { Module(new UARTController(0x3000, 0x1000, freq, baud)) }
  val buttons = withReset(RegNext(CPUreset)) { Module(new ButtonController(0x4000,0x1000, freq)) }
  val video = Module(new VideoController(0x100000, 0x100000, freq))
  instMem.io <> CPU.io.inst
  dataMem.io <> CPU.io.data
  led.io.bus <> CPU.io.data
  uart.io.bus <> CPU.io.data
  buttons.io.bus <> CPU.io.data
  video.io.bus <> CPU.io.data

  CPU.io.data.readData := dataMem.io.readData | led.io.bus.readData | uart.io.bus.readData | buttons.io.bus.readData

  io.led := led.io.led
  uart.io.rx := io.rx
  io.tx := uart.io.tx

  buttons.io.btn := io.btn
  
  io.vga := video.io.vga

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
    dataMem.io.addr := bootLoader.io.addr
    dataMem.io.writeData := bootLoader.io.writeData
    dataMem.io.writeWord := true.B
  }
  when (bootLoader.io.loading) {
    CPUreset := true.B
  }
  CPU.io.startAddr := bootLoader.io.startAddr
}

class clk_wiz_0 extends BlackBox {
  val io = IO(new Bundle {
    val reset = Input(Bool())
    val clk_in = Input(Clock())
    val clk_out = Output(Clock())
    val locked = Output(Bool())
  })
}

class Top(freq: Int, baud: Int) extends Module {
  val io = IO(new TopIO())

  val wiz = Module(new clk_wiz_0)
  wiz.io.clk_in := clock
  wiz.io.reset := false.B

  withClock(wiz.io.clk_out) {
    val syncReset = RegNext(RegNext(RegNext(reset)))
    val top = withReset(syncReset) { Module(new TopSlow(freq, baud)) }
    io <> top.io
  }
}

object Top extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Top(75000000, 115200))
}

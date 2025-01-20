import chisel3._
import chisel3.util._

class TopIO extends Bundle {
  val led = Output(Vec(16, Bool()))
  val rx = Input(Bool())
  val tx = Output(Bool())
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
  val video = Module(new VideoController(0x100000, 0x200000))
  instMem.io <> CPU.io.inst
  dataMem.io <> CPU.io.data
  led.io.bus <> CPU.io.data
  uart.io.bus <> CPU.io.data
  video.io.bus <> CPU.io.data

  CPU.io.data.readData := dataMem.io.readData | led.io.bus.readData | uart.io.bus.readData

  io.led := led.io.led
  uart.io.rx := io.rx
  io.tx := uart.io.tx

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
  // Debug:
  // io.tx := bootLoaderUart.io.tx
  io.led(13) := bootLoader.io.loading
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

  // val clk = Reg(Bool())
  // clk := ~clk
  val wiz = Module(new clk_wiz_0)
  wiz.io.reset := reset
  wiz.io.clk_in := clock
  // val topSlow = withClockAndReset(wiz.io.clk_out, ~wiz.io.locked) { Module(new TopSlow(freq, baud)) }
  // io <> topSlow.io


  // withClock(wiz.io.clk_out) {
  //   val debugReset = RegInit(false.B)
  //   // val reset = RegNext(RegNext(RegNext(~wiz.io.locked)))
  //   val delayedReset = RegNext(RegNext(RegNext(reset)))
  //   reset := false.B
  //   // val topSlow = withReset(RegNext(RegNext(RegNext(~wiz.io.locked)))) { Module(new TopSlow(freq, baud)) }
  //   val topSlow = withReset(reset) { Module(new TopSlow(freq, baud)) }
  //   io <> topSlow.io
  // }



  val topSlow = withClockAndReset(wiz.io.clk_out, RegNext(RegNext(RegNext(~wiz.io.locked)))) { Module(new TopSlow(freq, baud)) }
  io <> topSlow.io

  io.led(15) := wiz.io.locked
  // io.led := topSlow.io.led
  // val io = topSlow.io
}

object Top extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Top(80000000, 115200))
}
import chisel3._
import chisel3.util._

class LEDController(size: Int, start: Int) extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new Bus())
    val led = Output(Vec(16, Bool()))
  })

  val width = log2Up(size)
  val page = io.bus.addr(31,width)

  val led = RegInit(0.U(16.W))

  io.bus.readData := led
  io.bus.readValid := RegNext(page === (start/size).U)
  io.led := led.asBools

  when (io.bus.write && (page === (start/size).U)) {
    led := io.bus.writeData
  }
}
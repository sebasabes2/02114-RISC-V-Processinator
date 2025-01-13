import chisel3._
import chisel3.util._

class LEDController(start: Int, size: Int) extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new Bus())
    val led = Output(Vec(16, Bool()))
  })

  val width = log2Up(size)
  val page = io.bus.addr(31,width)

  val led0 = RegInit(0.U(8.W))
  val led1 = RegInit(0.U(8.W))

  io.bus.readData := led1 ## led0
  io.bus.readValid := RegNext(page === (start/size).U)
  io.led := (led1 ## led0).asBools

  // Write
  when (page === (start/size).U) {
    when (io.bus.writeWord || io.bus.writeHalf) {
      led0 := io.bus.writeData(7,0)
      led1 := io.bus.writeData(15,8)
    } .elsewhen(io.bus.writeByte) {
      when (io.bus.addr(0)) {
        led1 := io.bus.writeData(7,0)
      } .otherwise {
        led0 := io.bus.writeData(7,0)
      }
    }
  }
}
import chisel3._
import chisel3.util._
import lib.peripherals.MemoryMappedUart
import lib.peripherals.MemoryMappedUart.UartPins

class UARTController(start: Int, size: Int, freq: Int, baud: Int) extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new Bus())
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  val width = log2Up(size)
  val page = io.bus.addr(31,width)
  val index = io.bus.addr(width - 1,0)

  val uart = MemoryMappedUart(freq, baud, 8, 8)

  uart.io.pins.rx := io.rx
  io.tx := uart.io.pins.tx

  uart.io.port.read := page === (start/size).U
  uart.io.port.addr := index

  io.bus.readData := uart.io.port.rdData
  io.bus.readValid := RegNext(page === (start/size).U)

  when (io.bus.write && (page === (start/size).U)) {
    uart.io.port.write := true.B
    uart.io.port.wrData := io.bus.writeData
  } .otherwise {
    uart.io.port.write := false.B
    uart.io.port.wrData := 0.U
  }
}
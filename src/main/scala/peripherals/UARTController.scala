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

  val valid = page === (start/size).U && io.bus.read
  uart.io.port.read := valid
  uart.io.port.addr := index

  when (RegNext(valid)) {
    io.bus.readData := uart.io.port.rdData
  } .otherwise {
    io.bus.readData := 0.U
  }
  io.bus.readValid := valid

  val write = io.bus.writeWord | io.bus.writeHalf | io.bus.writeByte
  when (write && (page === (start/size).U)) {
    uart.io.port.write := true.B
    uart.io.port.wrData := io.bus.writeData
  } .otherwise {
    uart.io.port.write := false.B
    uart.io.port.wrData := 0.U
  }
}
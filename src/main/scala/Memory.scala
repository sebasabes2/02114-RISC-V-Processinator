import chisel3._
import chisel3.util._

class Bus extends Bundle {
  val addr = Output(UInt(32.W))
  val readData = Input(UInt(32.W))
  val readValid = Input(Bool())
  val writeData = Output(UInt(32.W))
  val write = Output(Bool())
}

class Memory(start: Int, size: Int) extends Module {
  val io = IO(Flipped(new Bus()))
  val mem = SyncReadMem(size/4, UInt(32.W)) //right shift?
  val width = log2Up(size)
  val page = io.addr(31,width)
  val index = io.addr(width - 1,2)
  when (RegNext(page === (start/size).U)) {
    io.readData := mem.read(index)
    io.readValid := true.B
  } .otherwise {
    io.readData := 0.U
    io.readValid := false.B
  }
  when (io.write && (page === (start/size).U)) {
    mem.write(index, io.writeData)
  }
}

import chisel3._
import chisel3.util._

class Bus extends Bundle {
  val addr = Output(UInt(32.W))
  val readData = Input(UInt(32.W))
  val readValid = Input(Bool())
  val writeData = Output(UInt(32.W))
  val write = Output(Bool())
}

class Memory(size: Int, start: Int) extends Module {
  val io = IO(Flipped(new Bus()))
  val mem = SyncReadMem(size/4, UInt(32.W))
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
  when (io.write && (page === (start/size).U)) { // RegNext(page === (start/size).U)
    mem.write(index, io.writeData)
  }

  // Debug
  // when (reset.asBool) {
  //   mem.write(0.U, 0x00002137.U) // lui x2, 2
  // } .elsewhen (RegNext(reset.asBool)) {
  //   mem.write(0.U, 0x00600093.U) // addi x1, x0, 6
  // } .elsewhen (RegNext(RegNext(reset.asBool))) {
  //   mem.write(0.U, 0x00112023.U) // sw x1, 0(x2)
  // }
  // println("size, size/4, width, width-1, start/size", size, size/4, width, width-1, start/size)
}

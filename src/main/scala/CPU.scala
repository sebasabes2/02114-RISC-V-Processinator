import chisel3._
import chisel3.util._

class CPU extends Module {
  val io = IO(new Bundle {
    val instAddr = Output(UInt(32.W))
    val instData = Input(UInt(32.W))

    val addr = Output(UInt(32.W))
    val readData = Input(UInt(32.W))
    val writeData = Output(UInt(32.W))
    val write = Output(Bool())

    val debug = Output(Bool())
  })

  val reg = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val PC = RegInit(0.U(32.W))

  // Fetch
  io.instAddr := PC
  PC := PC + 4.U

  // Decode
  val instruction = io.instData
  when (instruction === 0x07b00093.U) {
    reg(1.U) := 123.U 
  }

  // Memory
  io.addr := 0.U
  io.writeData := 0.U
  io.write := false.B

  // Debug
  io.debug := reg(1.U) === 123.U
}

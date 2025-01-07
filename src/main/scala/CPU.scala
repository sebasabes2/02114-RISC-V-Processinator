import chisel3._
import chisel3.util._

class CPU extends Module {
  val io = IO(new Bundle {
    val inst = new Bus()
    val data = new Bus()
    val debug = Output(Bool())
  })

  val reg = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val PC = RegInit(0.U(32.W))

  // Fetch
  io.inst.addr := PC
  io.inst.write := false.B
  io.inst.writeData := 0.U
  PC := PC + 4.U

  // Decode
  val instruction = io.inst.readData
  when (instruction === 0x07b00093.U) {
    reg(1.U) := 123.U 
  }

  // Memory
  io.data.addr := 0.U
  io.data.writeData := 0.U
  io.data.write := false.B

  // Debug
  io.debug := reg(1.U) === 123.U
}

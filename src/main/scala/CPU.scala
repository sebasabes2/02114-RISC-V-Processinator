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

  val opcode = instruction(6,0) //control
  val rd = RegNext(instruction(11,7)) // write register
  val funct3 = RegNext(instruction(14,12))
  val rs1 = RegNext(instruction(19,15)) //read register 1
  val rs2 = RegNext(instruction(24,20)) //read register 2
  val funct7 = RegNext(instruction(31,25))

  val I_imm = RegNext(instruction(31,20))
  val S_imm = RegNext(instruction(31,25) ## instruction(11,7))
  val B_imm = RegNext(instruction(31) ## instruction(7) ## instruction(30,25) ## instruction(11,8) ## 0.U(1.W))
  val U_imm = RegNext(instruction(31,12))
  val J_imm = RegNext(instruction(31) ## instruction(19,12) ## instruction(20) ## instruction(30,21))

  // Execute

  reg(rd) := reg(rs1) + reg(rs2)

  // Memory
  io.data.addr := 0.U
  io.data.writeData := 0.U
  io.data.write := false.B

  // Debug
  io.debug := reg(1.U) === 123.U
}

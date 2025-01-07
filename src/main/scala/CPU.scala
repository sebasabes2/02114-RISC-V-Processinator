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
  io.addr := 0.U
  io.writeData := 0.U
  io.write := false.B

  // Debug
  io.debug := reg(1.U) === 123.U
}

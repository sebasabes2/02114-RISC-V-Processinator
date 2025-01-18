import chisel3._
import chisel3.util._

class RegFile() extends Module {
  val io = IO(new Bundle {
    val rs1, rs2 = Input(UInt(5.W))
    val imm1, imm2 = Input(UInt(32.W))
    val useImm1, useImm2 = Input(Bool())
    
    val op1, op2 = Output(UInt(32.W))
    val rv1, rv2 = Output(UInt(32.W))

    val rd = Input(UInt(5.W))
    val writeData = Input(UInt(32.W))
    val write = Input(Bool())
    
    // Debug
    val debugReg = Output(Vec(32, UInt(32.W)))
  })

  val reg1 = SyncReadMem(32, UInt(32.W))
  val reg2 = SyncReadMem(32, UInt(32.W))
  val debugReg = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  val rv1 = reg1.read(io.rs1)
  val rv2 = reg2.read(io.rs2)

  io.op1 := Mux(RegNext(io.useImm1), RegNext(io.imm1), rv1)
  io.op2 := Mux(RegNext(io.useImm2), RegNext(io.imm2), rv2)
  io.rv1 := rv1
  io.rv2 := rv2

  when (io.write) {
    reg1.write(io.rd, io.writeData)
    reg2.write(io.rd, io.writeData)
    debugReg(io.rd) := io.writeData
  }

  io.debugReg := debugReg
}

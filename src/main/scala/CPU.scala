import chisel3._
import chisel3.util._

class ControlSignals extends Bundle {
  // Decode
  val operand1, operand2 = UInt(32.W)
  val ALUWB, MemWB, MemStore = Bool()
  val ALUmode = UInt(4.W)
  val rd, rs = UInt(5.W)
  // Execute
  val ALUResult = UInt(32.W)
}

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
  val rd = instruction(11,7) // write register
  val funct3 = instruction(14,12)
  val rs1 = instruction(19,15) //read register 1
  val rs2 = instruction(24,20) //read register 2
  val funct7 = instruction(31,25)

  val I_imm = instruction(31,20)
  val S_imm = instruction(31,25) ## instruction(11,7)
  val B_imm = instruction(31) ## instruction(7) ## instruction(30,25) ## instruction(11,8) ## 0.U(1.W)
  val U_imm = instruction(31,12) ## 0.U(12.W)
  val J_imm = instruction(31) ## instruction(19,12) ## instruction(20) ## instruction(30,21) ## 0.U(1.W)

  val id = Wire(new ControlSignals)
  id.operand1 := 0.U
  id.operand2 := 0.U
  id.ALUWB := false.B
  id.MemWB := false.B
  id.MemStore := false.B
  id.ALUmode := 0.U
  id.rd := rd
  id.rs := rs2
  id.ALUResult := 0.U

  switch (opcode) {
    is (Opcodes.add) {
      id.operand1 := reg(rs1)
      id.operand2 := reg(rs2)
      id.ALUWB := true.B
      id.ALUmode := funct7(5) ## funct3
    }
    is (Opcodes.addi) {
      id.operand1 := reg(rs1)
      id.operand2 := I_imm
      id.ALUWB := true.B
      id.ALUmode := funct3
    }
    is (Opcodes.load) {
      id.operand1 := reg(rs1)
      id.operand2 := I_imm
      id.MemWB := true.B
    }
    is (Opcodes.store) {
      id.operand1 := reg(rs1)
      id.operand2 := S_imm
      id.MemStore := true.B
    }
    is (Opcodes.branch) {
      // complicated
    }
    is (Opcodes.lui) {
      id.operand1 := U_imm
      id.operand2 := 0.U
      id.ALUWB := true.B
    }
    is (Opcodes.auipc) {
      id.operand1 := U_imm
      id.operand2 := PC
      id.ALUWB := true.B
    }
  }

  // Execute
  val ex = RegNext(id)
  ex.ALUResult := 0.U
  switch (ex.ALUmode(2,0)) {
    is (0.U) {
      when (ex.ALUmode(3)) {
        ex.ALUResult := ex.operand1 - ex.operand2
      } .otherwise {
        ex.ALUResult := ex.operand1 + ex.operand2
      }
    }
    is (1.U){
      ex.ALUResult := ex.operand1 << ex.operand2(5,0)
    }
    is (2.U){
      ex.ALUResult := ex.operand1.asSInt < ex.operand2.asSInt
    }
    is (3.U){
      ex.ALUResult := ex.operand1 < ex.operand2
    }
    is (4.U){
      ex.ALUResult := ex.operand1 ^ ex.operand2
    }
    is (5.U){ //check if SInt is on srl or sra
      when (ex.ALUmode(3)) { //sra
        ex.ALUResult := ex.operand1 >> ex.operand2(5,0) //test if 5,0 or 4,0. (4,0 >> 31x ikke 32x)
      } .otherwise { //srl
        ex.ALUResult := (ex.operand1.asSInt >> ex.operand2(5,0)).asUInt
      }
    }
    is (6.U){
      ex.ALUResult := ex.operand1 | ex.operand2
    }
    is (7.U){
      ex.ALUResult := ex.operand1 & ex.operand2
    }
  }

  // Memory
  val mem = RegNext(ex)
  io.data.addr := mem.ALUResult
  io.data.writeData := reg(mem.rs)
  io.data.write := mem.MemStore

  // Writeback
  val wb = RegNext(mem)  
  when (wb.rd =/= 0.U) {
    when (wb.ALUWB) {
      reg(wb.rd) := wb.ALUResult
    } .elsewhen (wb.MemWB) {
      reg(wb.rd) := io.data.readData
    }
  }

  // Debug
  io.debug := reg(1.U) === 123.U
}

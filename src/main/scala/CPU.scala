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
  val rd = (instruction(11,7)) // write register
  val funct3 = (instruction(14,12))
  val rs1 = (instruction(19,15)) //read register 1
  val rs2 = (instruction(24,20)) //read register 2
  val funct7 = (instruction(31,25))

  val I_imm = (instruction(31,20))
  val S_imm = (instruction(31,25) ## instruction(11,7))
  val B_imm = (instruction(31) ## instruction(7) ## instruction(30,25) ## instruction(11,8) ## 0.U(1.W))
  val U_imm = (instruction(31,12))
  val J_imm = (instruction(31) ## instruction(19,12) ## instruction(20) ## instruction(30,21))

  val operand1 = WireDefault(0.U(32.W))
  val operand2 = WireDefault(0.U(32.W))
  val ALUWB = WireDefault(false.B)
  val MemWB = WireDefault(false.B)
  val ALUmode = WireDefault(0.U(3.W))

  switch (opcode) {
    is (Opcodes.add) {
      operand1 := reg(rs1)
      operand2 := reg(rs2)
      ALUWB := true.B
      ALUmode := funct3
    }
    is (Opcodes.addi) {
      operand1 := reg(rs1)
      operand2 := I_imm
      ALUWB := true.B
      ALUmode := funct3
    }
    is (Opcodes.load) {
      operand1 := reg(rs1)
      operand2 := I_imm
      MemWB := true.B
    }
    is (Opcodes.store) {
      operand1 := reg(rs1)
      operand2 := S_imm
    }
    is (Opcodes.branch) {
      // complicated
    }
    is (Opcodes.lui) {
      operand1 := U_imm << 12.U
      operand2 := 0.U
      ALUWB := true.B
    }
    is (Opcodes.auipc) {
      operand1 := U_imm << 12.U
      operand2 := PC
      ALUWB := true.B
    }
  }

  // Execute
  val op1 = RegNext(operand1)
  val op2 = RegNext(operand2)
  val ALUResult = WireDefault(0.U(32.W))

  switch (funct3) {
    is (0.U) {
      switch (funct7){
        is(0.U){ //add
          ALUResult := op1 + op2
        }
        is(32.U){ //sra
          ALUResult := op1 - op2
        }
      }
    }
    is (1.U){
      ALUResult := op1 << op2(5,0)
    }
    is (2.U){
      ALUResult := op1.asSInt < op2.asSInt
    }
    is (3.U){
      ALUResult := op1 < op2
    }
    is (4.U){
      ALUResult := op1 ^ op2
    }
    is (5.U){ //check if SInt is on srl or sra
      switch (funct7){
        is(0.U){ //srl
          ALUResult := (op1.asSInt >> op2(5,0)).asUInt
        }
        is(32.U){ //sra
          ALUResult := op1 >> op2(5,0) //test if 5,0 or 4,0. (4,0 >> 31x ikke 32x)
        }
      }
    }
    is (6.U){
      ALUResult := op1 | op2
    }
    is (7.U){
      ALUResult := op1 & op2
    }
  }

  // Memory
  io.data.addr := ALUResult
  io.data.writeData := 0.U
  io.data.write := false.B

  // Writeback
  val destination = RegNext(RegNext(RegNext(rd)))
  when (destination =/= 0.U) {
    when (RegNext(RegNext(RegNext(ALUWB)))) {
      reg(destination) := RegNext(RegNext(ALUResult))
    } .elsewhen (RegNext(RegNext(RegNext(MemWB)))) {
      reg(destination) := io.data.readData
    }
  }

  // Debug
  io.debug := reg(1.U) === 123.U
}

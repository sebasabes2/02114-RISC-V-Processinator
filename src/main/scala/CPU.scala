import chisel3._
import chisel3.util._

class CPU extends Module {
  val io = IO(new Bundle {
    val inst = new Bus()
    val data = new Bus()
    val startAddr = Input(UInt(32.W))

    // Only for testing
    val reg = Output(Vec(32, UInt(32.W)))
    val PC = Output(UInt(32.W))
  })

  // GP-Registers
  val reg = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val newReg = WireDefault(reg)
  reg := newReg
  io.reg := newReg

  // PC
  val PC = RegInit(io.startAddr - 4.U)
  val newPC = WireDefault(PC + 4.U)
  io.PC := newPC

  // Fetch
  io.inst.addr := newPC
  io.inst.writeWord := false.B
  io.inst.writeHalf := false.B
  io.inst.writeByte := false.B
  io.inst.writeData := 0.U
  PC := newPC

  // Decode
  val instruction = io.inst.readData

  val opcode = instruction(6,0) //control
  val rd = (instruction(11,7)) // write register
  val funct3 = (instruction(14,12))
  val rs1 = (instruction(19,15)) //read register 1
  val rs2 = (instruction(24,20)) //read register 2
  val funct7 = (instruction(31,25))

  val I_imm = Fill(20, instruction(31)) ## instruction(31,20)
  val S_imm = Fill(20, instruction(31)) ## instruction(31,25) ## instruction(11,7)
  val B_imm = Fill(19, instruction(31)) ## (instruction(31) ## instruction(7) ## instruction(30,25) ## instruction(11,8) ## 0.U(1.W))
  val U_imm = (instruction(31,12) ## 0.U(12.W))
  val J_imm = Fill(11,instruction(31)) ## (instruction(31) ## instruction(19,12) ## instruction(20) ## instruction(30,21) ## 0.U(1.W))

  val operand1 = WireInit(UInt(32.W), DontCare)
  val operand2 = WireInit(UInt(32.W), DontCare)
  val ALUWB = WireDefault(false.B)
  val MemWB = WireDefault(false.B)
  val MemStore = WireDefault(false.B)
  val ALUmode = WireDefault(0.U(4.W))
  val Bmode = WireDefault(false.B)

  val useRs1 = WireDefault(false.B)
  val useRs2 = WireDefault(false.B)

  val jumpAddress = WireInit(UInt(32.W), DontCare)
  val doJump = WireDefault(false.B)

  
  // ALUResult belongs to Execute stage
  val ALUResult = WireInit(UInt(32.W), DontCare)

  switch (opcode) {
    is (Opcodes.add) {
      operand1 := newReg(rs1)
      operand2 := newReg(rs2)
      ALUWB := true.B
      ALUmode := funct7(5) ## funct3
      useRs1 := true.B
      useRs2 := true.B
    }
    is (Opcodes.addi) {
      operand1 := newReg(rs1)
      operand2 := I_imm
      ALUWB := true.B
      val artithmeticToggle = Mux(funct3 === ALUModes.shiftRight, funct7(5), 0.U(1.W))
      ALUmode := artithmeticToggle ## funct3
      useRs1 := true.B
    }
    is (Opcodes.load) {
      operand1 := newReg(rs1)
      operand2 := I_imm
      MemWB := true.B
      useRs1 := true.B
    }
    is (Opcodes.store) {
      operand1 := newReg(rs1)
      operand2 := S_imm
      MemStore := true.B
      useRs1 := true.B
    }
    is (Opcodes.branch) {
      operand1 := newReg(rs1)
      operand2 := newReg(rs2)
      ALUmode := funct3
      Bmode := true.B
      useRs1 := true.B
      useRs2 := true.B
      jumpAddress := PC + B_imm
    }
    is (Opcodes.lui) {
      operand1 := 0.U
      operand2 := U_imm
      ALUWB := true.B
    }
    is (Opcodes.auipc) {
      operand1 := PC
      operand2 := U_imm
      ALUWB := true.B
    }
    is (Opcodes.jal){
      operand1 := PC
      operand2 := 4.U
      ALUWB := true.B
      jumpAddress := PC + J_imm
      doJump := true.B
    }
    is (Opcodes.jalr){
      operand1 := PC
      operand2 := 4.U
      ALUWB := true.B
      when (RegNext(ALUWB) && (RegNext(rd) =/= 0.U) && (RegNext(rd) === rs1)) {
        jumpAddress := ALUResult + I_imm
      } .otherwise {
        jumpAddress := newReg(rs1) + I_imm
      }
      useRs1 := true.B
      doJump := true.B
    }
  }

  //ex
  val ex_forwardA = (RegNext(ALUWB) && (RegNext(rd) =/= 0.U) && (RegNext(rd) === rs1) && useRs1)
  val ex_forwardB = (RegNext(ALUWB) && (RegNext(rd) =/= 0.U) && (RegNext(rd) === rs2) && useRs2)

  //Mem-WB
  val mem_forwardA = (RegNext(MemWB) && (RegNext(rd) =/= 0.U) && (RegNext(rd) === rs1) && useRs1)
  val mem_forwardB = (RegNext(MemWB) && (RegNext(rd) =/= 0.U) && (RegNext(rd) === rs2) && useRs2)
  val mem_forward = mem_forwardA || mem_forwardB

  when(mem_forward){
    ALUWB := false.B
    MemWB := false.B
    MemStore := false.B
    Bmode := false.B
    doJump := false.B
    newPC := PC
  }

  val flushing = WireDefault(false.B)
  when (flushing) {
    ALUWB := false.B
    MemWB := false.B
    MemStore := false.B
    Bmode := false.B
    doJump := false.B
  }

  when (ex_forwardA) {
    operand1 := ALUResult
  }
  when (ex_forwardB) {
    operand2 := ALUResult
  }

  // Execute
  val op1 = RegNext(operand1)
  val op2 = RegNext(operand2)
  val BranchMode = RegNext(Bmode)
  val ex_ALUmode = RegNext(ALUmode)
  val funct3_ex = RegNext(funct3)

  switch (ex_ALUmode(2,0)) {
    is (ALUModes.add) {
      when (ex_ALUmode(3)) {
        ALUResult := op1 - op2
      } .otherwise {
        ALUResult := op1 + op2
      }
    }
    is (ALUModes.shiftLeft){
      ALUResult := op1 << op2(5,0)
    }
    is (ALUModes.setLessThan){
      ALUResult := (op1.asSInt < op2.asSInt).asUInt()
    }
    is (ALUModes.setLessThanU){
      ALUResult := (op1 < op2)
    }
    is (ALUModes.xor){
      ALUResult := op1 ^ op2
    }
    is (ALUModes.shiftRight){
      when (ex_ALUmode(3)) { //sra
        ALUResult := (op1.asSInt >> op2(4,0)).asUInt()
      } .otherwise { //srl
        ALUResult := op1 >> op2(4,0)
      }
    }
    is (ALUModes.or){
      ALUResult := op1 | op2
    }
    is (ALUModes.and){
      ALUResult := op1 & op2
    }
  }

  // val BranchTaken = WireDefault(false.B)
  val BranchTaken = WireInit(Bool(), DontCare)
  switch(ex_ALUmode) {
    is(BranchModes.beq) {
      BranchTaken := op1 === op2
    }
    is(BranchModes.bne) {
      BranchTaken := op1 =/= op2
    }
    is(BranchModes.blt) {
      BranchTaken := op1.asSInt < op2.asSInt
    }
    is(BranchModes.bge) {
      BranchTaken := op1.asSInt >= op2.asSInt
    }
    is(BranchModes.bltu) {
      BranchTaken := op1 < op2
    }
    is(BranchModes.bgeu) {
      BranchTaken := op1 >= op2
    }
  }

  when ((BranchMode && BranchTaken) || RegNext(doJump)) {
    newPC := RegNext(jumpAddress)
    flushing := true.B
  }

  // Memory (Execute continue)

  io.data.addr := ALUResult
  io.data.writeData := newReg(RegNext(rs2))
  io.data.writeByte := RegNext(MemStore) && (funct3_ex === 0.U)
  io.data.writeHalf := RegNext(MemStore) && (funct3_ex === 1.U)
  io.data.writeWord := RegNext(MemStore) && (funct3_ex === 2.U)


  // Memory/Writeback
  val funct3_mem = RegNext(funct3_ex)
  // val funct3_wb = RegNext(funct3_mem)
  
  //LoadToMem belongs to Mem-WB stage
  // val LoadToMem = WireDefault(0.U(32.W))
  val LoadToMem = WireInit(UInt(32.W), DontCare)
  switch(funct3_mem){
    is(0.U){ //byte
      LoadToMem := Fill(24,io.data.readData(7)) ## io.data.readData(7,0)
    }
    is(1.U){//half
      LoadToMem := Fill(16,io.data.readData(15)) ## io.data.readData(15,0)
    }
    is(2.U){ //word
      LoadToMem := io.data.readData
    }
    is(4.U){ //byte (U)
      LoadToMem := io.data.readData(7,0)
    }
    is(5.U){ //half (U)
      LoadToMem := io.data.readData(15,0)
    }
  }

  val destination = RegNext(RegNext(rd))
  when (destination =/= 0.U) {
    when (RegNext(RegNext(ALUWB))) {
      newReg(destination) := RegNext(ALUResult)
    } .elsewhen (RegNext(RegNext(MemWB))) {
      newReg(destination) := LoadToMem
    }
  }
}

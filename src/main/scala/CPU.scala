import chisel3._
import chisel3.util._

class CPU extends Module {
  val io = IO(new Bundle {
    val inst = new Bus()
    val data = new Bus()

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
  val PC = RegInit(0xfffffffcL.U(32.W))
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

  val operand1 = WireDefault(0.U(32.W))
  val operand2 = WireDefault(0.U(32.W))
  val ALUWB = WireDefault(false.B)
  val MemWB = WireDefault(false.B)
  val MemStore = WireDefault(false.B)
  val ALUmode = WireDefault(0.U(4.W))
  val Bmode = WireDefault(false.B)
  val Jmode = WireDefault(0.U(2.W))

  val useRs1 = WireDefault(false.B)
  val useRs2 = WireDefault(false.B)

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
    }
    is (Opcodes.lui) {
      operand1 := U_imm
      operand2 := 0.U
      ALUWB := true.B
    }
    is (Opcodes.auipc) {
      operand1 := U_imm
      operand2 := PC
      ALUWB := true.B
    }
    is (Opcodes.jal){
      operand1 := PC
      operand2 := 4.U
      ALUWB := true.B
      Jmode := 1.U
    }
    is (Opcodes.jalr){
      operand1 := PC
      operand2 := 4.U
      ALUWB := true.B
      Jmode := 2.U
    }
  }

  //ex
  val ex_forwardA = (RegNext(ALUWB) & (RegNext(rd) =/= 0.U) & (RegNext(rd) === rs1) & useRs1)
  val ex_forwardB = (RegNext(ALUWB) & (RegNext(rd) =/= 0.U) & (RegNext(rd) === rs2) & useRs2)

  //Mem-WB
  val mem_forwardA = (RegNext(MemWB) & (RegNext(rd) =/= 0.U) & (RegNext(rd) === rs1) & useRs1)
  val mem_forwardB = (RegNext(MemWB) & (RegNext(rd) =/= 0.U) & (RegNext(rd) === rs2) & useRs2)

  when(mem_forwardA || mem_forwardB){
    ALUWB := false.B
    MemWB := false.B
    MemStore := false.B
    Bmode := false.B
    newPC := PC
  }

  // ALUResult belongs to Execute stage
  val ALUResult = WireDefault(0.U(32.W))
  //LoadToMem belongs to Mem-WB stage
  val LoadToMem = WireDefault(0.U(32.W))
  when (ex_forwardA) {
    operand1 := ALUResult
  }
  when (ex_forwardB) {
    operand2 := ALUResult
  }

  // Execute
  val op1 = RegNext(operand1)
  val op2 = RegNext(operand2)
  val BranchOffset = RegNext(B_imm)
  val BranchMode = RegNext(Bmode)
  val ex_ALUmode = RegNext(ALUmode)
  val jalOffset = RegNext(J_imm)
  val jalrOffset = RegNext(I_imm)
  val JumpMode = RegNext(Jmode)
  val funct3_ex = RegNext(funct3)

  // when (forwardA) {
  //   op1 := RegNext(ALUResult)
  // }
  // when (forwardB) {
  //   op2 := RegNext(ALUResult)
  // }

//   when (mem_forwardA) {
//     op1 := LoadToMem
//   }
//   when (mem_forwardB) {
//     op2 := LoadToMem
//   }

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

  val BranchTaken = WireDefault(false.B)
  switch(ex_ALUmode) {
    is(0.U) {
      BranchTaken := op1 === op2
    }
    is(1.U) {
      BranchTaken := op1 =/= op2
    }
    is(4.U) {
      BranchTaken := op1.asSInt < op2.asSInt
    }
    is(5.U) {
      BranchTaken := op1.asSInt >= op2.asSInt
    }
    is(6.U) {
      BranchTaken := op1 < op2
    }
    is(7.U) {
      BranchTaken := op1 >= op2
    }
  }

  when(BranchMode && BranchTaken){
    newPC := RegNext(PC)+BranchOffset
  }

  switch(JumpMode){
    is(1.U){
      newPC := RegNext(PC)+jalOffset
    }
    is(2.U){
      newPC := newReg(RegNext(rs1))+jalrOffset
    }
  }

  // Memory (Execute continue)
  val funct3_mem = RegNext(funct3_ex)

  io.data.addr := ALUResult
  io.data.writeData := newReg(RegNext(rs2))
  io.data.writeByte := RegNext(MemStore) && (funct3_ex === 0.U)
  io.data.writeHalf := RegNext(MemStore) && (funct3_ex === 1.U)
  io.data.writeWord := RegNext(MemStore) && (funct3_ex === 2.U)


  // Memory/Writeback
  // val funct3_wb = RegNext(funct3_mem)
  switch(funct3_mem){
    is(0.U){
      LoadToMem := Fill(24,io.data.readData(7)) ## io.data.readData(7,0)
    }
    is(1.U){
      LoadToMem := Fill(16,io.data.readData(15)) ## io.data.readData(15,0)
    }
    is(2.U){
      LoadToMem := io.data.readData
    }
    is(4.U){
      LoadToMem := io.data.readData(7,0)
    }
    is(5.U){
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

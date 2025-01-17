import chisel3._
import chisel3.util._

class ControlSignals extends Bundle {
  // Decode
  val operand1, operand2, jumpAddress = UInt(32.W)
  val ALUWB, MemWB, MemStore, Bmode, doJump = Bool()
  val ALUmode = UInt(4.W)
  val rd, rs = UInt(5.W)
  val funct3 = UInt(3.W)
  // Execute
  // val ALUResult = UInt(32.W)
}

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

  // Stages
  val id = Wire(new ControlSignals)
  val ex = RegNext(id)
  val mem = RegNext(ex)

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
  val rd = instruction(11,7) // write register
  val funct3 = instruction(14,12)
  val rs1 = instruction(19,15) //read register 1
  val rs2 = instruction(24,20) //read register 2
  val funct7 = instruction(31,25)

  val I_imm = Fill(20, instruction(31)) ## instruction(31,20)
  val S_imm = Fill(20, instruction(31)) ## instruction(31,25) ## instruction(11,7)
  val B_imm = Fill(19, instruction(31)) ## (instruction(31) ## instruction(7) ## instruction(30,25) ## instruction(11,8) ## 0.U(1.W))
  val U_imm = (instruction(31,12) ## 0.U(12.W))
  val J_imm = Fill(11,instruction(31)) ## (instruction(31) ## instruction(19,12) ## instruction(20) ## instruction(30,21) ## 0.U(1.W))

  id.operand1 := 0.U
  id.operand2 := 0.U
  id.ALUWB := false.B
  id.MemWB := false.B
  id.MemStore := false.B
  id.ALUmode := 0.U
  id.Bmode := false.B
  id.rd := rd
  id.rs := rs2
  // id.ALUResult := 0.U
  id.funct3 := funct3
  
  val useRs1 = WireDefault(false.B)
  val useRs2 = WireDefault(false.B)

  id.jumpAddress := 0.U
  id.doJump := false.B

  switch (opcode) {
    is (Opcodes.add) {
      id.operand1 := newReg(rs1)
      id.operand2 := newReg(rs2)
      id.ALUWB := true.B
      id.ALUmode := funct7(5) ## funct3
      useRs1 := true.B
      useRs2 := true.B
    }
    is (Opcodes.addi) {
      id.operand1 := newReg(rs1)
      id.operand2 := I_imm
      id.ALUWB := true.B
      val artithmeticToggle = Mux(funct3 === ALUModes.shiftRight, funct7(5), 0.U(1.W))
      id.ALUmode := artithmeticToggle ## funct3
      useRs1 := true.B
    }
    is (Opcodes.load) {
      id.operand1 := newReg(rs1)
      id.operand2 := I_imm
      id.MemWB := true.B
      useRs1 := true.B
    }
    is (Opcodes.store) {
      id.operand1 := newReg(rs1)
      id.operand2 := S_imm
      id.MemStore := true.B
      useRs1 := true.B
    }
    is (Opcodes.branch) {
      id.operand1 := newReg(rs1)
      id.operand2 := newReg(rs2)
      id.ALUmode := funct3
      id.Bmode := true.B
      useRs1 := true.B
      useRs2 := true.B
      // Test
      id.jumpAddress := PC + B_imm
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
    is (Opcodes.jal){
      id.operand1 := PC
      id.operand2 := 4.U
      id.ALUWB := true.B
      id.jumpAddress := PC + J_imm
      id.doJump := true.B
    }
    is (Opcodes.jalr){
      id.operand1 := PC
      id.operand2 := 4.U
      id.ALUWB := true.B
      id.jumpAddress := newReg(rs1) + I_imm
      id.doJump := true.B
    }
  }

  //ex
  val ex_forwardA = (ex.ALUWB & ex.rd =/= 0.U & ex.rd === rs1 & useRs1)
  val ex_forwardB = (ex.ALUWB & ex.rd =/= 0.U & ex.rd === rs2 & useRs2)

  //Mem-WB
  val mem_forwardA = (ex.MemWB & ex.rd =/= 0.U & ex.rd === rs1 & useRs1)
  val mem_forwardB = (ex.MemWB & ex.rd =/= 0.U & ex.rd === rs2 & useRs2)

  when(mem_forwardA || mem_forwardB){
    id.ALUWB := false.B
    id.MemWB := false.B
    id.MemStore := false.B
    id.Bmode := false.B
    newPC := PC
  }

  val flushing = WireDefault(false.B)
  when(flushing){
    id.ALUWB := false.B
    id.MemWB := false.B
    id.MemStore := false.B
    id.Bmode := false.B
  }

  val ALUResult = WireDefault(0.U(32.W))
  when (ex_forwardA) {
    id.operand1 := ALUResult
  }
  when (ex_forwardB) {
    id.operand2 := ALUResult
  }

  // Execute
  switch (ex.ALUmode(2,0)) {
    is (ALUModes.add) {
      when (ex.ALUmode(3)) {
        ALUResult := ex.operand1 - ex.operand2
      } .otherwise {
        ALUResult := ex.operand1 + ex.operand2
      }
    }
    is (ALUModes.shiftLeft){
      ALUResult := ex.operand1 << ex.operand2(4,0)
    }
    is (ALUModes.setLessThan){
      ALUResult := ex.operand1.asSInt < ex.operand2.asSInt
    }
    is (ALUModes.setLessThanU){
      ALUResult := ex.operand1 < ex.operand2
    }
    is (ALUModes.xor){
      ALUResult := ex.operand1 ^ ex.operand2
    }
    is (ALUModes.shiftRight){
      when (ex.ALUmode(3)) { //sra
        ALUResult := (ex.operand1.asSInt >> ex.operand2(4,0)).asUInt()
      } .otherwise { //srl
        ALUResult := ex.operand1 >> ex.operand2(4,0)
      }
    }
    is (ALUModes.or){
      ALUResult := ex.operand1 | ex.operand2
    }
    is (ALUModes.and){
      ALUResult := ex.operand1 & ex.operand2
    }
  }

  val BranchTaken = WireDefault(false.B)
  switch(ex.ALUmode) {
    is(0.U) {
      BranchTaken := ex.operand1 === ex.operand2
    }
    is(1.U) {
      BranchTaken := ex.operand1 =/= ex.operand2
    }
    is(4.U) {
      BranchTaken := ex.operand1.asSInt < ex.operand2.asSInt
    }
    is(5.U) {
      BranchTaken := ex.operand1.asSInt >= ex.operand2.asSInt
    }
    is(6.U) {
      BranchTaken := ex.operand1 < ex.operand2
    }
    is(7.U) {
      BranchTaken := ex.operand1 >= ex.operand2
    }
  }

  when ((ex.Bmode && BranchTaken) || ex.doJump) {
    newPC := ex.jumpAddress
    flushing := true.B
  }

  // Memory (Execute continue)
  // val funct3_mem = RegNext(funct3_ex)

  io.data.addr := ALUResult
  io.data.writeData := newReg(ex.rs)
  io.data.writeByte := ex.MemStore && (ex.funct3 === 0.U)
  io.data.writeHalf := ex.MemStore && (ex.funct3 === 1.U)
  io.data.writeWord := ex.MemStore && (ex.funct3 === 2.U)


  // Memory/Writeback
  // val funct3_wb = RegNext(funct3_mem)
  
  val LoadToMem = WireDefault(0.U(32.W))
  switch(mem.funct3){
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

  when (mem.rd =/= 0.U) {
    when (mem.ALUWB) {
      newReg(mem.rd) := RegNext(ALUResult)
    } .elsewhen (mem.MemWB) {
      newReg(mem.rd) := LoadToMem
    }
  }
}

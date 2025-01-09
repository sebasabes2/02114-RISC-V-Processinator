import chisel3._
import chisel3.util._

class Top extends Module {
  val io = IO(new Bundle {
    val led = Output(Vec(16, Bool()))
  })

  val CPUreset = WireDefault(false.B) // Needed for FSM boot loader
  val CPU = withReset(CPUreset) { Module(new CPU()) }
  val instMem = Module(new Memory(0x1000, 0x0000))
  val dataMem = Module(new Memory(0x1000, 0x1000))
  val ledController = Module(new LEDController(0x1000, 0x2000))
  instMem.io <> CPU.io.inst
  dataMem.io <> CPU.io.data
  ledController.io.bus <> CPU.io.data

  when (dataMem.io.readValid) {
    CPU.io.data.readData := dataMem.io.readData
  } .elsewhen (ledController.io.bus.readValid) {
    CPU.io.data.readData := ledController.io.bus.readData
  } .otherwise {
    CPU.io.data.readData := 0.U
  }

  io.led := ledController.io.led

  // FSM boot loader

  val program = Array(
    0x00002137, // lui x2, 2
    0x00600093, // addi x1, x0, 6
    0x00112023  // sw x1, 0(x2)
  )

  val program = Array (
    0x00000093, // li x1, 0      # loop index 
    0x00001137, // li x2, 0x1000 # max loop
    0x00100193, // li x3, 1      # LED on
    0x00002237, // li x4, 0x2000 # LED address
                
                // loop 1:
    0x00108093, // addi x1, x1, 1
    0,
    0,
    0,
    0xfe20cee3, // blt x1, x2, loop1
    0x00322023, // sw x3, 0(x4)

                // loop 2:
    0x403080b3, // sub x1, x1, x3
    0xfe104ee3, // blt x0, x1, loop2
    0x00022023, // sw x0, 0(x4)

    0xfe0004e3, // beq x0, x0, loop1
  )

  val loadCounter = RegInit(0.U(10.W))
  when (loadCounter <= program.length.U) {
    loadCounter := loadCounter + 1.U
  }
  CPUreset := loadCounter === program.length.U
  for (i <- 0 until program.length) {
    when (loadCounter === i.U) {
      instMem.io.addr := (i << 2).U
      instMem.io.writeData := program(i).U
      instMem.io.write := true.B
    }
  }

  // // Debug
  // when (reset.asBool) {
  //   instMem.io.addr := 0.U
  //   instMem.io.writeData := 0x00002137.U // lui x2, 2
  //   instMem.io.write := true.B
  // } .elsewhen (RegNext(reset.asBool)) {
  //   instMem.io.addr := 4.U
  //   instMem.io.writeData := 0x00600093.U // addi x1, x0, 6
  //   instMem.io.write := true.B
  // } .elsewhen (RegNext(RegNext(reset.asBool))) {
  //   instMem.io.addr := 8.U
  //   instMem.io.writeData := 0x00112023.U // sw x1, 0(x2)
  //   instMem.io.write := true.B
  // }
}

object Top extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Top())
}
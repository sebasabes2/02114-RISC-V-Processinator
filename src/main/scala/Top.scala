import chisel3._
import chisel3.util._

class Top extends Module {
  val io = IO(new Bundle {
    val led = Output(Vec(16, Bool()))
  })

  val CPUreset = WireDefault(false.B) // Needed for FSM boot loader
  val CPU = withReset(RegNext(CPUreset)) { Module(new CPU()) }
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

  // val program = Array(
  //   0x00002137, // lui x2, 2
  //   0x00600093, // addi x1, x0, 6
  //   0x00112023  // sw x1, 0(x2)
  // )

  val program = Array (
    0x00000093, // li x1, 0        # loop index 
    0x006cf137, // li x2, 0x6cf000 # max loop
    0x00100193, // li x3, 1        # LED on
    0x00002237, // li x4, 0x2000   # LED address
                
                // loop 1:
    0x00108093, // addi x1, x1, 1
    0,
    0,
    0,
    0xfe20c8e3L, // blt x1, x2, loop1
    0,
    0,
    0,
    0x00322023, // sw x3, 0(x4)

                // loop 2:
    0xfff08093L, // addi x1, x1, -1
    0,
    0,
    0,
    0xfe1048e3L, // blt x0, x1, loop2
    0,
    0,
    0,
    0x00022023, // sw x0, 0(x4)

    0xfa000ce3L, // beq x0, x0, loop1
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
}

object Top extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Top())
}
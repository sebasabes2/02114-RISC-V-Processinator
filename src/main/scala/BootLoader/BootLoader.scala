import chisel3._
import chisel3.util._
import chisel.lib.uart._

class BootLoader() extends Module {
  val io = IO(new Bundle {
    val word = Input(UInt(32.W))
    val wordReady = Input(Bool())
    val addr = Output(UInt(32.W))
    val writeData = Output(UInt(32.W))
    val write = Output(Bool())
    val loading = Output(Bool())
  })

  val addr = WireDefault(0.U(32.W))
  val writeData = WireDefault(0.U(32.W))
  val write = WireDefault(false.B)
  
  object state extends ChiselEnum {
    val idle, getPointer, getInstruction = Value
  }
  import state._

  val startCode = 0x00017373.U
  val endCode = 0x00027373.U
  val writePointer = Reg(UInt(32.W))
  val fsm = RegInit(state.idle)
  switch (fsm) {
    is (idle) {
      when (io.wordReady && io.word === startCode) {
        fsm := getPointer
      }
    }
    is (getPointer) {
      when (io.wordReady) {
        writePointer := io.word
        fsm := getInstruction
      }
    }
    is (getInstruction) {
      when (io.wordReady) {
        when (io.word === endCode) {
          fsm := idle
        } .elsewhen (io.word === startCode) {
          fsm := getPointer
        } .otherwise {
          addr := writePointer
          writeData := io.word
          write := true.B
          writePointer := writePointer + 4.U
        }
      }
    }
  }

  io.addr := RegNext(addr)
  io.writeData := RegNext(writeData)
  io.write := RegNext(write)
  io.loading := RegNext(fsm =/= idle)
}
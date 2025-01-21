import chisel3._
import chisel3.util._

class DebounceBtn() extends Module {
  val io = IO(new Bundle {
    val btn = Input(Bool())
    val debounced = Output(Bool())
  })
  //sync
  val btnSync = RegNext(RegNext(io.btn))

  //debounce
  val CLOCK_FREQ = 100000000 // 100 MHz
  val DEBOUNCE_PERIOD = 20000 // 20 ns
  val DEBOUNCE_COUNTER_MAX = CLOCK_FREQ / 1000000 * DEBOUNCE_PERIOD
  val debounce_counter = RegInit(0.U(log2Up(DEBOUNCE_COUNTER_MAX).W))

  val btnDebounced = Reg(Bool())

  debounce_counter := debounce_counter + 1.U
  when(debounce_counter === (DEBOUNCE_COUNTER_MAX-1).U){
    btnDebounced := btnSync
  }

  //majority
  val shiftReg = RegInit(0.U(3.W))
  when(debounce_counter === (DEBOUNCE_COUNTER_MAX-1).U) {
    shiftReg := shiftReg(1, 0) ## btnDebounced
  }
  val btnClean = ( shiftReg (2) & shiftReg (1)) | ( shiftReg (2) & shiftReg (0)) | ( shiftReg (1) & shiftReg (0))

  io.debounced := btnClean
}

import chisel3._
import chisel3.util._
import lib.peripherals.MemoryMappedUart

class ButtonController(start: Int, size: Int) extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new Bus())
    val btn = Input(Vec(4, Bool()))
  })

  //BUS
  val width = log2Up(size)
  val page = io.bus.addr(31, width)

  //sync
  val btnSyncU = RegNext(RegNext(io.btn(0)))
  val btnSyncL = RegNext(RegNext(io.btn(1)))
  val btnSyncR = RegNext(RegNext(io.btn(2)))
  val btnSyncD = RegNext(RegNext(io.btn(3)))

  //debounce
  val CLOCK_FREQ = 100000000 // 100 MHz
  val DEBOUNCE_PERIOD = 20000 // 20 ns
  val DEBOUNCE_COUNTER_MAX = CLOCK_FREQ / 1000000 * DEBOUNCE_PERIOD
  val debounce_counter = RegInit(0.U(log2Up(DEBOUNCE_COUNTER_MAX).W))
  val tick = debounce_counter === (DEBOUNCE_COUNTER_MAX-1).U

  val btnDebouncedU = Reg(Bool())
  val btnDebouncedL = Reg(Bool())
  val btnDebouncedR = Reg(Bool())
  val btnDebouncedD = Reg(Bool())

  debounce_counter := debounce_counter + 1.U
  when(debounce_counter === (DEBOUNCE_COUNTER_MAX-1).U){
    debounce_counter := 0.U
    btnDebouncedU := btnSyncU
    btnDebouncedL := btnSyncL
    btnDebouncedR := btnSyncR
    btnDebouncedD := btnSyncD
  }

  val shiftRegU = RegInit(0.U(3.W))
  val shiftRegL = RegInit(0.U(3.W))
  val shiftRegR = RegInit(0.U(3.W))
  val shiftRegD = RegInit(0.U(3.W))

  when(debounce_counter === (DEBOUNCE_COUNTER_MAX-1).U) {
    shiftRegU := shiftRegU(1, 0) ## btnDebouncedU
    shiftRegL := shiftRegL(1, 0) ## btnDebouncedL
    shiftRegR := shiftRegR(1, 0) ## btnDebouncedR
    shiftRegD := shiftRegD(1, 0) ## btnDebouncedD
  }

  //majority

  //works btnSync, io.btn(x), btnDebounce
  //check shiftReg
  val btnCleanU = ( shiftRegU (2) & shiftRegU (1)) | ( shiftRegU (2) & shiftRegU (0)) | ( shiftRegU (1) & shiftRegU (0))
  val btnCleanL = ( shiftRegL (2) & shiftRegL (1)) | ( shiftRegL (2) & shiftRegL (0)) | ( shiftRegL (1) & shiftRegL (0))
  val btnCleanR = ( shiftRegR (2) & shiftRegR (1)) | ( shiftRegR (2) & shiftRegR (0)) | ( shiftRegR (1) & shiftRegR (0))
  val btnCleanD = ( shiftRegD (2) & shiftRegD (1)) | ( shiftRegD (2) & shiftRegD (0)) | ( shiftRegD (1) & shiftRegD (0))

  val valid = RegNext(page === (start/size).U)
  when (valid) {
    //                     4             3           2           1
    io.bus.readData := btnCleanL ## btnCleanD ## btnCleanR ## btnCleanU
  } .otherwise {
    io.bus.readData := 0.U
  }
  io.bus.readValid := valid
}




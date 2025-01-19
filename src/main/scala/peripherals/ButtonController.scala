import chisel3._
import chisel3.util._
import lib.peripherals.MemoryMappedUart

class ButtonController(start: Int, size: Int) extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new Bus())
    val btn = Input(Vec(4, Bool()))
  })

  val btnU = RegInit(false.B)
  val btnR = RegInit(false.B)
  val btnD = RegInit(false.B)
  val btnL = RegInit(false.B)





  when(io.btn(0)){
    btnU := true.B
  }
  when(io.btn(1)){
    btnL := true.B
  }
  when(io.btn(2)){
    btnR := true.B
  }
  when(io.btn(3)){
    btnD := true.B
  }


  val width = log2Up(size)
  val page = io.bus.addr(31, width)

  val valid = RegNext(page === (start/size).U)
  when (valid) {
    //                  4       3       2       1
    io.bus.readData := btnU ## btnR ## btnD ## btnL
    btnU := false.B
    btnR := false.B
    btnD := false.B
    btnL := false.B
  } .otherwise {
    io.bus.readData := 0.U
  }
  io.bus.readValid := valid
}




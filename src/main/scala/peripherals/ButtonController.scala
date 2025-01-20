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

  //btns
  val btnU = Module(new DebounceBtn)
  val btnL = Module(new DebounceBtn)
  val btnR = Module(new DebounceBtn)
  val btnD = Module(new DebounceBtn)
  btnU.io.btn := io.btn(0)
  btnL.io.btn := io.btn(1)
  btnR.io.btn := io.btn(2)
  btnD.io.btn := io.btn(3)

  //rising edge
  val risingEdgeU = RegInit(false.B)
  val risingEdgeR = RegInit(false.B)
  val risingEdgeL = RegInit(false.B)
  val risingEdgeD = RegInit(false.B)
  when(btnU.io.btn & !RegNext(btnU.io.btn)){
    risingEdgeU := true.B
  }
  when(btnR.io.btn & !RegNext(btnR.io.btn)){
    risingEdgeR := true.B
  }
  when(btnL.io.btn & !RegNext(btnL.io.btn)){
    risingEdgeL := true.B
  }
  when(btnD.io.btn & !RegNext(btnD.io.btn)){
    risingEdgeD := true.B
  }

  val valid = RegNext(page === (start/size).U)
  when (valid) {
    //                     4               3              2              1
    io.bus.readData := (btnL.io.btn ## btnD.io.btn ## btnR.io.btn ## btnU.io.btn
                     ## risingEdgeL ## risingEdgeD ## risingEdgeR ## risingEdgeU )
    risingEdgeU := false.B
    risingEdgeR := false.B
    risingEdgeL := false.B
    risingEdgeD := false.B

  } .otherwise {
    io.bus.readData := 0.U
  }
  io.bus.readValid := valid
}




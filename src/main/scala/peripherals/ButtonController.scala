import chisel3._
import chisel3.util._
import lib.peripherals.MemoryMappedUart

class ButtonController(start: Int, size: Int, freq: Int) extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new Bus())
    val btn = Input(Vec(8, Bool()))
  })

  //btns
  val btnU = Module(new DebounceBtn(freq))
  val btnL = Module(new DebounceBtn(freq))
  val btnR = Module(new DebounceBtn(freq))
  val btnD = Module(new DebounceBtn(freq))
  val btn0 = Module(new DebounceBtn(freq))
  val btn1 = Module(new DebounceBtn(freq))
  val btn2 = Module(new DebounceBtn(freq))
  val btn3 = Module(new DebounceBtn(freq))
  btnU.io.btn := io.btn(0)
  btnL.io.btn := io.btn(1)
  btnR.io.btn := io.btn(2)
  btnD.io.btn := io.btn(3)
  btn0.io.btn := io.btn(4)
  btn1.io.btn := io.btn(5)
  btn2.io.btn := io.btn(6)
  btn3.io.btn := io.btn(7)

  //rising edge
  val risingEdgeU = RegInit(false.B)
  val risingEdgeR = RegInit(false.B)
  val risingEdgeL = RegInit(false.B)
  val risingEdgeD = RegInit(false.B)
  val risingEdge0 = RegInit(false.B)
  val risingEdge1 = RegInit(false.B)
  val risingEdge2 = RegInit(false.B)
  val risingEdge3 = RegInit(false.B)
  when(btnU.io.debounced & !RegNext(btnU.io.debounced)){
    risingEdgeU := true.B
  }
  when(btnR.io.debounced & !RegNext(btnR.io.debounced)){
    risingEdgeR := true.B
  }
  when(btnL.io.debounced & !RegNext(btnL.io.debounced)){
    risingEdgeL := true.B
  }
  when(btnD.io.debounced & !RegNext(btnD.io.debounced)){
    risingEdgeD := true.B
  }
  when(btn0.io.debounced & !RegNext(btn0.io.debounced)){
    risingEdge0 := true.B
  }
  when(btn1.io.debounced & !RegNext(btn1.io.debounced)){
    risingEdge1 := true.B
  }
  when(btn2.io.debounced & !RegNext(btn2.io.debounced)){
    risingEdge2 := true.B
  }
  when(btn3.io.debounced & !RegNext(btn3.io.debounced)){
    risingEdge3 := true.B
  }
  
  //BUS
  val width = log2Up(size)
  val page = io.bus.addr(31, width)
  val index = io.bus.addr(width - 1,0)
  val valid = RegNext(page === (start/size).U && io.bus.read)
  when (valid) {
    when (RegNext(index(2))) {
      io.bus.readData := (btn3.io.debounced ## btn2.io.debounced ## btn1.io.debounced ## btn0.io.debounced
                       ## risingEdge3       ## risingEdge2       ## risingEdge1       ## risingEdge0 )
      risingEdge3 := false.B
      risingEdge2 := false.B
      risingEdge1 := false.B
      risingEdge0 := false.B
    } .otherwise {
      //                     4                    3                    2                    1
      io.bus.readData := (btnL.io.debounced ## btnD.io.debounced ## btnR.io.debounced ## btnU.io.debounced
                       ## risingEdgeL       ## risingEdgeD       ## risingEdgeR       ## risingEdgeU )
      risingEdgeU := false.B
      risingEdgeR := false.B
      risingEdgeL := false.B
      risingEdgeD := false.B
    }

  } .otherwise {
    io.bus.readData := 0.U
  }
  io.bus.readValid := valid
}




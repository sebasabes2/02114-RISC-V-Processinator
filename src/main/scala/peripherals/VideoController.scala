import chisel3._
import chisel3.util._

class VGA extends Bundle {
  val Red = Output(UInt(4.W))
  val Green = Output(UInt(4.W))
  val Blue = Output(UInt(4.W))
  val Hsync = Output(Bool())
  val Vsync = Output(Bool())
}

class VideoController(start: Int, size: Int, freq: Int) extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new Bus())
    val vga = new VGA()
  })

  val Y_WIDTH = 8
  val X_WIDTH = 9

  val MEM_SIZE = 1 << (Y_WIDTH + X_WIDTH)
  val mem = SyncReadMem(MEM_SIZE, UInt(6.W))

  val width = log2Up(size) // 20
  val page = io.bus.addr(31,width) // 0x1
  val index = io.bus.addr(width - 1,0) // 0
  val write = io.bus.writeWord | io.bus.writeHalf | io.bus.writeByte
  when (write && (page === (start/size).U)) {
    mem.write(index, io.bus.writeData(5,0))
  }

  io.bus.readValid := false.B
  io.bus.readData := 0.U

  //VGA parameters
  val VGA_H_DISPLAY_SIZE = 640
  val VGA_H_FRONT_PORCH_SIZE = 16
  val VGA_H_SYNC_PULSE_SIZE = 96
  val VGA_H_BACK_PORCH_SIZE = 48
  val VGA_V_DISPLAY_SIZE = 480
  val VGA_V_FRONT_PORCH_SIZE = 10
  val VGA_V_SYNC_PULSE_SIZE = 2
  val VGA_V_BACK_PORCH_SIZE = 33
  val SCALE_FACTOR = freq / 25000000;

  val VGA_H_TOTAL = VGA_H_DISPLAY_SIZE + VGA_H_FRONT_PORCH_SIZE + VGA_H_SYNC_PULSE_SIZE + VGA_H_BACK_PORCH_SIZE
  val VGA_V_TOTAL = VGA_V_DISPLAY_SIZE + VGA_V_FRONT_PORCH_SIZE + VGA_V_SYNC_PULSE_SIZE + VGA_V_BACK_PORCH_SIZE

  val ScaleCounterReg = Reg(UInt(log2Up(SCALE_FACTOR).W))
  val CounterXReg = Reg(UInt(10.W))
  val CounterYReg = Reg(UInt(10.W))

  when(ScaleCounterReg === (SCALE_FACTOR - 1).U) {
    ScaleCounterReg := 0.U
    when(CounterXReg === (VGA_H_TOTAL - 1).U) {
      CounterXReg := 0.U
      when(CounterYReg === (VGA_V_TOTAL - 1).U) {
        CounterYReg := 0.U
      }.otherwise {
        CounterYReg := CounterYReg + 1.U
      }
    }.otherwise {
      CounterXReg := CounterXReg + 1.U
    }
  }.otherwise {
    ScaleCounterReg := ScaleCounterReg + 1.U
  }

  val Hsync = (CounterXReg < (VGA_H_DISPLAY_SIZE + VGA_H_FRONT_PORCH_SIZE).U || (CounterXReg >= (VGA_H_DISPLAY_SIZE + VGA_H_FRONT_PORCH_SIZE + VGA_H_SYNC_PULSE_SIZE).U))
  val Vsync = (CounterYReg < (VGA_V_DISPLAY_SIZE + VGA_V_FRONT_PORCH_SIZE).U || (CounterYReg >= (VGA_V_DISPLAY_SIZE + VGA_V_FRONT_PORCH_SIZE + VGA_V_SYNC_PULSE_SIZE).U))

  val inDisplayArea = (CounterXReg < VGA_H_DISPLAY_SIZE.U) && (CounterYReg < VGA_V_DISPLAY_SIZE.U)
  val pixelX = CounterXReg(9,1)
  val pixelY = CounterYReg(9,1)

  val pixel = mem.read(pixelY(Y_WIDTH - 1,0) ## pixelX(X_WIDTH - 1,0))

  val red = Mux(RegNext(inDisplayArea), pixel(5,4) ## pixel(5,4), 0.U)
  val green = Mux(RegNext(inDisplayArea), pixel(3,2) ## pixel(3,2), 0.U)
  val blue = Mux(RegNext(inDisplayArea), pixel(1,0) ## pixel(1,0), 0.U)

  io.vga.Hsync := RegNext(RegNext(Hsync))
  io.vga.Vsync := RegNext(RegNext(Vsync))
  io.vga.Red := RegNext(red)
  io.vga.Green := RegNext(green)
  io.vga.Blue := RegNext(blue)
}
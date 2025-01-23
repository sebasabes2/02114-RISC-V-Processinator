import chisel3._
import chisel3.util._

class Bus extends Bundle {
  val addr = Output(UInt(32.W))
  val readData = Input(UInt(32.W))
  val readValid = Input(Bool())
  val writeData = Output(UInt(32.W))
  val writeWord = Output(Bool())
  val writeHalf = Output(Bool())
  val writeByte = Output(Bool())
}

class Memory(start: Int, size: Int) extends Module {
  val io = IO(Flipped(new Bus()))
  val mem0 = SyncReadMem(size/4, UInt(8.W))
  val mem1 = SyncReadMem(size/4, UInt(8.W))
  val mem2 = SyncReadMem(size/4, UInt(8.W))
  val mem3 = SyncReadMem(size/4, UInt(8.W))
  val width = log2Up(size)
  val page = io.addr(31,width)
  val index = io.addr(width - 1,2)
  val select = io.addr(1,0)
  val read0 = mem0.read(index)
  val read1 = mem1.read(index)
  val read2 = mem2.read(index)
  val read3 = mem3.read(index)




  // Read
  when (RegNext(page === (start/size).U)) {
    io.readData := 0.U
    switch (RegNext(select)){
      is(0.U){
        //io.readData := mem3.read(index) ## mem2.read(index) ## mem1.read(index) ## mem0.read(index)
        io.readData := read3 ## read2 ## read1 ## read0
      }
      is(1.U){
        //io.readData := mem0.read(index) ## mem3.read(index) ## mem2.read(index) ## mem1.read(index)
        io.readData := read0 ## read3 ## read2 ## read1
      }
      is(2.U){
        //io.readData := mem1.read(index) ## mem0.read(index) ## mem3.read(index) ## mem2.read(index)
        io.readData := read1 ## read0 ## read3 ## read2
      }
      is(3.U){
        //io.readData := mem2.read(index) ## mem1.read(index) ## mem0.read(index) ## mem3.read(index)
        io.readData := read2 ## read1 ## read0 ## read3
      }
    }
//    io.readData := mem3.read(index) ## mem2.read(index) ## mem1.read(index) ## mem0.read(index)
    io.readValid := true.B
  } .otherwise {
    io.readData := 0.U
    io.readValid := false.B
  }
  // Write
  when (page === (start/size).U) {
    when (io.writeWord) {
      mem0.write(index, io.writeData(7,0))
      mem1.write(index, io.writeData(15,8))
      mem2.write(index, io.writeData(23,16))
      mem3.write(index, io.writeData(31,24))
    } .elsewhen(io.writeHalf) {
      when (select(1)) {
        mem2.write(index, io.writeData(7,0))
        mem3.write(index, io.writeData(15,8))
      } .otherwise {
        mem0.write(index, io.writeData(7,0))
        mem1.write(index, io.writeData(15,8))
      }
    } .elsewhen (io.writeByte) {
      switch (select) {
        is (0.U) {
          mem0.write(index, io.writeData(7,0))
        }
        is (1.U) {
          mem1.write(index, io.writeData(7,0))
        }
        is (2.U) {
          mem2.write(index, io.writeData(7,0))
        }
        is (3.U) {
          mem3.write(index, io.writeData(7,0))
        }
      }
    }
  }
}

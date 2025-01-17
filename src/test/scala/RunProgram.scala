import chisel3._
import chiseltest._

object RunProgram {
  def apply(dut: CPU, program: Array[Int]) {
    var i = 0
    while (true) {
      val addr = dut.io.inst.addr.peekInt()
      dut.clock.step(1)
      val index = addr / 4
      if (index >= program.length) {
        // Finish remaining stages
        dut.io.inst.readData.poke(0x00000013.U) // nop
        dut.clock.step(5)
        return
      }
      dut.io.inst.readData.poke(program(index.intValue()).U)
      // If program is stuck
      i = i + 1
      if (i >= 100000) {
        println("Reached max clock cycles of " + i)
        return
      }
    }
  }
}

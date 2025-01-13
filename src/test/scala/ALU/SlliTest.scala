import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SlliTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU " should "pass" in {
    test(new CPU()) { dut =>
      dut.io.inst.readData.poke(0x07b00093) //addi x1 x0 123
      dut.clock.step(10)
      dut.io.reg(1).expect(123.U)
      dut.io.inst.readData.poke(0x00209113.U) //slli x2 x1 2
      dut.clock.step(10)
      dut.io.reg(2).expect(492.U)

      dut.io.inst.readData.poke(0xf8500093L) //addi x1 x0 -123
      dut.clock.step(10)
      //println("x3: "+ dut.io.reg(1).peekInt())
      dut.io.inst.readData.poke(0x00209113.U) //slli x2 x1 2
      dut.clock.step(10)
      //println("x3: "+ dut.io.reg(2).peekInt())
      dut.io.reg(2).expect(0xfffffe14L.U)


    }
  }
}

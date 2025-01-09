import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SltiTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU " should "pass" in {
    test(new CPU()) { dut =>
      dut.io.inst.readData.poke(0x07b00093) //addi x1 x0 123
      dut.clock.step(10)
      dut.io.reg(1).expect(123.U)

      dut.io.inst.readData.poke(0xf840a113.U) //slti x2 x1 -124
      dut.clock.step(10)
      dut.io.reg(2).expect(0.U)

      dut.io.inst.readData.poke(0x07c0a113.U) //slti x2 x1 124
      dut.clock.step(10)
      dut.io.reg(2).expect(1.U)
    }
  }
}

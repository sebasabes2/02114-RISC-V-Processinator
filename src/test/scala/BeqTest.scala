import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BeqTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU " should "pass" in {
    test(new CPU()) { dut =>
      dut.io.inst.readData.poke(0x07b00093.U) //addi x1 x0 123
      dut.clock.step(10)
      dut.io.reg(1).expect(123.U)

      dut.io.inst.readData.poke(0x07B00113.U) //addi x2 x0 123
      dut.clock.step(10)
      dut.io.reg(2).expect(123.U)

      dut.io.inst.readData.poke(0x02208063) //beq x1 x2 32
      dut.clock.step(1)
      dut.io.inst.readData.poke(0.U)
      dut.clock.step(9)
      dut.io.PC.expect(140.U)
    }
  }
}

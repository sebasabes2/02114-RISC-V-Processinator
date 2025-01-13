import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class JalTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU " should "pass" in {
    test(new CPU()) { dut =>
      dut.io.inst.readData.poke(0x00000093.U) //addi x1 x0 0
      dut.clock.step(4)
      dut.io.reg(1).expect(0.U)

      dut.io.inst.readData.poke(0x00000113.U) //addi x2 x0 0
      dut.clock.step(4)
      dut.io.reg(2).expect(0.U)

      dut.io.inst.readData.poke(0x00508093.U) //addi x1 x1 5
      dut.clock.step(1)
      dut.io.inst.readData.poke(0x00000013.U) //no op
      dut.clock.step(3)

      dut.io.inst.readData.poke(0x00210113.U) //addi x2 x0 2
      dut.clock.step(1)
      dut.io.inst.readData.poke(0x00000013.U) //no op
      dut.clock.step(3)
      dut.io.PC.expect(64)

      dut.io.inst.readData.poke(0xff9ff1efL.U) //jal x3 -8
      dut.clock.step(1)
      dut.io.inst.readData.poke(0x00000013.U) //no op
      dut.io.PC.expect(68)
      dut.clock.step(1)
      dut.io.PC.expect(52)
      dut.clock.step(3)
      println("x3: "+ dut.io.reg(3).peekInt())

    }
  }
}

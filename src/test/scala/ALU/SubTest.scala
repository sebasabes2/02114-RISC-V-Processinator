import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SubTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU " should "pass" in {
    test(new CPU()) { dut =>
      dut.io.inst.readData.poke(0x07b00093.U) //addi x1 x0 123
      dut.clock.step(10)
      dut.io.reg(1).expect(123.U)

      dut.io.inst.readData.poke(0x1c800113.U) //addi x2 x0 456
      dut.clock.step(10)
      dut.io.reg(2).expect(456.U)

      dut.io.inst.readData.poke(0x402081b3.U) //sub x3 x2 x1
      dut.clock.step(10)
      dut.io.reg(3).expect(4294966963L.U)
    }
  }
}

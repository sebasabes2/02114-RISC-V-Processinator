import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LuiTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "pass" in {
    test(new CPU()) { dut =>
      val lui = 0x00bc60b7.U // lui x1, 3014
      dut.io.inst.readData.poke(lui)
      dut.clock.step(10)
      
      val addi = 0x14e08113.U // addi x2, x1, 334
      dut.io.inst.readData.poke(addi)
      dut.clock.step(10)

      dut.io.reg(2).expect(12345678.U)
    }
  }
}

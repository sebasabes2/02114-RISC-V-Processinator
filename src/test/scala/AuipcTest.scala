import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AuipcTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "pass" in {
    test(new CPU()) { dut =>
      val add = 0x00f00093.U // addi x1, x0, 15
      dut.io.inst.readData.poke(add)
      dut.clock.step(1)
      val auipc = 0x0000c117.U // auipc x2, 12
      dut.io.inst.readData.poke(auipc)
      dut.clock.step(1)

      dut.io.inst.readData.poke(0.U)
      dut.clock.step(10)
      dut.io.reg(2).expect(0x0000c004.U)
    }
  }
}

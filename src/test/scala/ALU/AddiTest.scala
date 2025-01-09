import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AddiTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU " should "pass" in {
    test(new CPU()) { dut =>
      dut.io.inst.readData.poke(0x07b00093.U)
      dut.clock.step(10)
      dut.io.reg(1).expect(123.U)
    }
  }
}

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AuipcTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "pass" in {
    test(new CPU()) { dut =>
      val program = Array (
        0x00f00093, // addi x1, x0, 15
        0x0000c117, // auipc x2, 12
      )
      RunProgram(dut, program)
      dut.io.reg(2).expect(0x0000c004.U)
    }
  }
}

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ForwardingTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "pass" in {
    test(new CPU()) { dut =>
      val program = Array(
        0x06e00093, // addi, x1, x0, 110
        0x07b00113, // addi, x2, x0, 123
        0x002081b3, // add, x3, x1, x2
      )
      RunProgram(dut, program)
      dut.io.reg(3).expect(233.U)
    }
  }
}

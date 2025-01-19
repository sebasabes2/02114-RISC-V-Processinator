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
        0x00002203, // lw x4, 0(x0)
        0x00002283, // lw x5, 0(x0)
        0x00520333, // add x6, x4, x5

        // Possible infinite loop:
        0x0000a083, // lw x1, 0(x1)
        0x0000a083, // lw x1, 0(x1)
        0x01700393, // addi, x7, x0, 23
      )
      dut.io.data.readData.poke(67.U)
      RunProgram(dut, program)
      dut.io.reg(3).expect(233.U)
      dut.io.reg(6).expect(134.U)
      dut.io.reg(1).expect(67.U)
      dut.io.reg(7).expect(23.U)
    }
  }
}

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FlushingTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "pass" in {
    test(new CPU()) { dut =>
      val program = Array(
        0x00000863, // beq x0, x0, first

        0x00100093, // addi x1, x0, 1
        0x00100113, // addi x2, x0, 1
        0x00100193, // addi x3, x0, 1

                    // first:
        0x0100006f, // jal x0, second
        
        0x00100213, // addi x4, x0, 1
        0x00100293, // addi x5, x0, 1
        0x00100313, // addi x6, x0, 1

                    // second
        0x00000f97, // auipc x31, 0
        0x014f8067, // jalr x0, 20(x31)

        0x00100393, // addi x7, x0, 1
        0x00100413, // addi x8, x0, 1
        0x00100493, // addi x9, x0, 1
      )
      RunProgram(dut, program)
      for (i <- 1 to 9) {
        dut.io.reg(i).expect(0.U)
      }
    }
  }
}

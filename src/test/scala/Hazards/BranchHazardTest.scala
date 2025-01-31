import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BranchHazardTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "pass" in {
    test(new CPU()) { dut =>
      val program = Array(
        0x04100513, // addi x10 x0 65	li a0, 'A'
        0,
        0x00150513, // addi x10 x10 1	addi a0, a0, 1
        0x00150513, // addi x10 x10 1	addi a0, a0, 1
        0x00150513, // addi x10 x10 1	addi a0, a0, 1
        0x00050093, // addi x1 x10 0	mv x1, a0
        // 0x00000e63, // beq x0 x0 28	beq x0, x0, end
        0x00000063, // beq x0 x0 0	beq x0, x0, loop
        // 0x00000013, // addi x0 x0 0	nop # hazard
        0x06100513, // addi x10 x0 97	li a0, 'a'
        0xfe0002e3L, // beq x0 x0 -28	beq x0, x0, alternative
        0,
      )
      dut.io.startAddr.poke(28.U)
      dut.reset.poke(true.B)
      dut.clock.step(10)
      dut.reset.poke(false.B)

      RunProgram(dut, program)
      dut.io.reg(1).expect('d')
    }
  }
}

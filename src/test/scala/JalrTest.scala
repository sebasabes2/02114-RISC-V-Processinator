import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class JalrTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU " should "pass" in {
    test(new CPU()) { dut =>
      println("PC: "+ dut.io.PC.peekInt())
      dut.io.inst.readData.poke(0x01400113.U) //addi x2 x0 20
      dut.clock.step(1)
      dut.io.inst.readData.poke(0x00000013.U) //no op
      dut.clock.step(3)
      println("PC: "+ dut.io.PC.peekInt())
      println("x3: "+ dut.io.reg(3).peekInt())

      dut.io.inst.readData.poke(0x014101e7.U) //jalr x3 x2 20
      dut.clock.step(1)
      dut.io.inst.readData.poke(0x00000013.U) //no op
      println("PC: "+ dut.io.PC.peekInt())
      println("x3: "+ dut.io.reg(3).peekInt())
      dut.clock.step(1)
      println("PC: "+ dut.io.PC.peekInt())
      println("x3: "+ dut.io.reg(3).peekInt())
      dut.clock.step(1)
      println("PC: "+ dut.io.PC.peekInt())
      println("x3: "+ dut.io.reg(3).peekInt())
      dut.clock.step(1)
      println("PC: "+ dut.io.PC.peekInt())
      println("x3: "+ dut.io.reg(3).peekInt())
      dut.clock.step(1)
      println("PC: "+ dut.io.PC.peekInt())
      println("x3: "+ dut.io.reg(3).peekInt())

      //make expects and remove println

    }
  }
}

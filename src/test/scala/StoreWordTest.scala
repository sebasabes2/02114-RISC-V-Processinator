import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class StoreWordTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "pass" in {
    test(new CPU()) { dut =>
      val lui = 0xdeadc0b7l.U // li x1, 0xdeadbeef
      val addi = 0xeef08093l.U // li x1, 0xdeadbeef
      dut.io.inst.readData.poke(lui)
      dut.clock.step(1)
      dut.io.inst.readData.poke(0.U)
      dut.clock.step(10)
      dut.io.inst.readData.poke(addi)
      dut.clock.step(1)
      dut.io.inst.readData.poke(0.U)
      dut.clock.step(10)

      // See if x1 was loaded correctly
      dut.io.reg(1).expect(0xdeadbeefl.U)

      // Store Word
      dut.io.inst.readData.poke(0x00102523.U) // sw x1, 10(x0)
      dut.clock.step(1)
      dut.io.inst.readData.poke(0.U)
      // dut.clock.step(1)
      dut.io.data.addr.expect(10.U)
      dut.io.data.writeData.expect(0xdeadbeefl.U)
      dut.io.data.writeWord.expect(true.B)
      
      // Store Half Word
      dut.io.inst.readData.poke(0x00101523.U) // sh x1, 10(x0)
      dut.clock.step(1)
      dut.io.inst.readData.poke(0.U)
      // dut.clock.step(1)
      dut.io.data.addr.expect(10.U)
      dut.io.data.writeData.expect(0xdeadbeefl.U)
      dut.io.data.writeHalf.expect(true.B)
      
      // Store Byte
      dut.io.inst.readData.poke(0x00100523.U) // sb x1, 10(x0)
      dut.clock.step(1)
      dut.io.inst.readData.poke(0.U)
      // dut.clock.step(1)
      dut.io.data.addr.expect(10.U)
      dut.io.data.writeData.expect(0xdeadbeefl.U)
      dut.io.data.writeByte.expect(true.B)
    }
  }
}

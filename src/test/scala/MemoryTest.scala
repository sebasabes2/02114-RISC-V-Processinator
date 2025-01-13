import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MemoryTest extends AnyFlatSpec with ChiselScalatestTester {
  "Memory" should "pass" in {
    test(new Memory(0x0000, 0x1000)) { dut =>
      dut.io.writeData.poke(0xdeadbeefl)
      // Word
      dut.io.addr.poke(0) // 0
      dut.io.writeWord.poke(true.B)
      dut.clock.step(1)
      dut.io.writeWord.poke(false.B)
      // Half Word 
      dut.io.addr.poke(4) // 4
      dut.io.writeHalf.poke(true.B)
      dut.clock.step(1)
      dut.io.addr.poke(10) // 8
      dut.clock.step(1)
      dut.io.writeHalf.poke(false.B)
      // Byte
      dut.io.addr.poke(12) // 12
      dut.io.writeByte.poke(true.B)
      dut.clock.step(1)
      dut.io.addr.poke(17) // 16
      dut.clock.step(1)
      dut.io.addr.poke(22) // 20
      dut.clock.step(1)
      dut.io.addr.poke(27) // 24
      dut.clock.step(1)
      dut.io.writeByte.poke(false.B)

      dut.clock.step(10)

      // Check
      val expected = Array(0xdeadbeefl, 0xbeef, 0xbeef0000l, 0xef, 0xef00, 0xef0000, 0xef000000l)
      for (i <- 0 until expected.length) {
        dut.io.addr.poke(i*4)
        dut.clock.step(1)
        dut.io.readData.expect(expected(i))
        println("0x%04x".format(i*4) + ": " + "0x%08x".format(dut.io.readData.peekInt()))
      }
    }
  }
}

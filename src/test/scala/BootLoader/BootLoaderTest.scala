import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BootLoaderTest extends AnyFlatSpec with ChiselScalatestTester {
  "BootLoader" should "pass" in {
    test(new BootLoader) { dut =>
    
      def receive(instruction: UInt): Unit = {
        dut.io.word.poke(instruction)
        dut.clock.step(1)
        if (dut.io.write.peekBoolean()) {
          // println("0x%08x: 0x%08x".format(dut.io.addr.peekInt(), dut.io.writeData.peekInt()))
        } else {
          // println("wait")
        }
      }
      
      val startCode = 0x00017373.U
      val endCode = 0x00027373.U
      val sequence = Array(0x17.U, 0x183.U, startCode, 0x0.U, 0x378.U, 0x123.U, startCode, 0x1000.U, 0x389.U, endCode, 0x389.U, 0x23.U)
      
      dut.io.wordReady.poke(true.B)

      var valid = false
      for (i <- 0 until sequence.length) {
        receive(sequence(i))
        if (i >= 2 && sequence(i - 2) == startCode) {
          valid = true
        }
        if (sequence(i) == endCode || sequence(i) == startCode) {
          valid = false
        }
        if (valid) {
          dut.io.writeData.expect(sequence(i))
        } else {
          dut.io.write.expect(false.B)
        }
      }
    }
  }
}

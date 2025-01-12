import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WordUartTest extends AnyFlatSpec with ChiselScalatestTester {
  "WordUart" should "pass" in {
    test(new WordUart(1, 1)) { dut =>
      var count: Int = 0
      def step(steps: Int): Unit = {
        for (i <- 0 until steps) {
          dut.clock.step(1)
          if (dut.io.wordReady.peekBoolean()) {
            // println("%d: 0x%08x".format(count, dut.io.word.peekInt()))
          } else {
            // println(count + ": Invalid data")
          }
          count += 1
        }
      }

      def transmit(byte: Int): Unit = {
        dut.io.rx.poke(false.B)
        step(1)
        var x = byte
        for (i <- 0 until 8) {
          var bit = x & 1
          x = x >> 1
          dut.io.rx.poke(bit.B)
          step(1)
        }
      }

      dut.io.rx.poke(true.B)
      step(10)

      val values = Array(0x73, 0x73, 0x01, 0x00, 0x73, 0x73, 0x1, 0x00)
      for (i <- 0 until values.length) {
        transmit(values(i))
      }

      step(3)

      dut.io.wordReady.expect(true.B)
      dut.io.word.expect(0x00017373.U)
    }
  }
}

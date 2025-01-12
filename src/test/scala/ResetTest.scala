import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ResetTest extends AnyFlatSpec with ChiselScalatestTester {
  "Top" should "pass" in {
    test(new Top(10, 1)) { dut =>
      //println("Led value (start): " + dut.io.led(0).peekBoolean())
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(2)
      //println("Led value (2 steps): " + dut.io.led(0).peekBoolean())
      dut.clock.step(1)
      //println("Led value (3 steps): " + dut.io.led(0).peekBoolean())
//      dut.io.led(0).expect(true.B)
    }
  }
}

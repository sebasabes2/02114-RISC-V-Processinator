import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LEDTest extends AnyFlatSpec with ChiselScalatestTester {
  "Top" should "pass" in {
    test(new Top(10, 1)) { dut =>
      dut.clock.step(10)

      // Reset
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      dut.clock.step(10)

      // Run
      dut.clock.step(10)
      //dut.io.led(1).expect(true.B)
    }
  }
}

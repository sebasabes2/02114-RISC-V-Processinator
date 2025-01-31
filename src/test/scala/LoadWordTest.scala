import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LoadWordTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "pass" in {
    test(new CPU()) { dut =>
      // Fetch 
      dut.clock.step(1)
      // Decode
      val lw = 0x00a02083.U // lw x1, 10(x0)
      dut.io.inst.readData.poke(lw)
      dut.clock.step(1)
      dut.io.inst.readData.poke(0.U)

      for (i <- 0 until 10) {
        val dataAddr = dut.io.data.addr.peekInt()
        dut.clock.step(1)
        if (dataAddr == 10) {
          dut.io.data.readData.poke(30.U)
        } else {
          dut.io.data.readData.poke(0.U)
        }
      }

      dut.io.reg(1).expect(30.U)
    }
  }
}

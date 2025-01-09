import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class StoreWordTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "pass" in {
    test(new CPU()) { dut =>
      val li = 0x05900093.U // addi x1, x0, 89
      dut.io.inst.readData.poke(li)
      dut.clock.step(10)

      val sw = 0x00102523.U // sw x1, 10(x0)
      dut.io.inst.readData.poke(sw)
      dut.clock.step(1)
      dut.io.inst.readData.poke(0.U)

      dut.clock.step(1)
      dut.io.data.addr.expect(10.U)
      dut.io.data.writeData.expect(89.U)
      dut.io.data.write.expect(true.B)
    }
  }
}

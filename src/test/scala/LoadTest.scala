import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LoadTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "pass" in {
    test(new CPU()) { dut =>
      val lb = 0x00a00083.U // lb x1, 10(x0)
      val lh = 0x00a01083.U // lh x1, 10(x0)
      val lw = 0x00a02083.U // lw x1, 10(x0)
      val lbu =0x00a04083.U // lbu x1, 10(x0)
      val lhu =0x00a05083.U // lhu x1, 10(x0)

      val array = Array(lb,lh,lw,lbu,lhu)
      val expected = Array(4294967295L,4294967295L,131071,255,65535)
      for (j <- 0 until 5) {
        dut.clock.step(1)
        dut.io.inst.readData.poke(array(j))
        dut.clock.step(1)
        dut.io.inst.readData.poke(0.U)

        for( i <- 0 until 10){
          val dataAddr = dut.io.data.addr.peekInt()
          dut.clock.step(1)
          if (dataAddr == 10){
            dut.io.data.readData.poke(131071.U)
          }else {
            dut.io.data.readData.poke(0.U)
          }
        }
        //println("reg(1): " + dut.io.reg(1).peek())
        dut.io.reg(1).expect(expected(j))
      }
    }
  }
}

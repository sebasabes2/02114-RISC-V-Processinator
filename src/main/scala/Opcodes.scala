import chisel3._
import chisel3.util._

object Opcodes{
  val add    = 51.U
  val addi   = 19.U
  val load   = 3.U
  val store  = 35.U
  val branch = 99.U
  val jal    = 111.U
  val jalr   = 103.U
  val lui    = 55.U
  val auipc  = 23.U
  val ecall  = 115.U
}

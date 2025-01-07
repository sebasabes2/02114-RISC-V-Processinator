import chisel3._
import chisel3.util._

object Opcodes{
  val UPCadd    = 51.U
  val UPCaddi   = 19.U
  val UPCload   = 3.U
  val UPCstore  = 35.U
  val UPCbranch = 99.U
  val UPCjal    = 111.U
  val UPCjalr   = 113.U
  val UPClui    = 55.U
  val UPCauipc  = 23.U
  val UPCecall  = 115.U
}

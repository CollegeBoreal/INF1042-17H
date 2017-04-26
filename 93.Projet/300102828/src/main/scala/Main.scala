import caisse.Tax
import logic.board.Bits
object Main{
  def main(args: Array[String]): Unit = {
    println("la monait est:" + Tax.caisse(quantite = 2,prix = 5))
    println("la valeur est " + Bits.lastBit(Bits.BOTTOM_ROW))
  }
}






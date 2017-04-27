import caisse.Tax
import logic.board.Bits
object Main{
  def main(args: Array[String]): Unit = {
    println("la monait est:" + Tax.caisse(quantite = 2,prix = 5))
    println("la valeur est " + Bits.lastBit(Bits.BOTTOM_ROW))
    println("le nombre est " + Bits.count(Bits.CAPTURED))
    println("la valeur est " + Bits.at(1,9))
  }
}






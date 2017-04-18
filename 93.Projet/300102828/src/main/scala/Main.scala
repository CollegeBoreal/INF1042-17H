import caisse.Tax

object Main{
  def main(args: Array[String]): Unit = {
    println("le recu est:" + Tax.caisse(quantite = 2,prix = 5))
  }
}






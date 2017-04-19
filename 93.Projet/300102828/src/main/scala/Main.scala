import caisse.Tax

object Main{
  def main(args: Array[String]): Unit = {
    println("la monait est:" + Tax.caisse(quantite = 2,prix = 5))
  }
}






object Main{
  def main(args: Array[String]): Unit = {

  }
}
def caisse (quantite: Int, prix: Int): Unit = {
  val tax : Double = 0.13
  if(quantite == 0) return 0
  else return quantite*prix + tax*(quantite*prix)



}
println("le prix est:" + caisse)

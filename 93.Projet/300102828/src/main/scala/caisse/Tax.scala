package caisse

/**
  * Created by sowmdou on 2017-04-18.
  */
object Tax {
  def caisse(quantite: Int, prix: Int): Double = {
    val tax: Double = 0.13
   val retour = if (quantite == 0)  0
    else  quantite * prix + tax * (quantite * prix)
    val addition: Double = 20.3
    addition - retour
}

}
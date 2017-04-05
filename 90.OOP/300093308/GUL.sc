trait FormeGeometrique {
  def aire(): Int
}

class Carre(cote: Int) extends FormeGeometrique {
  def aire(): Int = cote*cote
}

val fan = new Carre(8)

class Rectangle(longeur: Int, largeur: Int) extends FormeGeometrique {
  def aire(): Int = longeur * largeur
}
  val salle = new Rectangle(10,11)

class Triangle(base: Int, hauteur: Int) extends FormeGeometrique {
  def aire(): Int = 1 / 2 * base * hauteur
}
  val piramide = new Triangle(6,8)

class Cercle(Pi: Int, rayon: Int) extends FormeGeometrique {
  def aire(): Int = Pi*rayon*rayon
}

// --TODO La fonction Math.PI doit entre convertie en Int
// --TODO La virgule en Anglais est remplacee par un point
val balle = new Cercle(Pi = Math.PI.toInt,4)

val aires = List[FormeGeometrique] (salle,fan,piramide,balle) // { --TODO Pourquoi des parentheses) BR

    aires.map(_.aire()).sum
// { --TODO Pourquoi des parentheses? BR









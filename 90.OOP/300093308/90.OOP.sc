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
val balle = new Cercle(Pi = 3,14,4)

val aires = List[FormeGeometrique] (piramide,balle) {
  aires.map(_.aire()).sum
}


val aires = List[FormeGeometrique] (salle,fan,piramide,balle) {

  aires.map(_.aire()).sum
}









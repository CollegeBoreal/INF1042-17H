
trait FormeGeometrique {
  def aire(): Int
}

class Carre(cote: Int) extends FormeGeometrique {
  def aire(): Int = cote*cote
}

val fan = new Carre(8)

class Rectangle(longueur: Int, largeur: Int) extends FormeGeometrique {
  def aire(): Int = longueur*largeur
}

val salle = new Rectangle(10,11)

val aires = List[FormeGeometrique](salle,fan)

aires.map(_.aire()).sum
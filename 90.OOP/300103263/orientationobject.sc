trait FormeGeometrique {
  def aire(): Int
}

class Carre(cote: Int) extends FormeGeometrique {
  def aire(): Int = cote * cote
}

val fan=new Carre(8)
class Rectangle(longeur:Int,largeur:Int) extends FormeGeometrique {
def aire():Int =longeur*largeur
}

val salle=new Rectangle(10*11)

val aires = list(FormeGeometrique)(salle,fan)


val fan=new cercle(5)
class cercle (rayon:Int) extends FormeGeometrique {
  def aire():Int =3.14*rayon*rayon
}
val fan=new triangle(5,4)
class triangle(base:Int,hauteur:Int) extends FormeGeometrique {
  def aire():Int=base*hauteur/2
}
val fan=somme aire`{

def somme():Int=aire(cercle)+aire(rectangle)+aire(triangle)+aire(carre)
  )
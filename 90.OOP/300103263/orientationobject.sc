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

// TODO le Rectangle comprend deux paramètres ou sont'ils?
val salle=new Rectangle(10,11)

// TODO La liste commence par une majuscule
val aires = List[FormeGeometrique](salle,fan)


// TODO Fan est déja défini ci-dessus
//val fan=new cercle(5)
val ballon=new Cercle(5)
// TODO Une classe par définition est en majuscule
class Cercle (rayon:Int) extends FormeGeometrique {
//  def aire():Int =3.14*rayon*rayon
// TODO Conversion en Int néscessaire
    def aire():Int =Math.PI.toInt*rayon*rayon
}
// TODO Fan est déja défini ci-dessus
//val fan=new triangle(5,4)
// TODO La liste commence par une majuscule
val triangle=new Triangle(5,4)
class Triangle(base:Int,hauteur:Int) extends FormeGeometrique {
  def aire():Int=base*hauteur/2
}


// TODO A quoi sert ce code?
//val fan=somme aire`{

// TODO voir correction
//def somme():Int=aire(cercle)+aire(rectangle)+aire(triangle)+aire(carre)
//  )
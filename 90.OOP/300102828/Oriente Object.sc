import java.awt.Rectangle
trait FormeGeometrique{
  def aire(): Int
}
class Carre(cote: Int) extends FormeGeometrique {
  def aire(): Int = cote * cote
}
val fan = new Carre(8)
class Rectangle(longueur: Int, largeur: Int) extends FormeGeometrique{
  def aire(): Int = longueur*largeur

}
val salle = new Rectangle(10,11)
class Triangle(base: Int, hauteur: Int)extends FormeGeometrique{
  def aire(): Int = (base*hauteur)/2
}
val panneau = new Triangle(4,3)
class Cercle(rayon:  Double) extends FormeGeometrique{

  def aire(): Double = Math.PI *(rayon*rayon)
}
val montre = new Cercle(2)
val aires = List[FormeGeometrique](fan,salle,panneau,montre)
aires.map(_.aire()).sum






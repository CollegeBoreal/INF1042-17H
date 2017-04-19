package formes

/**
  * Created by rasso on 2017-04-18.
  */


import java.awt.Rectangle
trait FormeGeometrique{
  def aire(): Int
}
class Carre(cote: Int) extends FormeGeometrique {
  def aire(): Int = cote * cote
}
def volume(): Int =(cote*cote*cote)
val fan = new Carre(8)
class Rectangle(longueur: Int, largeur: Int,hauteur:Int) extends FormeGeometrique{
  def aire(): Int = longueur*largeur

}
def volume(): Int =(longueur*largeur*hauteur)
val salle = new Rectangle(10,11)
class Triangle(base: Int, hauteur: Int)extends FormeGeometrique{
  def aire(): Int = (base*hauteur)/2
}
def volume(): Int =(aire*hauteur)/3
val panneau = new Triangle(4,3)

class Cercle(rayon:  Double) extends FormeGeometrique{

  def aire(): Double = Math.PI *(rayon*rayon)
  class Cercle(rayon: Double) extends FormeGeometrique{
    //  def aire(): Double = 3.14*(rayon*rayon)
    /
    def aire(): Int = (Math.PI*(rayon*rayon)).toInt
    >>>>>>> 3a77401f277a5fbc6837d2f145b500f4cba0b801
  }
  def volume(): Int=(4/3*3.14)*(rayon*rayon*rayon)
  val montre = new Cercle(2)
  val volume = List[FormeGeometrique](fan,salle,panneau,montre)
  volume.map(_.volume()).sum





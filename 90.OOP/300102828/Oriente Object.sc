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
<<<<<<< HEAD
class Cercle(rayon:  Double) extends FormeGeometrique{

  def aire(): Double = Math.PI *(rayon*rayon)
=======
class Cercle(rayon: Double) extends FormeGeometrique{
//  def aire(): Double = 3.14*(rayon*rayon)
  //--TODO l'override doit etre de la meme signature
  //--TODO Il faut donc convertir en Int
    def aire(): Int = (Math.PI*(rayon*rayon)).toInt
>>>>>>> 3a77401f277a5fbc6837d2f145b500f4cba0b801
}
val montre = new Cercle(2)
val aires = List[FormeGeometrique](fan,salle,panneau,montre)
aires.map(_.aire()).sum






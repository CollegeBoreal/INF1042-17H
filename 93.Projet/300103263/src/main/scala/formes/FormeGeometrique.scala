
package formes

/**
  * Created by rasso on 2017-04-18.
  */



trait FormeGeometrique{
  def aire(): Int
}
class Carre(cote: Int) extends FormeGeometrique {
  def aire(): Int = cote * cote
  def volume(): Int =cote *cote*cote

 class Rectangle((longeur: Int )(largeur: Int) (hauteur: Int) extends FormeGeometrique   {
  def volume (): Int = longeur * largeur * hauteur
}
  }

  }

  class Rectangle(longueur: Int, largeur: Int, hauteur: Int) extends FormeGeometrique{
    def volume(): Int = longueur*largeur*hauteur

  }
  class Triangle(base: Int, hauteur: Int)extends FormeGeometrique{
    def volume(): Int = (base*hauteur)/2*(hauteur/3)
  }


  class Cercle(rayon:  Double) extends FormeGeometrique{

    def aire(): Double = Math.PI *(rayon*rayon)

    class Cercle(rayon: Double) extends FormeGeometrique{
      //  def aire(): Double = 3.14*(rayon*rayon)
      def aire(): Int = (Math.PI*(rayon*rayon)).toInt

    }


    val volume = List[FormeGeometrique](fan,salle,panneau,montre)
    volume.map(_.volume()).sum



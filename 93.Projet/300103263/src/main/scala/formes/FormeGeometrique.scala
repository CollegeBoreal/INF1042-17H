
package formes

/**
  * Created by rasso on 2017-04-18.
  */



trait FormeGeometrique{
  def volume(): Int
}
class Carre(cote: Int) extends FormeGeometrique {

  def volume(): Int =cote *cote*cote
}





  class Rectangle(longueur: Int, largeur: Int, hauteur: Int) extends FormeGeometrique{
    def volume(): Int = longueur*largeur*hauteur

  }
  class Triangle(base: Int, hauteur: Int)extends FormeGeometrique{
    def volume(): Int = (base*hauteur)/2*(hauteur/3)
  }


  class Cercle(rayon:  Double) extends FormeGeometrique {


    class Cercle(rayon: Double) extends FormeGeometrique
    {
      def volume(): Int = (4 * Math.PI * (rayon * rayon)) toInt
    }








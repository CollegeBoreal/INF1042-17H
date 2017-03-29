package geometrie

/**
  * Created by User on 2016-05-18.
  */
class Point(var x:Int , var y :Int ) {

  def translation (dx:Int,dy: Int): Unit = {
    x = x + dx
    y = y + dy
  }

 override def toString():String = s"($x,$y)"
}

// Singleton
object Figure {
  def ligne (a : Point, b: Point):( Point,Point )= (a,b)

}

 abstract class Figure {

  def aire ( ):Int

 }

class Carre (cote :Int) extends Figure {
  def aire(): Int = cote * cote

}
class Rectangle(long:Int,largeur :Int) extends Figure {
  def aire(): Int = long * largeur

}

 class Cercle (rayon:Int) extends Figure {
  def aire () :Int  = math.Pi.toInt*rayon*rayon


}

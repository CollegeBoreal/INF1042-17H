import com.sun.javafx.geom.Rectangle
import formes.Carre

object Main {
  def main(args: Array[String]): Unit = {
    println("hello, world") {
    val fan = new Carre(8)
    println(fan.volume()
    val salle = new Rectangle(10, 14, 11)
    println(salle.volume())
    val panneau = new Triangle(4, 3, 7)
    println(panneau.volume())
    val montre = new Cercle(2)
    println(montre.volume())
    val volume = List[FormeGeometrique](fan, salle, panneau, montre)
    volume.map(_.volume()).sum
  }
  




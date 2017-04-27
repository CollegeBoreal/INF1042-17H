import formes._

object Main {
  def main(args: Array[String]): Unit = {

      val fan = new Carre(8)
      println(fan.volume())
      val salle = new Rectangle(10, 14, 11)
      println(salle.volume())
      val panneau = new Triangle(4, 7)
      println(panneau.volume())
      val montre = new Cercle(2)
      println(montre.volume())
      val volume = List[FormeGeometrique](fan, salle, panneau, montre)
      val somme=       volume.map(_.volume()).sum
      println (somme)


  }
}



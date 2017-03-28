// tester
import week7._

object Solutions {

  def main(args: Array[String]): Unit = {
    // 9oz et 4oz verres
    val problem = new WaterPouring(Vector(9,4))

    // Solution si la solution recherchee est de 6oz
//    problem.solutions(6).foreach(println)

    problem.solutions(7).foreach(println)

  }

}
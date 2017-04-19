object Main {
  def main(args: Array[String]): Unit = {
val resultat = (Tri.tri_bulle(tableau = Array(4,5,6,3,1)))
    resultat.foreach(x => print(x))
  }
}
object Tri{
  def tri_bulle(tableau: Array[Int]): Array [Int]= {
    Array(1,2,5,3,6)
  }
}
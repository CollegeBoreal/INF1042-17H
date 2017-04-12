import mathematique.Math

object Main {
  def main(args: Array[String]): Unit = {
    println("Bonjour Monde\n")
    val a: Int = 4
    val b: Int = 2
    println(a +"/"+b +"=" + Math.eucledien(a,b))
  }
}


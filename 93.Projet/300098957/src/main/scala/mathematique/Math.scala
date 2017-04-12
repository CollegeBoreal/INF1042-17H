package mathematique

/**
  * Created by to-boreal on 12-04-17.
  */
object Math {

  def eucledien(a: Int, b:Int): Int = {
    if (b == 0) a
    else
      eucledien(b,a % b)
  }
}

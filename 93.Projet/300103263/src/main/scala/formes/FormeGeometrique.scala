
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
}

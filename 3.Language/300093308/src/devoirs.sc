
def addition(x:Int,y: Int): Int = {
  if (y==0) return 0
  x+y
}
addition(2,0)

val xs= List(1,2,3)

def additionL(xs:List[Int]): Int = {

//  xs.foldLeft(0)((a,b) => a+b)
//  xs.foldLeft(0)(_+_)
xs.sum

}
additionL(xs)

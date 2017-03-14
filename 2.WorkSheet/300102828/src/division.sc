def divide(x: Int , y: Int):(Int,Int) = {
   if (x ==0) 0
   val (q,r)= divide(x/2,y)
//  val q=2*q
//  val r=2*r
//   if (x%2!=0) r+1
//   if (r>=y) r-y,=q+1

  val (q1,r1) =
  if (x%2!=0) (2*q,2*r+1)
  else (2*q,2*r)

  if (r1>=y) (q+1,r-y)
  else (q1,r1)
}



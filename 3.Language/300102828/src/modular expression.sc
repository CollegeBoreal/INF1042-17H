
def modexp( x: Int , y: Int ,N: Int): Int = {
  if(y==0) return 1
  val z =modexp(x,y/2,N)
  if(y%2==0)  z*z%N
  else  x*z*z%N



}


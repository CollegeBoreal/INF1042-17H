def multiply(x: Double ,y: Double): Double =   {
  if(y==0)return 0
  val z=multiply(x ,y/2)
  if(y/2==0) 2*z
  else  x+2*z
}

multiply(2,2)
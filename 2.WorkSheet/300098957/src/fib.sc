type N = Int

def fib(n: N):N = {
  if (n==0) 0
  else if (n==1) 1
  else fib(n-1) + fib(n - 2)
}

fib(1)


fib(2)


fib(20)

def memoize[I, O](f: I => O): I => O = new scala.collection.mutable.HashMap[I, O]() {self =>
  override def apply(key: I) = self.synchronized(getOrElseUpdate(key, f(key)))
}

lazy val fibM: Int => BigInt = memoize {
  case 0 => 0
  case 1 => 1
  case n => fibM(n-1) + fibM(n-2)
}


fibM(100)



// http://www.luigip.com/?p=200

def fibs: Stream[BigInt] = 0 #:: fibs.scanLeft(BigInt(1))(_ + _)

fibs(100)



def memoize[I, O](f: I => O): I => O = new scala.collection.mutable.HashMap[I, O]() {self =>
  override def apply(key: I) = self.synchronized(getOrElseUpdate(key, f(key)))
}

lazy val fibM: Int => BigInt = memoize {
  case 0 => 0
  case 1 => 1
  case n => fibM(n-1) + fibM(n-2)
}


fibM(100)



// http://www.luigip.com/?p=200

def fibs: Stream[BigInt] = 0 #:: fibs.scanLeft(BigInt(1))(_ + _)

fibs(100)


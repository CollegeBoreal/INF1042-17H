object  ApplicationTriBulle
{
//  val table  =  Array[Int](1,5,2,7,9,4,5,6)  ;  // le tableau è trier en attribut

/*
  static void  TriBulle  ( )    {
    int  n  =  table.length - 1 ;
    for (  int  i  =  n ;  i >= 1 ;  i -- )
    for (  int  j  =  2 ;  j <= i ;  j ++ )
    if ( table[j - 1]  >  table[j] )
    {
      int  temp  =  table[j - 1] ;
      table[j - 1]  =  table[j] ;
      table[j]  =  temp ;
    }
  }
*/

/*
  static void  Impression  ( )   {
    // Affichage du tableau
    int  n  =  table.length - 1 ;
    for (  int  i  =  1 ;  i <= n ;  i ++ )
    System.out.print ( table[i] + " , ");
    System.out.println ();
  }
*/

  def Initialisation( ): Seq[Double] = {
    // remplissage aléatoire du tableau

    (1 to 10).map( x => Math.random () * 100 )

  }

  def main(args: Array[String]): Unit = {
    val tableau = Initialisation  ( );
    println ("Taille du Tableau initial :", tableau.size);
//    Impression  ( );
//    TriBulle  ( );
    println ("Tableau une fois trié :");
//    Impression  ( );
  }
}
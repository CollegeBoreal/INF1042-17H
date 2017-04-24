class  ApplicationTriBulle
{
  static  int [] table  =  new  int [10]  ;  // le tableau è trier en attribut

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

  static void  Impression  ( )   {
    // Affichage du tableau
    int  n  =  table.length - 1 ;
    for (  int  i  =  1 ;  i <= n ;  i ++ )
    System.out.print ( table[i] + " , ");
    System.out.println ();
  }

  static void  Initialisation   ( )    {
    // remplissage aléatoire du tableau
    int  n  =  table.length - 1 ;
    for (  int  i  =  1 ;  i <= n ;  i ++ )
    table[i]  =  ( int )( Math.random () * 100 );
  }

  public static void  main ( String [ ] args )    {
    Initialisation  ( );
    System.out.println ("Tableau initial :");
    Impression  ( );
    TriBulle  ( );
    System.out.println ("Tableau une fois trié :");
    Impression  ( );
  }
}
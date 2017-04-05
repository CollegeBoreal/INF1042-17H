
import patmat.Huffman.{Bit, Fork, Leaf}

/**
  * Created by boreal-300098957 on 17-04-04.
  */
object Main {

  def main(args: Array[String]) {

    /*
    * A Huffman coding tree for the French language.
    * Generated from the data given at
    *   http://fr.wikipedia.org/wiki/Fr%C3%A9quence_d%27apparition_des_lettres_en_fran%C3%A7ais
    */


    val secret: List[Bit] = List(1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1)

    println("À l'aide du dictionnaire Francais ")
    println("Décoder le message secret suivant: ",secret)

    /**
      * Écrire une fonction qui retourne le code secret
      */
    abstract class CodeTree
    case class Fork(left: CodeTree, right: CodeTree, chars: List[Char], weight: Int) extends CodeTree
    case class Leaf(char: Char, weight: Int) extends CodeTree


    type Bit = Int

    def decode(tree: CodeTree, bits: List[Bit]): List[Char] = decode0(tree, bits, tree)
    def decode0(tree: CodeTree, bits: List[Bit], t: CodeTree): List[Char] = t match {
      case l : Leaf => l.char::decode0(tree, bits, tree)
      case f : Fork =>
        if (bits.isEmpty) List() else {
          val c = if (bits.head==0) f.left else f.right
          decode0(tree, bits.tail, c)
        }
    }

    val frenchCode: CodeTree = Fork(
      Fork(
        Fork(
          Leaf('s',121895),
          Fork(
            Leaf('d',56269),
            Fork(
              Fork(
                Fork(
                  Leaf('x',5928),
                  Leaf('j',8351)
                  ,List('x','j'),14279)
                ,
                Leaf('f',16351)
                ,List('x','j','f'),30630)
              ,
              Fork(
                Fork(
                  Fork(
                    Fork(
                      Leaf('z',2093),
                      Fork(
                        Leaf('k',745),Leaf('w',1747),List('k','w'),2492),List('z','k','w'),4585),
                    Leaf('y',4725),List('z','k','w','y'),9310),
                  Leaf('h',11298),List('z','k','w','y','h'),20608),
                Leaf('q',20889),List('z','k','w','y','h','q'),41497),List('x','j','f','z','k','w','y','h','q'),72127),List('d','x','j','f','z','k','w','y','h','q'),128396),List('s','d','x','j','f','z','k','w','y','h','q'),250291),Fork(Fork(Leaf('o',82762),Leaf('l',83668),List('o','l'),166430),
          Fork(
            Fork(
              Leaf('m',45521),
              Leaf('p',46335),
              List('m','p'),91856),
            Leaf('u',96785),List('m','p','u'),188641),List('o','l','m','p','u'),355071),List('s','d','x','j','f','z','k','w','y','h','q','o','l','m','p','u'),605362),
      Fork(
        Fork(
          Fork(
            Leaf('r',100500),
            Fork(
              Leaf('c',50003),
              Fork(
                Leaf('v',24975),
                Fork(
                  Leaf('g',13288),
                  Leaf('b',13822),
                  List('g','b'),27110
                )
                ,List('v','g','b'),52085)
              ,List('c','v','g','b'),102088)
            ,List('r','c','v','g','b'),202588)
          , Fork(
            Leaf('n',108812),Leaf('t',111103),List('n','t'),219915),List('r','c','v','g','b','n','t'),422503),

        Fork(
          Leaf('e',225947),
          Fork(Leaf('i',115465),Leaf('a',117110),List('i','a'),232575), List('e','i','a'),458522)
        ,List('r','c','v','g','b','n','t','e','i','a'),881025)
      ,List('s','d','x','j','f','z','k','w','y','h','q','o','l','m','p','u','r','c','v','g','b','n','t','e','i','a'),1486387)

    def decodedSecret: List[Char] = decode(frenchCode, secret)
    println("le secret est:" + decodedSecret)


  }
}

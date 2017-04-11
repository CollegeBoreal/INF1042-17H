
import com.sun.xml.internal.bind.v2.TODO
import patmat.Huffman
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
    println("Décoder le message secret suivant: ", secret)

    /**
      * Écrire une fonction qui retourne le code secret
      */
    abstract class CodeTree
    case class Fork(left: CodeTree, right: CodeTree, chars: List[Char], weight: Int) extends CodeTree
    case class Leaf(char: Char, weight: Int) extends CodeTree


    val decodedSecret: List[Char] = Huffman.decode(Huffman.frenchCode, secret)

    println("le secret est:" + decodedSecret)

    val clear: List[Char] = List('r', 'a', 's', 's', 'o', 'u', 'l')
   val encodedClear = Huffman.encode(Huffman.frenchCode)( clear)

    println("le code est:" + encodedClear )
  }
}






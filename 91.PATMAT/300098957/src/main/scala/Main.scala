
import langues.Langues
import patmat.Huffman
import patmat.Huffman.Bit

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
    def decodedSecret: List[Char] = Huffman.decode(Langues.frenchCode,secret)

    println("le secret est:" + decodedSecret)

  }
}
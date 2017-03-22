package week7

/**
  * Created by to-boreal on 2017-01-26.
  */
class WaterPouring(capacities: Vector[Int]) {

/*
 * Inventaire
 */

  // Index des verres
  val glasses = 0 until capacities.length

  // Etats
  type State = Vector[Int]

  // Verser
  // Types de deplacement
  trait Pouring {
    def change(state: State): State
  }

  case class Empty(glass: Int) extends Pouring {
    def change(state: State): State =
      state updated (glass,0)
  }

  case class Fill(glass: Int) extends Pouring {
    def change(state: State): State =
      state updated (glass,capacities(glass))
  }

  case class Transfer(from: Int, to: Int) extends Pouring {
    def change(state: State): State = {
      val amount = state(from) min capacities(to) - state(to)
      state updated (from, state(from) - amount) updated (to, state(to) + amount)
    }
  }

  // Pouring Possibilities
  val successors =
    (for (g <- glasses) yield Empty(g)) ++
      (for (g <- glasses) yield Fill(g))  ++
      (for (from <- glasses; to <- glasses if from != to) yield Transfer(from,to))

  // Path
  // Node
  class Sequence(val finalState: State)(history: List[Pouring]) {
    def +(pour: Pouring) = new Sequence(pour change finalState)(pour :: history)
    override def toString = (history.reverse mkString " , ") + " state==> " + finalState
  }
  // Graph Search
  def search(explored: Set[State]): Set[Sequence] => Stream[Set[Sequence]] = sequences => {
    if (sequences.isEmpty) Stream.Empty
    else {
      val frontier = for {
        sequence <- sequences
        node <- successors map (sequence +)
        if !(explored contains node.finalState)
      } yield node
      val explorations =  explored ++ ( frontier map (_.finalState))
      sequences #:: search(explorations)(frontier)
    }
  }

  // Setting up the graph
  // initial Collection(state)
  val initialState = capacities map (x => 0)
  val allSequences = search(Set(initialState))(Set(new Sequence(initialState)(Nil)))

  def solutions(goal: Int): Stream[Sequence] =
    for {
      sequence <- allSequences
      node <- sequence
      if node.finalState contains goal
    } yield node

}

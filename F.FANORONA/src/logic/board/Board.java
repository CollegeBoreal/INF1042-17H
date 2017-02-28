package logic.board;

import logic.engine.EndgameDatabase;
import logic.engine.Eval;
import logic.engine.Hash;
import logic.engine.MoveGenerator;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// Position representation and alpha-beta search

public class Board {
	static final boolean PVS = true;
	// Flag to enable or disable principal variation search. In this
	// modification to
	// alpha-beta, once we have one line with a reasonable evaluation, we search
	// the other
	// lines with a zero-width window, and then search again (with a normal
	// window)
	// any lines that don't fail low.

	static final boolean IID = true;
	static final int IID_PLY = 25;
	static final int IID_LIMIT = 55;
	// Internal iterated deepening: if we have no bestMove, and depth is at
	// least
	// the limit, try a recursive search to depth-ply to get a good first move.

	static final int MIN_HASH_DEPTH = 15;
	// Avoid hashing beneath this depth, not worth the time

	public static final int PLY = 10; // unit for fractional search extensions
	static final int FORCED_MOVE_EXTENSION = 5; // forced move is interesting
												// but not odd
	static final int FORCED_CAPTURE_EXTENSION = 10; // forced capture might be
													// sac, b v careful
	static final int ENDGAME_CAPTURE_EXTENSION = 5; // endgame capture
													// interesting but not odd
	static final int FORCED_ENDGAME_CAPTURE = 10; // forced in endgame
	static final int MULTIPLE_CAPTURE_EXTENSION = 7; // extend multiple-capture
														// sequences
	static final int EARLY_PASS_EXTENSION = 10; // extend when pass looks better
												// than capture

	public static final int WON_POSITION = 10000; // multiplied by no pieces on
													// board at win
	static final int DECREMENTABLE = 5000; // big enough to use PLY_DECREMENT
	public static final int PLY_DECREMENT = 1; // make far-away win look worse
												// than nearer

	static Hash hash = new Hash(32768); // make a hash table for the search
	public static EndgameDatabase endgameDatabase = null; // don't make db
															// except
															// application
															// version

	public Board previousPosition;
	public long myPieces, opponentPieces;
	public long alreadyVisited; // moves already part of capture sequence

	public volatile static int sequenceNumber = 0; // check value for early
													// termination of search

	// Search results
	public int evaluation; // numerical value of current position
	public long bestMove; // move giving that numerical value
	public Board principalVariation; // the Board resulting from that move
	public Board child; // the Board we're currently searching
	public MoveGenerator moveGenerator; // the Move Generator used by this board
	public boolean forced; // only one move exists -- used to determine
							// extensions
	public boolean hasPrincipalVariation; // does principalVariation point to
											// anything useful?

	// type of evaluation -- upper bound on true value, lower bound, or exact
	// for communication between search and hash table
	static public final int EVAL_UPPER_BOUND = 0;
	static public final int EVAL_LOWER_BOUND = 1;
	static public final int EVAL_EXACT = 2;

	// search statistics
	public static long nodeCount; // for callers of alphabeta to collect stats
	public static long leafCount;
	public static final boolean COLLECT_EXTRA_STATISTICS = false;
	public static long endgameEvalCount;
	public static long boardConsCount;
	public static long moveGenConsCount;
	public static long pvChangeCount;

	// bunch o' quick booleans
	public final boolean whiteToMove() { // is white the player on move?
		return (myPieces & Bits.IS_WHITE) != 0;
	}

	public final boolean midCapture() { // was last move a capture?
		return myPieces < 0;
	}

	public final boolean wasPass() { // was last move a pass? (opponent didn't
										// move)
		return (myPieces ^ opponentPieces) < 0;
	}

	public final boolean wasShuffle() { // opponent moved but didn't capture?
		return opponentPieces >= 0;
	}

	public final boolean gameOver() { // has game finished?
		return (myPieces & Bits.ON_BOARD) == 0
				|| (opponentPieces & Bits.ON_BOARD) == 0
				|| !(new MoveGenerator(this)).hasMoreElements();
	}

	public final boolean whiteWins() { // if so, is player on move the winner?
		return !(whiteToMove() ^ ((opponentPieces & Bits.ON_BOARD) == 0));
	}

	// make initial board position
	public Board() {
		previousPosition = null;
		myPieces = Bits.INITIAL_BOT | Bits.IS_WHITE;
		opponentPieces = Bits.INITIAL_TOP;
		alreadyVisited = 0;
	}

	public Board(boolean blackAtTop, boolean whiteGoesFirst) {
		previousPosition = null;
		boolean ImOnTop = (blackAtTop ^ whiteGoesFirst);
		if (ImOnTop) {
			myPieces = Bits.INITIAL_TOP;
			opponentPieces = Bits.INITIAL_BOT;
		} else {
			myPieces = Bits.INITIAL_BOT;
			opponentPieces = Bits.INITIAL_TOP;
		}
		if (whiteGoesFirst)
			myPieces |= Bits.IS_WHITE;
		else
			opponentPieces |= Bits.IS_WHITE;
		alreadyVisited = 0;
	}

	// Make board for position after move
	// Arg is bitboard of changed piece positions
	public Board(Board prev, long move) {
		previousPosition = prev;
		long captures = prev.opponentPieces & move;
		if (captures != 0) { // capturing anything?
			opponentPieces = (prev.opponentPieces ^ captures) | Bits.CAPTURED;
			move ^= captures;
			alreadyVisited = prev.alreadyVisited | move;
			myPieces = (prev.myPieces ^ move) | Bits.CAPTURED;
		} else {
			opponentPieces = prev.myPieces ^ move;
			myPieces = prev.opponentPieces & ~Bits.CAPTURED;
			alreadyVisited = 0;
		}
	}

	// Create a child position for the given move.
	// The logic here should match Board(Board,long).
	public final void setChild(long move) {
		if (child == null) {
			child = new Board(this, move);
			if (COLLECT_EXTRA_STATISTICS)
				boardConsCount++;
		} else {
			long captures = opponentPieces & move;
			if (captures != 0) { // capturing anything?
				child.opponentPieces = (opponentPieces ^ captures)
						| Bits.CAPTURED;
				move ^= captures;
				child.alreadyVisited = alreadyVisited | move;
				child.myPieces = (myPieces ^ move) | Bits.CAPTURED;
			} else {
				child.opponentPieces = myPieces ^ move;
				child.myPieces = opponentPieces & ~Bits.CAPTURED;
				child.alreadyVisited = 0;
			}
		}
		child.bestMove = -1L;
	}

	// set up to generate moves available from this position
	public final void setMoveGenerator() {
		if (moveGenerator == null) {
			moveGenerator = new MoveGenerator(this);
			if (COLLECT_EXTRA_STATISTICS)
				moveGenConsCount++;
		} else
			moveGenerator.reset(this);
	}

	// Set up a move as the principal variation of a position
	// assumes that it is already in child
	//
	// This routine is key to our strategy of avoiding too many memory
	// allocations.
	// During the search, the set of Board objects we create has a comb-like
	// structure,
	// in which the objects in a linked list formed by child pointers each have
	// dangling
	// off them another linked list formed by principalVariation pointers; the
	// size of
	// this comb is quadratic in the search depth (therefore small compared to
	// the whole
	// search tree). This routine maintains this comb-like structure, while
	// changing
	// the principal variation of a node to a new line found while searching the
	// child,
	// by switching the positions of the node's child and principalVariation
	// pointers.
	//
	public final void setPrincipalVariation() {
		if (COLLECT_EXTRA_STATISTICS)
			pvChangeCount++;
		if (principalVariation == null) {
			principalVariation = child;
			child = principalVariation.child;
			if (child != null)
				child.previousPosition = this;
		} else {
			Board temp = child;
			child = principalVariation;
			principalVariation = temp;
			child.child = principalVariation.child;
			if (child.child != null)
				child.child.previousPosition = child;
		}
		principalVariation.child = null;
		hasPrincipalVariation = true;
	}

	// search; sets principalVariation and evaluation
	// search gets aborted whenever sequenceNumber != Board.sequenceNumber
	public final void alphaBeta(int depth, int alpha, int beta,
			int sequenceNumber) {
		nodeCount++;
		hasPrincipalVariation = false;
		int hashKey = -1;

		if (myPieces >= 0) { // if (!midCapture())

			// Time for a leaf evaluation?
			if (depth <= 0 && Eval.evaluate(this, alpha, beta, depth)) {
				leafCount++;
				return;
			}

			// Transposition Table
			// See if we've already hashed the results of searching this
			// position
			hashKey = hash.hashKey(this);
			if (hash.getHash(this, hashKey, alpha, beta, depth)) {
				if (bestMove >= 0 && evaluation >= alpha && evaluation <= beta) {
					setChild(bestMove);
					child.hasPrincipalVariation = false;
					setPrincipalVariation();
				}
				return;
			}
		}

		// Check if aborting (after leaf eval and hashing to speed up
		// non-aborted search)
		if (sequenceNumber != Board.sequenceNumber)
			return;

		// POSITION IS NOT TERMINAL AND NOT HASHED. WE HAVE TO DO A RECURSIVE
		// SEARCH...

		// Find first move to be searched
		long move;
		boolean moveGeneratorIsSet = false;
		if (bestMove >= 0)
			move = bestMove; // history heuristic
		else if (IID && depth >= IID_LIMIT) { // internal iterated deepening
			alphaBeta(depth - IID_PLY, alpha, beta, sequenceNumber);
			move = bestMove;
		} else {
			setMoveGenerator(); // make move generator now we know we need one
			moveGeneratorIsSet = true;
			move = moveGenerator.nextElement();
			forced = !(moveGenerator.hasMoreElements());
		}
		long firstMove = move;

		// Compute extensions
		int newDepth = depth - PLY;
		int captureExtension = 0;
		if (forced) {
			newDepth += FORCED_MOVE_EXTENSION;
			if (opponentPieces >= 0)
				captureExtension = FORCED_ENDGAME_CAPTURE
						- FORCED_MOVE_EXTENSION;
			else
				captureExtension = FORCED_CAPTURE_EXTENSION
						- FORCED_MOVE_EXTENSION;
		} else if (opponentPieces >= 0) // if (wasShuffle())
			captureExtension = ENDGAME_CAPTURE_EXTENSION;
		else if (myPieces < 0) // if (midCapture())
			captureExtension = MULTIPLE_CAPTURE_EXTENSION;

		// Set up alpha-beta parameters
		evaluation = -Integer.MAX_VALUE;
		int evalType = EVAL_UPPER_BOUND; // assume u.b. until we reach eval >
											// alpha
		int pvsBeta = beta;

		// MAIN ALPHA-BETA LOOP
		while (move >= 0) {
			setChild(move);

			// Call alpha beta recursively to evaluate child position
			int moveEval;
			if (child.myPieces >= 0) { // if (!child.midCapture())
				child.alphaBeta(newDepth, -pvsBeta, -alpha, sequenceNumber);
				moveEval = -child.evaluation;
				if (moveEval >= pvsBeta && moveEval < beta) {
					child.alphaBeta(newDepth, -beta, -alpha, sequenceNumber);
					moveEval = -child.evaluation;
				}
				if (moveEval > evaluation && move == 0 && !forced) {
					// we think a pass is the best move? try again a little
					// deeper,
					// it looks suspiciously like a horizon effect delaying
					// tactic
					child.alphaBeta(newDepth + EARLY_PASS_EXTENSION, -beta,
							-alpha, sequenceNumber);
					moveEval = -child.evaluation;
				}
			} else if ((child.opponentPieces & Bits.ON_BOARD) != 0) {
				// disable pvs for capture nodes, it only makes sense for when
				// turn ends
				child.alphaBeta(newDepth + captureExtension, alpha, beta,
						sequenceNumber);
				moveEval = child.evaluation;
			} else { // all pieces captured, fail high but hash exact eval
				evaluation = Bits.count(myPieces & Bits.ON_BOARD)
						* WON_POSITION;
				evalType = EVAL_EXACT;
				bestMove = move;
				child.hasPrincipalVariation = false;
				setPrincipalVariation();
				break;
			}

			// Test if move is best so far, and if so how it compares to alpha
			// and beta
			if (moveEval > evaluation) {
				bestMove = move;
				evaluation = moveEval;
				if (moveEval > alpha) {
					alpha = moveEval;
					if (moveEval >= beta) {
						evalType = EVAL_LOWER_BOUND;
						break; // fail high, prune search by breaking from loop
					}
					evalType = EVAL_EXACT;
					setPrincipalVariation();
				}
			}

			// Prepare for next iteration by getting next move
			if (forced)
				break;
			if (!moveGeneratorIsSet) {
				setMoveGenerator();
				moveGeneratorIsSet = true;
			}
			move = moveGenerator.nextElement();
			if (move == firstMove)
				move = moveGenerator.nextElement();

			// Apply principal variation search: make artificially narrow window
			// for searches after the first, then widen when necessary.
			// But this gets screwed up by PLY_DECREMENT so don't use it in that
			// case.
			if (PVS && alpha < DECREMENTABLE && -alpha > -DECREMENTABLE)
				pvsBeta = alpha + 1;
		}

		// here after the main loop. adjust eval; hash the position for the next
		// time we see it.
		if (sequenceNumber != Board.sequenceNumber)
			return; // don't hash trash
		if (evaluation > DECREMENTABLE)
			evaluation -= PLY_DECREMENT;
		else if (evaluation < -DECREMENTABLE)
			evaluation += PLY_DECREMENT;
		if (hashKey >= 0)
			hash.setHash(this, hashKey, evalType, depth);
	}
}

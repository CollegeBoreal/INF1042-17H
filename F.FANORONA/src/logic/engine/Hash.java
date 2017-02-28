package logic.engine;

// Game of Fanorona
// David Eppstein, UC Irvine, 20 Jun 1997
//
// Hash table
//
// Note this only hashes current piece positions, not history.
// In particular, it doesn't hash the sequence of recent captures, so it's only safe to
// hash positions that are not midCapture().  This should be ok since the hashtable
// should mainly kick in towards the endgame when captures are less frequent.
//
// Since this is the most space-consuming part of the program
// (and to avoid the hassle of allocating an individual object per hash table member)
// we use an array of longs rather than of objects.  Each hash table entry consists
// of four longs:
//		table[4*key] = myPieces
//		table[4*key+1] = opponentPieces
//		table[4*key+2] = bestMove
//		table[4*key+3] = (evalType << 57) + (forced << 56) + (evalDepth << 32) + evaluation
//
// (Note extensions can be negative, the rest are unsigned.)

import java.util.Random;

import logic.board.Board;

public class Hash {
	static Random hasher = new Random();

	public static final boolean COLLECT_STATISTICS = true;
	public static int hits;
	public static int misses;
	public static int shallow;
	public static int badBound;

	public static final int DEPTH_ADJUSTMENT = 40; // hack to make neg depth
													// work

	private int hashSize;
	private long table[] = null;

	public Hash(int hs) {
		// silently round hash size to power of two
		hashSize = 1;
		while ((1 << hashSize) < hs)
			hashSize++;
		table = new long[1 << (hashSize + 2)];
	}

	// find hash table index for board position
	// hex values from DiscreteMath`Combinatorica`DeBruijnSequence[{0,1},7]
	public final int hashKey(Board b) {
		long n = b.myPieces * 0x9910602f496efed1L + b.opponentPieces
				* 0xb3ac55ca1f3c7538L;
		return (int) (n >>> (64 - hashSize));
		// long n = (b.myPieces - b.opponentPieces);
		// return (int) ((n * 0x1121400305112141L) >>> (64 - hashSize));
	}

	// perform hash lookup.
	// sets Board.evaluation, Board.evalDepth, Board.bestMove, Board.extensions
	// returns true on successful lookup, false otherwise
	// (but a false return may still end up changing bestMove and extensions).
	//
	public final boolean getHash(Board board, int key, int alpha, int beta,
			int depth) {
		key <<= 2;
		if (table[key] == board.myPieces
				&& table[key + 1] == board.opponentPieces) {
			board.bestMove = table[key + 2];
			long info = table[key + 3];
			board.forced = ((info & (1L << 56)) != 0);
			int evalType = (int) (info >>> 57) & 0xff;
			int evalDepth = ((int) (info >>> 32) & 0x00ffff) - DEPTH_ADJUSTMENT;
			int evaluation = (int) info;

			if (evalDepth < depth) {
				if (COLLECT_STATISTICS)
					shallow++;
			} else if (evalType == Board.EVAL_EXACT
					|| (evalType == Board.EVAL_UPPER_BOUND && evaluation <= alpha)
					|| (evalType == Board.EVAL_LOWER_BOUND && evaluation >= beta)) {
				board.evaluation = evaluation;
				if (COLLECT_STATISTICS)
					hits++;
				return true;
			} else if (COLLECT_STATISTICS)
				badBound++;
		} else if (COLLECT_STATISTICS)
			misses++;
		return false;
	}

	// store board in hash table for future lookup
	public final void setHash(Board board, int key, int evalType, int depth) {
		depth += DEPTH_ADJUSTMENT;
		if (depth < 0)
			return;
		key <<= 2;
		table[key] = board.myPieces;
		table[key + 1] = board.opponentPieces;
		table[key + 2] = board.bestMove;
		evalType <<= 1;
		if (board.forced)
			evalType |= 1;
		table[key + 3] = ((long) evalType << 56) + ((long) depth << 32)
				+ (board.evaluation & 0xffffffffL);
	}
}
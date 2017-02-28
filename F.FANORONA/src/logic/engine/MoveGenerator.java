package logic.engine;

import logic.board.Bits;
import logic.board.Board;

public class MoveGenerator // implements Enumeration (not really)
{
	// The board we're generating moves from and various derived quantities
	Board board; // board we are moving from
	long storedFrom; // positions we can move from
	long storedTo; // positions we can move to

	long movesV, movesH, movesS, movesB; // positions we can move to ignoring
											// capturability

	// what kind of move we're making and which pieces can move that way
	int moveSetIndex; // where we are in the list
	long set; // set of positions this moveSet is working on

	// what kind of move is represented by the bits in set
	int captureType;
	static final int CAPTURE_FORWARD = 0;
	static final int CAPTURE_BACKWARD = 1;
	static final int NO_CAPTURE = 2;
	static final int PASS = 3;
	static final int NO_MORE_MOVES = 4;
	boolean madeCapture;
	int shift; // which direction pieces are moving in current move set

	// The main routine to find sets of moves
	// nextElement then pulls off the moves from these sets one bit at a time.
	//
	// This is really not a subroutine but a coroutine: each time we call it we
	// want
	// to continue at the statement immediately after the point at which we
	// returned
	// from the previous call. Since Java doesn't have coroutines built-in, we
	// use
	// moveSetIndex as a program counter, with a big switch statement marking
	// all the
	// different possible entry points, but (unlike the usual switch) falling
	// through
	// from case to case rather than having any break statements.
	//
	private final void findNextSet() {
		long from = storedFrom;
		long to = storedTo;
		long target = board.opponentPieces;
		switch (++moveSetIndex) {
		// CASES 0-7: CAPTURES
		case 0: // CAPTURE FORWARD VERTICALLY OR BACKWARD -VERTICALLY
			captureType = CAPTURE_FORWARD;
			movesV = (from & (to >>> Bits.SHIFT_VERTICAL))
					| (to & (from >>> Bits.SHIFT_VERTICAL));
			if ((set = (movesV & (target >>> 2 * Bits.SHIFT_VERTICAL))) != 0) {
				shift = Bits.SHIFT_VERTICAL;
				madeCapture = true;
				return;
			}
			++moveSetIndex; // fall into...
		case 1: // CAPTURE FORWARD HORIZONTALLY OR BACKWARD -HORIZONTALLY
			movesH = (from & (to >>> Bits.SHIFT_HORIZONTAL))
					| (to & (from >>> Bits.SHIFT_HORIZONTAL));
			if ((set = (movesH & (target >>> 2 * Bits.SHIFT_HORIZONTAL))) != 0) {
				shift = Bits.SHIFT_HORIZONTAL;
				madeCapture = true;
				return;
			}
			++moveSetIndex; // fall into...
		case 2: // CAPTURE FORWARD SLANTLY OR BACKWARD -SLANTLY
			storedFrom = (from &= Bits.DIAGONAL);
			movesS = (from & (to >>> Bits.SHIFT_SLANT))
					| (to & (from >>> Bits.SHIFT_SLANT));
			if ((set = (movesS & (target >>> 2 * Bits.SHIFT_SLANT))) != 0) {
				shift = Bits.SHIFT_SLANT;
				madeCapture = true;
				return;
			}
			++moveSetIndex; // fall into...
		case 3: // CAPTURE FORWARD BACKSLANTLY OR BACKWARD -BACKSLANTLY
			movesB = (from & (to >>> Bits.SHIFT_BACKSLANT))
					| (to & (from >>> Bits.SHIFT_BACKSLANT));
			if ((set = (movesB & (target >>> 2 * Bits.SHIFT_BACKSLANT))) != 0) {
				shift = Bits.SHIFT_BACKSLANT;
				madeCapture = true;
				return;
			}
			++moveSetIndex; // fall into...
		case 4: // CAPTURE FORWARD -VERTICALLY OR BACKWARD VERTICALLY
			captureType = CAPTURE_BACKWARD;
			if ((set = (movesV & (target << Bits.SHIFT_VERTICAL))) != 0) {
				shift = Bits.SHIFT_VERTICAL;
				madeCapture = true;
				return;
			}
			++moveSetIndex; // fall into...
		case 5: // CAPTURE FORWARD -HORIZONTALLY OR BACKWARD HORIZONTALLY
			if ((set = (movesH & (target << Bits.SHIFT_HORIZONTAL))) != 0) {
				shift = Bits.SHIFT_HORIZONTAL;
				madeCapture = true;
				return;
			}
			++moveSetIndex; // fall into...
		case 6: // CAPTURE FORWARD -SLANTLY OR BACKWARD SLANTLY
			if ((set = (movesS & (target << Bits.SHIFT_SLANT))) != 0) {
				shift = Bits.SHIFT_SLANT;
				madeCapture = true;
				return;
			}
			++moveSetIndex; // fall into...
		case 7: // CAPTURE FORWARD -BACKSLANTLY OR BACKWARD BACKSLANTLY
			if ((set = (movesB & (target << Bits.SHIFT_BACKSLANT))) != 0) {
				shift = Bits.SHIFT_BACKSLANT;
				madeCapture = true;
				return;
			}
			++moveSetIndex; // fall into...

			// CASES 8-11: SHUFFLES
		case 8: // VERTICAL SHUFFLE OR PASS
			if (board.midCapture()) { // illegal to shuffle?
				captureType = PASS;
				moveSetIndex = 11; // set up so next call to findset runs into
									// case 12
				set = 1; // return a set with one move in it
				return;
			} else if (madeCapture) { // capture was forced?
				moveSetIndex = 11; // move to case 12, end of moves
				captureType = NO_MORE_MOVES;
				return;
			}
			captureType = NO_CAPTURE;
			if ((set = movesV) != 0) {
				shift = Bits.SHIFT_VERTICAL;
				return;
			}
			++moveSetIndex; // fall into...
		case 9: // HORIZONTAL SHUFFLE
			if ((set = movesH) != 0) {
				shift = Bits.SHIFT_HORIZONTAL;
				return;
			}
			++moveSetIndex; // fall into...
		case 10: // SLANT SHUFFLE
			if ((set = movesS) != 0) {
				shift = Bits.SHIFT_SLANT;
				return;
			}
			++moveSetIndex; // fall into...
		case 11: // BACKSLANT SHUFFLE
			if ((set = movesB) != 0) {
				shift = Bits.SHIFT_BACKSLANT;
				return;
			}
			++moveSetIndex; // fall into...

			// CASE 12: RAN OUT OF SHUFFLES
		case 12:
			moveSetIndex--; // stay in this case and always return zero
			captureType = NO_MORE_MOVES;
			return;

		default:
			throw new IllegalArgumentException();
		}
	}

	// is there a capturing move available?
	// should be called on a newly created MoveGenerator
	public final boolean hasCapture() {
		if (set == 0)
			findNextSet();
		return (captureType == CAPTURE_FORWARD || captureType == CAPTURE_BACKWARD);
	}

	// initialize and set up first position
	public MoveGenerator(Board b) {
		reset(b);
	}

	public void reset(Board b) {
		board = b;
		moveSetIndex = -1;
		set = 0;
		madeCapture = false;

		// Find positions we can move from and to.
		// At start of move they are just occupied and empty positions, but in
		// midCapture
		// we restrict from to the piece just moved and to to places it can
		// legally go.
		long myPieces = b.myPieces;

		// Narrow down target positions for midcapture moves to avoid our
		// previous positions
		if (b.midCapture()) {
			long move = myPieces ^ b.previousPosition.myPieces;
			storedFrom = myPieces & move; // Only allow same piece to move
			if ((move & (move << Bits.SHIFT_VERTICAL)) != 0)
				move = (move << Bits.SHIFT_VERTICAL)
						| (move >>> Bits.SHIFT_VERTICAL);
			else if ((move & (move << Bits.SHIFT_HORIZONTAL)) != 0)
				move = (move << Bits.SHIFT_HORIZONTAL)
						| (move >>> Bits.SHIFT_HORIZONTAL);
			else if ((move & (move << Bits.SHIFT_SLANT)) != 0)
				move = (move << Bits.SHIFT_SLANT) | (move >>> Bits.SHIFT_SLANT);
			else
				// if ((move & (move << Bits.SHIFT_BACKSLANT)) != 0)
				move = (move << Bits.SHIFT_BACKSLANT)
						| (move >>> Bits.SHIFT_BACKSLANT);
			storedTo = Bits.ON_BOARD
					& ~(move | myPieces | b.opponentPieces | b.alreadyVisited);
			return;
		} else {
			storedFrom = myPieces;
			storedTo = Bits.ON_BOARD & ~(myPieces | b.opponentPieces);
			return;
		}
	}

	// are there any ungenerated moves?
	public final boolean hasMoreElements() {
		if (set == 0)
			findNextSet();
		return (captureType != NO_MORE_MOVES);
	}

	// find next move in sequence by pulling bits out of set
	public final long nextElement() {
		if (set == 0)
			findNextSet(); // make sure we have a move to generate
		long bit = set;
		bit &= -bit; // bit = Bits.lastBit(set)
		set ^= bit;
		switch (captureType) {
		case CAPTURE_FORWARD:
			long retval = bit | (bit << shift);
			bit <<= 2 * shift;
			while ((bit & board.opponentPieces) != 0) {
				retval |= bit;
				bit <<= shift;
			}
			return retval;

		case CAPTURE_BACKWARD:
			retval = bit | (bit << shift);
			bit >>>= shift;
			while ((bit & board.opponentPieces) != 0) {
				retval |= bit;
				bit >>>= shift;
			}
			return retval;

		case NO_CAPTURE:
			return bit | (bit << shift);

		case PASS:
			return 0;

		case NO_MORE_MOVES:
		}
		return -1L;
	}

	// find an arbitrary move in a position
	static int arbitraryMoveIndex = 0;

	static public final Board arbitraryMove(Board board) {
		int i = arbitraryMoveIndex; // remember which move we want
		arbitraryMoveIndex++; // and next time take the following move instead
		MoveGenerator moveGenerator = new MoveGenerator(board);
		while (moveGenerator.hasMoreElements()) { // loop through moves taking
													// i'th
			long move = moveGenerator.nextElement();
			if (--i < 0)
				return new Board(board, move);
		}
		arbitraryMoveIndex = 1; // ran out, had to return 0'th, return 1'st next
								// time
		return new Board(board, new MoveGenerator(board).nextElement());
	}
}

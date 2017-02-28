package logic.engine;

import logic.board.Bits;
import logic.board.Board;

public abstract class Eval {
	// For which squares does the ability to move there make a piece
	// non-trapped?
	static final long ACTIVE_SQUARES = 002503763774774124L;

	// Terms for broken-fortress evaluation
	static final int ATTACK_WEIGHT = 600; // value of a successful attack
	static final int TRAPPED_PIECE_WEIGHT = 100; // value of a trapped piece
	static final int CONVERSION_WEIGHT = 35; // bonus for converting to simpler
												// pos
	static final int MAX_POSITIONAL_EVAL = 50; // expected max of next two terms
	static final int FORWARD_WEIGHT = 10; // value of a piece in the attacking
											// zone
	static final int SPACE_WEIGHT = 1; // penalty for allowing defense
										// liebensraum

	// Terms for eval of relatively even positions
	static final int PIECE_WEIGHT = 300; // multiplier for material ratio
	static final int PIECE_VALUE = 100; // add this number to ratio of active
										// posn
	static final int ATTACK_BONUS = 50; // one side attacking but cant break
										// fort
	static final int ratios[] = ratios(); // table of precomputed ratios

	static final int[] ratios() {
		int[] r = new int[64];
		r[0] = Integer.MAX_VALUE;
		for (int i = 1; i < 64; i++)
			r[i] = PIECE_WEIGHT / i;
		return r;
	}

	// Extra depths to search if a capture is available on this or the next ply
	static final int CAPTURE_DEPTH = -30;
	static final int THREAT_DEPTH = -15;

	// Bit masks for finding batteries
	static final long LEFT_MARGIN = 006003401400700300L; // must be clear for
															// battery
	static final long LEFT_BATTERY = 000000000001000400L; // must be set in
															// battery test
	static final long RIGHT_MARGIN = 000140160030034006L; // must be clear for
															// battery
	static final long RIGHT_BATTERY = 000000000000002001L; // must be set in
															// battery test

	// Stuff for finding fortresses
	static final long SM_LEFT_FORT = 010006003001400400L;
	static final long SM_LEFT_GUARD = 000003000400600000L;
	static final long SM_LEFT_ATTACK = 003000000200000140L;
	static final long LG_LEFT_FORT = 016007403601700700L;
	static final long LG_LEFT_GUARD = 000000600700140000L;
	static final long LG_LEFT_ATTACK = 000600000040000030L;
	static final long SM_RIGHT_FORT = 000020030014006001L;
	static final long SM_RIGHT_GUARD = 000000020020004000L;
	static final long SM_RIGHT_ATTACK = 000300000020000014L;
	static final long LG_RIGHT_FORT = 000160170074036007L;
	static final long LG_RIGHT_GUARD = 000000300160060000L;
	static final long LG_RIGHT_ATTACK = 001400000200000060L;

	// Which squares are attacked by a given set of pieces moving to a given set
	// of squares?
	// (Note caller must mask for Bits.ON_BOARD)
	static final long attack(long attackers, long open) {
		long moves = ((attackers >>> Bits.SHIFT_VERTICAL) & open)
				| ((open >>> Bits.SHIFT_VERTICAL) & attackers);
		long unsafe = (moves >>> Bits.SHIFT_VERTICAL)
				| (moves << (2 * Bits.SHIFT_VERTICAL));
		moves = ((attackers >>> Bits.SHIFT_HORIZONTAL) & open)
				| ((open >>> Bits.SHIFT_HORIZONTAL) & attackers);
		unsafe |= (moves >>> Bits.SHIFT_HORIZONTAL)
				| (moves << (2 * Bits.SHIFT_HORIZONTAL));
		attackers &= Bits.DIAGONAL;
		moves = ((attackers >>> Bits.SHIFT_SLANT) & open)
				| ((open >>> Bits.SHIFT_SLANT) & attackers);
		unsafe |= (moves >>> Bits.SHIFT_SLANT)
				| (moves << (2 * Bits.SHIFT_SLANT));
		moves = ((attackers >>> Bits.SHIFT_BACKSLANT) & open)
				| ((open >>> Bits.SHIFT_BACKSLANT) & attackers);
		return unsafe | (moves >>> Bits.SHIFT_BACKSLANT)
				| (moves << (2 * Bits.SHIFT_BACKSLANT));
	}

	// Which squares are adjacent to a given set of squares?
	// (Note caller must mask for Bits.ON_BOARD)
	static final long nextTo(long s) {
		long n = (s >>> Bits.SHIFT_VERTICAL) | (s << Bits.SHIFT_VERTICAL);
		s |= (n & ~Bits.DIAGONAL);
		return n | (s >>> Bits.SHIFT_HORIZONTAL) | (s << Bits.SHIFT_HORIZONTAL);
	}

	// Which pieces are active or easily activated?
	//
	// Active pieces are those which can move safely, and are either on a good
	// square
	// (one with at least four neighbors) now or could be on such a square after
	// a safe move.
	// We also count as active pieces that are trapped on a good square, but
	// could be untrapped by the movement of another active piece.
	//
	static final long activity(long pieces, long safeMoves) {
		long act = (ACTIVE_SQUARES & nextTo(safeMoves))
				| nextTo(ACTIVE_SQUARES & safeMoves);
		return (act | (ACTIVE_SQUARES & nextTo(act & pieces))) & pieces;
	}

	// Which parts of the board have the strong squares controlled by one
	// player?
	public static final long DIAGONAL = 012522522524524525L;
	public static final long LEFT_CONTROL = 000002400400500000L;
	public static final long RIGHT_CONTROL = 000000500100120000L;
	public static final long CENTER_CONTROL = 000000120020024000L;

	// Entry from other places than alpha-beta search
	public static final boolean evaluate(Board b) {
		return evaluate(b, -Integer.MAX_VALUE, Integer.MAX_VALUE,
				-Integer.MAX_VALUE);
	}

	// Evaluation function itself
	// Returns true with eval in b.evaluation, or false if depth is too high
	public static final boolean evaluate(Board b, int alpha, int beta, int depth) {
		long myPieces = b.myPieces & Bits.ON_BOARD;
		long opponentPieces = b.opponentPieces & Bits.ON_BOARD;

		int myPieceCount = Bits.count(myPieces);
		int oppPieceCount = Bits.count(opponentPieces);

		// check for endgame database hits
		// here rather than in search so it takes less time (we already have
		// piece counts)
		if (myPieceCount <= 2 && oppPieceCount <= 2
				&& Board.endgameDatabase != null && Board.endgameDatabase.ready
				&& Board.endgameDatabase.lookup(b)) {
			if (Board.COLLECT_EXTRA_STATISTICS)
				Board.endgameEvalCount++;
			return true;
		}

		// compute attacks by my pieces. will next move be a capture?
		long occupied = myPieces | opponentPieces;
		long open = Bits.ON_BOARD & ~occupied;
		long myAttacks = attack(myPieces, open);
		if ((myAttacks & opponentPieces) != 0) {
			if (--oppPieceCount == 0) { // will capture win?
				b.evaluation = myPieceCount * Board.WON_POSITION
						- Board.PLY_DECREMENT;
				return true;
			}
			if (depth > CAPTURE_DEPTH)
				return false; // want a deeper search?

			// Extremely quick and dirty eval
			if (myPieceCount >= oppPieceCount)
				b.evaluation = (myPieceCount - oppPieceCount)
						* (ratios[oppPieceCount] + PIECE_VALUE);
			else
				b.evaluation = -(oppPieceCount - myPieceCount)
						* (ratios[myPieceCount] + PIECE_VALUE);
			return true;
		}

		// Compute attacks by opponent pieces, my active (able to safely move)
		// pieces
		long oppAttacks = attack(opponentPieces, open);
		long mySafeMoves = open & ~oppAttacks;
		long myActive = activity(myPieces, mySafeMoves);

		long attacked = myPieces & oppAttacks;
		if (myActive == 0 || // forced to move into capture or trap
				((attacked & myActive) != attacked) || // some attacked pieces
														// are already trapped
				(attacked & (attacked - 1)) != 0) // or too many captures to
													// evade?
		{
			if (--myPieceCount == 0) { // about to lose my last piece?
				b.evaluation = -(oppPieceCount * Board.WON_POSITION - 4 * Board.PLY_DECREMENT);
				return true;
			}
			if (depth > CAPTURE_DEPTH)
				return false; // want a deeper search?
		} else if (attacked != 0) {
			if (depth > THREAT_DEPTH)
				return false;
		}

		// Who controls key squares in each part of the board?
		// avoid slow conditionals -- ((L-1)>>57) is short for (L?0:127) when hi
		// bits of L clear
		int control = (int) (((opponentPieces & LEFT_CONTROL) - 1) >>> 57)
				- (int) (((myPieces & LEFT_CONTROL) - 1) >>> 57)
				+ (int) (((opponentPieces & CENTER_CONTROL) - 1) >>> 57)
				- (int) (((myPieces & CENTER_CONTROL) - 1) >>> 57)
				+ (int) (((opponentPieces & RIGHT_CONTROL) - 1) >>> 57)
				- (int) (((myPieces & RIGHT_CONTROL) - 1) >>> 57);

		// Compute opponent active pieces. Count active pieces.
		long oppSafeMoves = open & ~myAttacks;
		long oppActive = activity(opponentPieces, oppSafeMoves);
		int myActivity = Bits.count(myActive);
		int oppActivity = Bits.count(oppActive);

		// Quick and dirty eval for even or unclear positions
		if (myActivity == oppActivity) {
			myPieceCount += myActivity; // active pieces count double
			oppPieceCount += oppActivity;
			if (myPieceCount >= oppPieceCount)
				b.evaluation = control + (myPieceCount - oppPieceCount)
						* ratios[oppPieceCount];
			else
				b.evaluation = control - (oppPieceCount - myPieceCount)
						* ratios[myPieceCount];
			return true;
		}

		// Use active piece counts to determine which side is the attacker
		boolean attacking;
		long attackingPieces, defendingPieces, stuckDefenders, safeForDefense;
		int attackingActivity, attackingTrapped, defendingActivity, defendingTrapped;
		if (myActivity > oppActivity) {
			attacking = true;
			attackingPieces = myPieces;
			attackingActivity = myActivity;
			attackingTrapped = myPieceCount - myActivity;
			defendingPieces = opponentPieces;
			defendingActivity = oppActivity;
			stuckDefenders = opponentPieces & ~oppActive;
			defendingTrapped = oppPieceCount - oppActivity;
			safeForDefense = oppSafeMoves;
		} else {
			attacking = false;
			attackingPieces = opponentPieces;
			attackingActivity = oppActivity;
			attackingTrapped = oppPieceCount - oppActivity;
			defendingPieces = myPieces;
			defendingActivity = myActivity;
			stuckDefenders = myPieces & ~myActive;
			defendingTrapped = myPieceCount - myActivity;
			safeForDefense = mySafeMoves;
			control = -control;

			// switch alpha and beta
			int x = alpha;
			alpha = -beta;
			beta = -x;
		}

		// Down to a single defender? If so almost surely lost.
		// But make sure no chance of the defender taking down enough attackers
		// to draw.
		//
		// We check:
		// is there only one defender, and more than one attacker?
		// are any attackers trapped, or on bad squares accessible to the
		// defender?
		if (defendingActivity + defendingTrapped == 1
				&& attackingActivity >= 2
				&& attackingTrapped == 0
				&& (safeForDefense & nextTo(attackingPieces & ~ACTIVE_SQUARES)) == 0
				&& (safeForDefense & nextTo(attackingPieces) & nextTo(defendingPieces)) == 0) {
			b.evaluation = attackingActivity * Board.WON_POSITION
					- (Board.WON_POSITION / 2) + control;
			if (!attacking)
				b.evaluation = -b.evaluation;
			return true;
		}

		// Find fortresses and estimate material cost to break them
		long attackZone = 0, fortress = 0;
		int fortressStrength = 0;

		// Large and small left fortresses
		if ((LG_LEFT_FORT & attackingPieces) == 0
				&& (LG_LEFT_GUARD & defendingPieces) != 0) {
			fortress = LG_LEFT_FORT & defendingPieces;
			fortress &= fortress - 1;
			if (fortress != 0) {
				fortress &= fortress - 1;
				fortressStrength = (fortress == 0 ? 1 : 2);
				attackZone = LG_LEFT_ATTACK;
				if ((LG_LEFT_GUARD & stuckDefenders) != 0) {
					defendingActivity++;
					defendingTrapped--;
				}
			}
		} else if ((SM_LEFT_FORT & attackingPieces) == 0
				&& (SM_LEFT_GUARD & defendingPieces) != 0) {
			fortress = SM_LEFT_FORT & defendingPieces;
			fortress &= fortress - 1;
			if (fortress != 0) {
				fortressStrength = 1;
				attackZone = SM_LEFT_ATTACK;
				if ((SM_LEFT_GUARD & stuckDefenders) != 0) {
					defendingActivity++;
					defendingTrapped--;
				}
			}
		}

		// Large and small right fortresses
		if ((LG_RIGHT_FORT & attackingPieces) == 0
				&& (LG_RIGHT_GUARD & defendingPieces) != 0) {
			fortress = LG_RIGHT_FORT & defendingPieces;
			fortress &= fortress - 1;
			if (fortress != 0) {
				fortress &= fortress - 1;
				fortressStrength += (fortress == 0 ? 1 : 2);
				attackZone |= LG_RIGHT_ATTACK;
				if ((LG_RIGHT_GUARD & stuckDefenders) != 0) {
					defendingActivity++;
					defendingTrapped--;
				}
			}
		} else if ((SM_RIGHT_FORT & attackingPieces) == 0
				&& (SM_RIGHT_GUARD & defendingPieces) != 0) {
			fortress = SM_RIGHT_FORT & defendingPieces;
			fortress &= fortress - 1;
			if (fortress != 0) {
				fortressStrength++;
				attackZone |= SM_RIGHT_ATTACK;
				if ((SM_RIGHT_GUARD & stuckDefenders) != 0) {
					defendingActivity++;
					defendingTrapped--;
				}
			}
		}

		// No fortress but significant spatial control, treat as a unit-strength
		// fortress
		// (this covers many sideways fortress cases)
		if (fortressStrength == 0)
			fortressStrength = control >>> 31; // (C<0)?1:0

		// Can the fortresses be broken?
		int eval;
		if (attackingActivity - defendingActivity - fortressStrength > 0) {
			eval = ATTACK_WEIGHT
					* (attackingActivity - defendingActivity - fortressStrength)
					+ TRAPPED_PIECE_WEIGHT
					* (attackingTrapped - defendingTrapped) - CONVERSION_WEIGHT
					* defendingActivity;

		} else {

			// Attack won't work, make eval similar to even/unclear eval
			int a = 2 * attackingActivity + attackingTrapped;
			int d = 2 * defendingActivity + defendingTrapped;
			if (a > d)
				eval = (a - d) * ratios[d];
			else if (d > a)
				eval = (d - a) * ratios[a];
			else
				eval = 0;
			eval += control + ATTACK_BONUS;
		}

		// Do slower low-order eval terms only if near alpha-beta window
		if (eval + MAX_POSITIONAL_EVAL > alpha
				&& eval - MAX_POSITIONAL_EVAL < beta) {

			// find space available to defenders
			long space = defendingPieces;
			for (;;) {
				long newSpace = space | (nextTo(space) & safeForDefense);
				if (space == newSpace)
					break;
				space = newSpace;
			}

			// add in defensive space, number of forward attackers
			eval += FORWARD_WEIGHT * Bits.count(attackingPieces & attackZone)
					- SPACE_WEIGHT * Bits.count(space);
		}

		if (attacking)
			b.evaluation = eval;
		else
			b.evaluation = -eval;
		return true;
	}
}

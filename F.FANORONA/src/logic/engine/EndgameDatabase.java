package logic.engine;

// Game of Fanorona
// David Eppstein, UC Irvine, 28 Sep 1997
//
// Endgame database (application version only!)
//
// Usage:
//
// Call endgameDatabase.lookup(Board b)) to look up an individual board.
// It will return true if the database is set up and the board is there,
// with the correct value set in the board's evaluation.  Note that the board
// is assumed (but not checked) to be not midCapture().
//
// Call endgameDatabase.search(Board b) to find the best move for a board.
// The board may be midCapture(). Again, it returns true if successful,
// false if a full alpha-beta search needs to be performed.

//import java.io.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import logic.board.Bits;
import logic.board.Board;

public class EndgameDatabase {
	// The database contents
	byte db[];
	boolean ready = false;

	// Conditional compilation for db construction code
	static final boolean CREATE_DATABASE = false;
	static final boolean SANITY_CHECKS = false;

	// Convert contents to and from compact single-byte representation.
	// Assumes winning evals are always an odd multiple of PLY_DECREMENT
	// and losing evals are always an even multiple.
	final byte evalByte(int eval) {
		if (eval == 0)
			return 0;
		int x = (eval < 0 ? -eval : eval);
		int base, ply;
		if (x <= Board.WON_POSITION) {
			base = 64;
			ply = Board.WON_POSITION - x;
		} else {
			base = 128;
			ply = 2 * Board.WON_POSITION - x;
		}
		if (ply <= 0) {
			if (SANITY_CHECKS)
				System.out.println("Non-positive ply! " + ply + " from eval "
						+ eval);
			ply = 1;
		} else if (ply >= 64) {
			if (SANITY_CHECKS)
				System.out.println("Huge ply! " + ply + " from eval " + eval);
			ply = 63;
		}
		int b = base - ply;
		return (byte) (eval < 0 ? -b : b);
	}

	final int eval(byte b) {
		if (b == 0)
			return 0;
		int x = (b < 0 ? -b : b);
		int base, ply;
		if (x <= 64) {
			base = Board.WON_POSITION;
			ply = 64 - x;
		} else {
			base = 2 * Board.WON_POSITION;
			ply = 128 - x;
		}
		x = base - ply;
		return (b < 0 ? -x : x);
	}

	// First step in database lookup: check if few enough pieces that it will be
	// there
	final boolean smallEnough(long pieces) {
		pieces &= Bits.ON_BOARD & pieces - 1;
		return (pieces & (pieces - 1)) == 0;
	}

	// Second step in database lookup: convert bitboard to 12-bit representation
	final short compact(long pieces) {
		pieces &= Bits.ON_BOARD;
		long y = pieces - 1;
		int x = Bits.count(pieces ^ y);
		pieces &= y;
		return (short) ((x << 6) + Bits.count(pieces ^ (pieces - 1)));
	}

	// Third step in database lookup: convert 12-bit to even shorter
	// representation
	// Reverse conversion can be done by lookup in array pieces[].
	short shortReps[];
	short numShortReps;
	long pieces[];

	short shortRep(long pieces) {
		return shortReps[compact(pieces)];
	}

	final void setupShortReps() {
		int i, j, k, l;
		numShortReps = 0;
		shortReps = new short[1 << 12];
		for (i = 0; i < 5; i++)
			for (j = 0; j < 9; j++)
				for (k = 0; k <= i; k++)
					for (l = 0; l < 9; l++) {
						if (k == i && l > j)
							continue;
						short c = compact(Bits.at(i, j) | Bits.at(k, l));
//						if (SANITY_CHECKS && shortReps[c] != 0)
//							System.out.println("Short rep already nonzero!");
						shortReps[c] = numShortReps++;
					}
//		if (SANITY_CHECKS && numShortReps != 45 * 23)
//			System.out.println("Number of short reps: " + numShortReps);
		if (CREATE_DATABASE) {
			pieces = new long[numShortReps];
			for (i = 0; i < 5; i++)
				for (j = 0; j < 9; j++)
					for (k = 0; k <= i; k++)
						for (l = 0; l < 9; l++) {
							if (k == i && l > j)
								continue;
							long p = Bits.at(i, j) | Bits.at(k, l);
							pieces[shortReps[compact(p)]] = p;
						}
		}
	}

	// Fourth step in database lookup: action of symmetry
	//
	// The symmetry group is Z2 * Z2 which we represent as a two-bit number,
	// one bit reversing rows, the other reversing columns.
	// Fortunately, we don't need to distinguish group elements from their
	// inverses.
	//
	short syms[][]; // indexed by symmetry(0-3), shortRep
	byte minSym[]; // which symmetry produces the smallest shortRep?
	short symReps[]; // conversion table to symmetric representations
	short shortRepForSym[]; // and vice versa
	short numSymReps;

	final void setupSyms() {
		syms = new short[4][];
		for (int i = 0; i < 4; i++)
			syms[i] = new short[numShortReps];
		minSym = new byte[numShortReps];
		symReps = new short[numShortReps];
		int i, j, k, l;
		for (i = 0; i < 5; i++)
			for (j = 0; j < 9; j++)
				for (k = 0; k <= i; k++)
					for (l = 0; l < 9; l++) {
						if (k == i && l > j)
							continue;
						short s = shortReps[compact(Bits.at(i, j)
								| Bits.at(k, l))];
						syms[0][s] = s;
						syms[1][s] = shortReps[compact(Bits.at(4 - i, j)
								| Bits.at(4 - k, l))];
						syms[2][s] = shortReps[compact(Bits.at(i, 8 - j)
								| Bits.at(k, 8 - l))];
						syms[3][s] = shortReps[compact(Bits.at(4 - i, 8 - j)
								| Bits.at(4 - k, 8 - l))];
						if (minSym[s] == 0) {
							symReps[s] = numSymReps++;
							for (byte r = 1; r < 4; r++) {
								if (syms[r][s] != s)
									minSym[syms[r][s]] = r;
								symReps[syms[r][s]] = symReps[s];
							}
						}
					}
		if (CREATE_DATABASE) {
			shortRepForSym = new short[numSymReps];
			for (short s = 0; s < numShortReps; s++)
				if (minSym[s] == 0) {
					if (SANITY_CHECKS && shortRepForSym[symReps[s]] != 0)
						System.out.println("shortRepForSym already nonzero!");
					shortRepForSym[symReps[s]] = s;
				}
		}
	}

	// Perform lookup
	final boolean lookup(Board b) {
		short myPieceRep = shortReps[compact(b.myPieces)];
		short oppPieceRep = shortReps[compact(b.opponentPieces)];
		byte sym = minSym[myPieceRep];
		int index = symReps[myPieceRep] * numShortReps + syms[sym][oppPieceRep];
		b.evaluation = eval(db[index]);
		return true;
	}

	// Do one-ply (plus multiple-capture) search, return results in evaluation
	// and bestMove
	// To avoid complete aimlessness, if we're using this to play (rather than
	// to construct
	// the database itself) we call evaluate on any drawn position.
	//
	final boolean search(Board b) {
		if (!smallEnough(b.myPieces) || !smallEnough(b.opponentPieces))
			return false;
//		if (SANITY_CHECKS && (b.myPieces & Bits.ON_BOARD) == 0)
//			System.out.println("No pieces in search!");
		b.setMoveGenerator();
		b.bestMove = -1L;
		b.evaluation = -Integer.MAX_VALUE;
		long move;
		while ((move = b.moveGenerator.nextElement()) >= 0) {
			b.setChild(move);
			int childEval;
			if ((b.child.opponentPieces & Bits.ON_BOARD) == 0) { // wipe out
				childEval = Bits.count(b.myPieces & Bits.ON_BOARD)
						* Board.WON_POSITION;
			} else if (b.child.myPieces < 0) { // if (child.midCapture())
				search(b.child); // search recursively
				childEval = b.child.evaluation;
			} else { // change sides, not all captured
				lookup(b.child); // find in database
				childEval = -b.child.evaluation; // negate to get score from our
													// pov

				if (ready && childEval == 0) { // using search to choose endgame
												// move?
					ready = false; // disable recursive database lookup
					if (Eval.evaluate(b.child))
						childEval = -b.child.evaluation; // do positional
															// evaluation of
															// drawn pos
					ready = true; // turn database back on
				}
			}
			if (childEval > 0)
				childEval -= Board.PLY_DECREMENT;
			else if (childEval < 0)
				childEval += Board.PLY_DECREMENT;
			if (childEval > b.evaluation) {
				b.evaluation = childEval;
				b.bestMove = move;
				if (ready)
					b.setPrincipalVariation();
			}
		}
//		if (SANITY_CHECKS && b.evaluation == -Integer.MAX_VALUE)
//			System.out.println("No moves found!");
		return true;
	}

	// Compute database!
	//
	// There are more sophisticated ways of doing this, but it's a little
	// complicated
	// (involving generating moves backwards rather than forwards) and made even
	// more
	// complex by the fact that our eval counts material at the win not just
	// plys to win.
	// So, the algorithm we use is straightforward:
	//
	// do {
	// call search() on all positions in the db
	// store changed evaluations back into the db
	// } while (!changed > 0)
	//
	final void setupDatabase() {
		if (SANITY_CHECKS) {
			for (short symRep = 0; symRep < numSymReps; symRep++) {
				if (symRep != symReps[shortRepForSym[symRep]])
					System.out.println("Non-inverse! symReps[shortRepForSym["
							+ symRep + "]] = "
							+ symReps[shortRepForSym[symRep]]);
				long p = pieces[shortRepForSym[symRep]];
				if (minSym[shortRepForSym[symRep]] != 0)
					System.out.println("Asymmetric rep!");
				if (shortRepForSym[symRep] != shortReps[compact(p)])
					System.out.println("Wrong shortrep!");
			}
			for (short shortRep = 0; shortRep < numShortReps; shortRep++) {
				if (pieces[shortRep] == 0)
					System.out.println("No pieces!");
				if (shortRep != syms[minSym[shortRep]][shortRepForSym[symReps[shortRep]]])
					System.out.println("Assymetric short rep!");
			}
		}
		db = new byte[numSymReps * numShortReps];
		System.out.println("Creating endgame database... " + numSymReps
				* numShortReps + " entries");
		int iterations = 0;
		Board b = new Board();
		int numChanges;
		do {
			numChanges = 0;
			for (short symRep = 0; symRep < numSymReps; symRep++) {
				b.myPieces = pieces[shortRepForSym[symRep]];
				for (short shortRep = 0; shortRep < numShortReps; shortRep++) {
					b.opponentPieces = pieces[shortRep];
					if ((b.myPieces & b.opponentPieces) != 0)
						continue;
					search(b);
					byte newEval = evalByte(b.evaluation);
					int index = symRep * numShortReps + shortRep;
					if (db[index] != newEval) {
						if (SANITY_CHECKS) {
							if (db[index] > 0 && newEval < db[index])
								System.out
										.println("Eval got worse after win found! "
												+ db[index] + "=>" + newEval);
							else if (db[index] < 0 && newEval > db[index])
								System.out
										.println("Eval got better after loss found! "
												+ db[index] + "=>" + newEval);
						}
						db[index] = newEval;
						numChanges++;
					}
				}
			}
			System.out.println("Iteration " + (++iterations) + ": "
					+ numChanges + " updated evals");
		} while (numChanges > 0);
		ready = true;
	}

	// set up the whole shebang
	public EndgameDatabase() {
		setupShortReps();
		setupSyms();
		try {
			String filename = System.getProperty("endgame.database.filename");
			if (filename != null) {
				InputStream stream = new FileInputStream(filename);
				db = new byte[numSymReps * numShortReps];
				stream.read(db);
				stream.close();
				ready = true;
			}
		} catch (IOException e) {
			System.out.println("Unable to read endgame database: "
					+ e.getMessage());
			if (CREATE_DATABASE) {
				setupDatabase();
				System.out.println("Writing database to file");
				try {
					OutputStream stream = new FileOutputStream("endgame.db");
					stream.write(db);
					stream.close();
				} catch (IOException ioe) {
					System.out.println("Write failed: " + ioe.getMessage());
				}
			} else {
				db = null;
				ready = false;
			}
		}
	}
}

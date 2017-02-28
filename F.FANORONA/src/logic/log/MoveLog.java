package logic.log;

import gui.widgets.Options;
import gui.widgets.TextPane;

import java.awt.Event;

import logic.board.Bits;
import logic.board.Board;
import logic.engine.Game;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// Keep track of the moves of the game

public class MoveLog extends TextPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6232458233537485283L;
	Game game;
	int moveNumber;

	// initialize
	public MoveLog(Game g) {
		game = g;
		showMoves(game);
	}

	// make TextArea look like List
	// synchronized public void addItem(String s) {
	// appendText(s + System.getProperty("line.separator"));
	// }
	// public void clear() { setText(""); }
	// public boolean isEditable() { return true; }

	// show who is about to play
	void showPlayer(Board b, boolean verbose) {
		if (b.gameOver()) {
			if (!verbose) {
				addItem(showMoveBuffer.toString());
				showMoveBuffer.setLength(0);
			}
			if (b.whiteWins())
				showMoveBuffer.append("White");
			else
				showMoveBuffer.append("Black");
			showMoveBuffer.append(" wins with ")
					.append(
							Bits.count((b.myPieces | b.opponentPieces)
									& Bits.ON_BOARD)).append(" pieces");
			if (verbose)
				addItem(showMoveBuffer.toString());
			return;
		}

		if (!verbose) {
			if (b.whiteToMove() != game.getParameter(Options.WHITE_GOES_FIRST))
				return;
			if (moveNumber != 0)
				addItem(showMoveBuffer.toString());
			showMoveBuffer.setLength(0);
			moveNumber++;
			showMoveBuffer.append(moveNumber).append(".");
			return;
		}

		if (b.previousPosition != null)
			addItem(""); // blank line to separate from prev move
		showMoveBuffer.setLength(0);

		if (b.whiteToMove() == game.getParameter(Options.WHITE_GOES_FIRST)) {
			moveNumber++;
			showMoveBuffer.append(moveNumber).append(". ");
			if (moveNumber < 10)
				showMoveBuffer.append(" "); // extra blank to line up #s
		} else
			showMoveBuffer.append("     ");
		if (b.whiteToMove())
			showMoveBuffer.append("White");
		else
			showMoveBuffer.append("Black");
		if (game.humanToMove(b))
			showMoveBuffer.append(" (Human):");
		else
			showMoveBuffer.append(" (Computer):");
		addItem(showMoveBuffer.toString());
	}

	// find position number (in Fox's notation) of given bit
	static final int bitNumbers[] = makeBitNumbers();

	static final int[] makeBitNumbers() {
		int[] a = new int[65];
		for (int i = 0; i < 65; i++)
			a[i] = -1;
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 9; j++) {
				long x = Bits.at(i, j);
				a[Bits.count(x ^ (x - 1))] = 9 * i + j + 1;
			}
		return a;
	}

	static int bitNumber(long bit) {
		int n = bitNumbers[Bits.count(bit ^ (bit - 1))];
		if (n >= 0)
			return n;
		throw new IllegalArgumentException(
				"Move log: can't find position of bit 0x"
						+ Long.toString(bit, 16));
	}

	static char files[] = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i' };

	static void putPos(StringBuffer buffer, int bitNo, boolean alg) {
		if (alg) {
			bitNo--; // make zero-based
			buffer.append(files[bitNo % 9]).append(5 - (bitNo / 9));
		} else
			buffer.append(bitNo);
	}

	// make string for move itself, append to given buffer
	public static void stringForMove(StringBuffer buffer, Board b, boolean alg,
			boolean verbose, boolean firstMove) {
		if (b.wasPass()) {
			if (firstMove)
				buffer.append("pass");
			return;
		}

		Board prev = b.previousPosition;
		long occupied = b.myPieces | b.opponentPieces;
		long captures = prev.opponentPieces & ~occupied & Bits.ON_BOARD;
		long from = prev.myPieces & ~occupied & Bits.ON_BOARD;
		long to = occupied & ~prev.myPieces & ~prev.opponentPieces
				& Bits.ON_BOARD;
		if ((to & (to - 1)) != 0 || (from & (from - 1)) != 0) { // bogus pv
			buffer.append(Long.toString(from | to | captures, 16)); // at least
																	// avoid
																	// crashing
			return;
		}

		if (!verbose) {
			if (!firstMove && (!prev.midCapture())) {
				buffer.append(" ");
				firstMove = true;
			}
			if (firstMove)
				putPos(buffer, bitNumber(from), alg);
			if (captures == 0)
				buffer.append("-");
			else {
				long nearStart = from;
				nearStart |= (nearStart << Bits.SHIFT_VERTICAL)
						| (nearStart >>> Bits.SHIFT_VERTICAL);
				nearStart |= (nearStart << Bits.SHIFT_HORIZONTAL)
						| (nearStart >>> Bits.SHIFT_HORIZONTAL);
				if ((nearStart & captures) != 0)
					buffer.append("<");
				else
					buffer.append(">");
			}
			putPos(buffer, bitNumber(to), alg);
		} else {
			putPos(buffer, bitNumber(from), alg);
			buffer.append("-");
			putPos(buffer, bitNumber(to), alg);
			if (captures != 0) {
				buffer.append(" x ");
				boolean firstCapture = true;
				while (captures != 0) {
					if (!firstCapture)
						buffer.append(", ");
					firstCapture = false;
					long bit = Bits.lastBit(captures);
					putPos(buffer, bitNumber(bit), alg);
					captures &= ~bit;
				}
			}
		}
	}

	// set up move log to end at the given move
	static StringBuffer showMoveBuffer = new StringBuffer();

	void showMove(Board b, boolean alg, boolean verbose) {
		if (b.previousPosition == null) {
			clear(); // no move, don't show anything
			showMoveBuffer.setLength(0);
			moveNumber = 0;
			showPlayer(b, verbose); // other than who is to move
		} else {
			showMove(b.previousPosition, alg, verbose);
			if (!verbose)
				stringForMove(showMoveBuffer, b, alg, verbose, false);
			else if (!b.wasPass()) {
				showMoveBuffer.setLength(0);
				showMoveBuffer.append("     ");
				stringForMove(showMoveBuffer, b, alg, verbose, false);
				addItem(showMoveBuffer.toString());
			}
			if (!b.midCapture())
				showPlayer(b, verbose);
		}
	}

	void showMoves(Game game) {
		boolean verbose = game.getParameter(Options.NOTATION_LONG);
		showMove(game.getBoard(),
				game.getParameter(Options.NOTATION_ALGEBRAIC), verbose);
		if (!verbose)
			addItem(showMoveBuffer.toString());
	}

	// gross hack to interface with TabPanel
	public boolean postEvent(Event e) {
		if (e.id == Event.WINDOW_EXPOSE)
			showMoves(game);
		return super.postEvent(e);
	}
}

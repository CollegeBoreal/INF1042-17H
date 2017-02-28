package logic.engine;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// Maintain structure of a single game
//
// Search options etc should eventually be stored here as well;
// currently a game is just represented by its current position
// (which also includes back pointers into the game history).
//
// Various methods are synchronized because this class is where
// the search thread communicates with the rest of the applet.

import java.util.BitSet;
import java.util.Observable;

import gui.widgets.Options;

import logic.board.Board;

class AttemptToSetNullBoard extends RuntimeException {
	private static final long serialVersionUID = 3018969207471758784L;
}

class NoPreviousPosition extends AttemptToSetNullBoard {
	private static final long serialVersionUID = -7431847706980296338L;
}

// Since notifying all observers might take a while, we do it in a separate
// thread.
class GameUpdateThread extends Thread {
	Game game;
	Object object;

	public GameUpdateThread(Game g, Object o) {
		game = g;
		object = o;
	}

	public void run() {
		setPriority(MIN_PRIORITY);
		game.gameUpdate(object);
	}
}

public class Game extends Observable {
	// Bitwise parameters. We don't care what they are, we just store them.
	// See Options.java for a list of the parameter values
	BitSet parameters = null;

	public BitSet getParameters() {
		return parameters;
	}

	public boolean getParameter(int i) {
		if (parameters == null)
			return false;
		else
			return parameters.get(i);
	}

	public void updateParameters() {
		new GameUpdateThread(this, parameters).start();
	}

	public void setParameters(BitSet p) {
		parameters = p;
		updateParameters();
	}

	// callback for GameUpdateThread
	// since for some reason setChanged() isn't public
	public void gameUpdate(Object object) {
		setChanged();
		notifyObservers(object);
	}

	// Board representing the current position in the game
	private Board board;

	public Game() {
		board = new Board();
	}

	public void updateBoard(Board b) {
		new GameUpdateThread(this, board).start();
	}

	synchronized public Board getBoard() {
		return board;
	}

	synchronized public void setBoard(Board b) {
		if (b == null)
			throw new AttemptToSetNullBoard();
		board = b;
		updateBoard(b);
	}

	public void resetBoard() {
		setBoard(new Board(getParameter(Options.BLACK_AT_TOP),
				getParameter(Options.WHITE_GOES_FIRST)));
	}

	synchronized public void move(Board from, Board to) {
		if (board == from)
			setBoard(to);
	}

	// Whose turn is it?
	public boolean humanToMove(Board b) {
		if (b.whiteToMove())
			return getParameter(Options.HUMAN_PLAYS_WHITE);
		else
			return getParameter(Options.HUMAN_PLAYS_BLACK);
	}

	synchronized public boolean humanToMove() {
		return humanToMove(board);
	}

	// is anything other than a pass available?
	// here rather than in MoveGenerator because it depends on options
	public final boolean mustPass(Board b) {
		if (!b.midCapture())
			return false;

		// near start of game, rules may require pass
		if (getParameter(Options.NO_MULTIPLE_CAPTURES)) {
			Board bb = b.previousPosition.previousPosition;
			if (bb == null)
				return true;
			bb = bb.previousPosition;
			if (bb != null && bb.previousPosition == null)
				return true;
		}

		// run move generator and test whether non-pass move exists
		MoveGenerator mg = new MoveGenerator(b);
		return (mg.nextElement() == 0);
	}
}

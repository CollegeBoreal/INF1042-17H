package logic.engine;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// Search engine control

import java.util.Observable;
import java.util.Observer;

import gui.widgets.Options;

//import java.util.*;

import logic.board.Board;
import logic.log.MoveLog;
import logic.log.SearchLog;

// thread with controlled access to termination paths
// each path should call terminationPath() and only proceed if it returns true
abstract class TerminationPathThread extends Thread {
	boolean terminating = false;

	synchronized boolean terminationPath() {
		if (terminating)
			return false;
		return terminating = true;
	}
}

class Search extends TerminationPathThread {
	// signal from watcher that it's time to stop searching
	// although this variable is viewed by multiple threads,
	// it should be safe to leave it unsynchronized.
	volatile boolean stop = false;

	boolean getStop() {
		return stop;
	}

	void setStop(boolean s) {
		stop = s;
	}

	static final int searchBound = 7200;

	Game game;
	Board board;
	SearchLog log;
	Board firstMove = null;
	SearchWatcher watcher = null;
	int ply = 0;

	Search(Game g, Board b, SearchLog sl, int p) {
		ply = p;
		if (ply == 0)
			watcher = new SearchWatcher(this);
		board = b;
		game = g;
		log = sl;
	}

	// Current candidate for best move
	// Again, assuming assignments are atomic, it should be safe to be
	// unsynchronized.
	volatile Board move = null;

	Board getMove() {
		return move;
	}

	void setMove(Board m) {
		move = m;
	}

	// different paths to thread termination
	// abort - when game has been updated out from under us (called from main
	// applet thread)
	// done - normal search termination (called from searcher thread)
	// panic - timeout expired, use partial search results (called from watcher
	// thread)
	//
	// note that to avoid timing complications we dont worry about explicitly
	// terminating the watcher -- that will be done for us when the board is
	// updated

	void suspendSafely() {
		try {
			this.suspendSafely();
		} catch (Throwable e) {
		}
	}

	void makeMove() {
		Board move = getMove();
		if (move == null)
			move = new Board(board, new MoveGenerator(board).nextElement());
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY); // make UI run
																	// fast
		game.move(board, move);
	}

	void abort() {
		if (!terminationPath())
			return;
		log.addItem("Position changed while searching, search aborted");
		Board.sequenceNumber++; // terminate currently running search
	}

	void done() {
		if (!terminationPath())
			return;
		makeMove();
		board.child = null; // now that search is over, allow gc to free nodes
		board.principalVariation = null;
		board.moveGenerator = null;
	}

	void panic() {
		if (!terminationPath())
			return;
		Board.sequenceNumber++; // terminate currently running search
		log.addItem("Search timed out, making move without finishing search");
		makeMove();
	}

	static StringBuffer searchLogBuffer = new StringBuffer();

	void pvString(Board b) {
		boolean alg = game.getParameter(Options.NOTATION_ALGEBRAIC);
		boolean firstMove = true;
		for (;;) {
			if (!b.hasPrincipalVariation)
				break;
			else {
				b = b.principalVariation;
				MoveLog
						.stringForMove(searchLogBuffer, b, alg, false,
								firstMove);
			}
			firstMove = false;
		}
	}

	// main search loop
	public void run() {
		// make redisplay happen before we start using all the CPU
		setPriority(MIN_PRIORITY);
		yield();

		// after redisplay, make priority above system maint tasks
		setPriority(NORM_PRIORITY);

		// If we have to pass, do so without search
		if (game.mustPass(board)) {
			log.addItem("Forced pass");
			setMove(new Board(board, 0));
			done();
			return;
		}

		// Test if we are in the first four ply.
		//
		// If so, we move arbitrarily rather than passing
		// to add a little variety to our play
		// (and also because the search doesn't know about mustPass and
		// may give a bogus principal variation).
		if (board.previousPosition == null
				|| board.previousPosition.previousPosition.previousPosition == null) {
			log.addItem("Choosing first move arbitrarily");
			setMove(MoveGenerator.arbitraryMove(board));
			done();
			return;
		}

		// avoid search if we only have one move (or even more if we have zero)
		MoveGenerator mg = new MoveGenerator(board);
		if (!mg.hasMoreElements())
			return;
		setMove(new Board(board, mg.nextElement()));
		if (!mg.hasMoreElements()) {
			log.addItem("Forced move");
			done();
			return;
		}

		// try endgame database before searching
		if (Board.endgameDatabase != null && Board.endgameDatabase.ready
				&& Board.endgameDatabase.search(board)) {
			setMove(board.principalVariation);
			searchLogBuffer.setLength(0);
			searchLogBuffer.append("Endgame database eval: ");
			centsToDollars(board.evaluation);
			log.addItem(searchLogBuffer.toString());
			done();
			return;
		}

		// set the alarm clock *AFTER* giving UI a chance to catch up on
		// redisplay
		// and after checking that we don't need to bother searching
		if (watcher != null)
			watcher.start();
		search();
	}

	static final void centsToDollars(int n) {
		if (n < 0) {
			searchLogBuffer.append("-");
			n = -n;
		}
		int cents = n % 100;
		searchLogBuffer.append(n / 100);
		if (cents < 10)
			searchLogBuffer.append(".0");
		else
			searchLogBuffer.append(".");
		searchLogBuffer.append(cents);
	}

	// iterated deepening until we get told to stop
	static final int ASPIRATION_WINDOW = 50;
	static int currentEval = 0;

	void search() {
		int depth = 1;
		boolean logDisabled = false;
		int previousEval = 0;
		board.bestMove = -1L;
		int sequenceNumber = ++Board.sequenceNumber; // abort any previously
														// running search
		while ((ply == 0 || depth <= ply) && !getStop()) {
			Board.nodeCount = Board.leafCount = 0;
			if (Board.COLLECT_EXTRA_STATISTICS)
				Board.boardConsCount = Board.moveGenConsCount = Board.endgameEvalCount = Board.pvChangeCount = 0;
			if (Hash.COLLECT_STATISTICS)
				Hash.hits = Hash.misses = Hash.shallow = Hash.badBound = 0;
			long startTime = System.currentTimeMillis();

			// Aspiration search -- start with an artificially narrow
			// [alpha,beta] window
			// centered on our best guess of the true search value, then repeat
			// if
			// we fail high or low. This assumes a fail high or low is going to
			// produce
			// a true evaluation of a (not necessarily) optimal line, rather
			// than just
			// returning alpha or beta itself, so we don't spend too much time
			// running
			// through numbers between our initial guess and the true search
			// value.
			int alpha = currentEval - ASPIRATION_WINDOW / 2;
			int beta = currentEval + ASPIRATION_WINDOW / 2;
			int aspirations = 0;
			for (;;) {
				if (sequenceNumber != Board.sequenceNumber) {
					abort();
					return;
				}
				board.alphaBeta(depth * Board.PLY, alpha, beta, sequenceNumber);
				currentEval = board.evaluation;
				aspirations++;
				if (currentEval >= beta) { // fail high
				// alpha = currentEval - 1;
				//
				// May lead to infinite loops in some pathological cases?
				// It shouldn't hurt much to leave alpha alone anyway, the hash
				// table
				// will take care of speeding up searches at nodes w/values less
				// than currentEval.
					beta = Integer.MAX_VALUE;
				} else if (currentEval <= alpha) { // fail low
					alpha = -Integer.MAX_VALUE;
					// beta = currentEval + 1;
				} else
					break;
				yield();
			}

			long endTime = System.currentTimeMillis();

			// because some important lines in the hash table might have
			// disappeared,
			// it is possible to see a win and then stop seeing it again.
			// in this case, we don't want to make a move into a non-winning
			// variation
			// just because it was returned by a deeper search...
			if (winning(previousEval) && !between(0, previousEval, currentEval))
				currentEval = previousEval;

			// don't just use principalVariation as move, it will get trashed by
			// the next
			// search -- instead make another board with the same move
			else if (board.principalVariation != null)
				setMove(new Board(board, board.bestMove));

			// log search (if in winning line improvement search, only log if
			// interesting)
			if (!logDisabled || currentEval != previousEval) {
				previousEval = currentEval;

				searchLogBuffer.setLength(0);
				searchLogBuffer.append(depth).append(" ply: ");
				centsToDollars(currentEval);
				searchLogBuffer.append(", ").append(Board.nodeCount).append(
						" nodes, ");
				searchLogBuffer.append(Board.leafCount).append(" leaves, ")
						.append(Board.nodeCount / (endTime - startTime + 1))
						.append("k nps");

				if (Hash.COLLECT_STATISTICS) {
					long hashTotal = Hash.hits + Hash.shallow + Hash.badBound
							+ Hash.misses;
					if (hashTotal > 0) {
						searchLogBuffer.append(", hash rate ").append(
								(100L * Hash.hits) / hashTotal);
						searchLogBuffer.append("%-");
						searchLogBuffer.append((100L * (Hash.hits
								+ Hash.shallow + Hash.badBound))
								/ hashTotal);
						searchLogBuffer.append("%");
					}
				}
				log.addItem(searchLogBuffer.toString());

				if (Board.COLLECT_EXTRA_STATISTICS) {
					searchLogBuffer.setLength(0);
					searchLogBuffer.append("  ").append(Board.boardConsCount)
							.append(" new boards, ");
					searchLogBuffer.append(Board.moveGenConsCount).append(
							" move generators, ");
					searchLogBuffer.append(Board.pvChangeCount).append(
							" pv changes, ");
					searchLogBuffer.append(Board.endgameEvalCount).append(
							" endgame evals");
					log.addItem(searchLogBuffer.toString());
				}

				searchLogBuffer.setLength(0);
				searchLogBuffer.append("  PV: ");
				pvString(board);
				log.addItem(searchLogBuffer.toString());
			}

			// The following line looks safe: if we see a win in k plys, no need
			// to search any deeper.
			//
			// if (board.evaluation < -searchBound || board.evaluation >
			// searchBound) done();
			//
			// Or is it? Because of the hash table and search extensions, the
			// first win we see might not
			// win in the fewest plys, and our opponent might be able to force a
			// repeated sequence of
			// moves where we see a win in our PV after each search, but it
			// never gets any closer.
			// In fact, I actually saw this happen.
			//
			// So, even if we think it's obvious how to win, we do a deeper
			// search to look for a better
			// win (fewer plys, or more plys but more of our pieces left at the
			// end). Also, this search
			// slows us down, so the user can see the moves as they're being
			// made. To avoid ridiculous
			// streams of long searches in the log, we only log "interesting"
			// searches beyond this point.

			if (winning(currentEval) && !logDisabled) {
				log.addItem("Winning line found, searching for better win");
				logDisabled = true;
			}

			depth++;
			yield(); // make sure other threads get a chance to run
		}
		done();
	}

	// test whether an evaluation is good enough to win
	static final boolean winning(int eval) {
		return eval < -searchBound || eval > searchBound;
	}

	// return true iff b is in the [closed] interval [a,c]. assumes a != c but
	// not a<c.
	static final boolean between(int a, int b, int c) {
		return (a >= b) != (c >= b);
	}
}

// Alarm clock for search
class SearchWatcher extends TerminationPathThread implements Observer {
	int searchTime = 1500; // time until we tell search to wake up (msec)

	public void setSearchTime(int st) {
		searchTime = st;
	}

	public int getSearchTime() {
		return searchTime;
	}

	Search search;

	SearchWatcher(Search s) {
		search = s;
		setPriority(MAX_PRIORITY);
	}

	boolean observing = false;

	synchronized void observe() {
		observing = true;
		search.game.addObserver(this);
	}

	synchronized void done() {
		if (observing)
			search.game.deleteObserver(this);
		observing = false;
		this.stop();
	}

	public void run() {
		observe();
		try {
			sleep(searchTime);
		} catch (InterruptedException e) {
		}
		search.setStop(true);
		try {
			sleep(5 * searchTime);
		} catch (InterruptedException e) {
		}
		if (!terminationPath())
			return;
		search.panic();
		done();
	}

	public void update(Observable g, Object o) {
		if (g != search.game)
			return; // ignore update that's not about the actual game
		if (o == search.board)
			return; // Ignore update that started this search running
		if (!terminationPath())
			return;
		search.abort(); // Mother will start a new one if necessary
		done();
	}
}

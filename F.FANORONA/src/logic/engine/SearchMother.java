package logic.engine;

// Game of Fanorona
// David Eppstein, UC Irvine, 3 Sep 1997
//
// Spawner of all searches

import java.util.Observable;
import java.util.Observer;

import gui.widgets.Options;

//import java.util.*;

import logic.Fanorona;
import logic.board.Board;
import logic.log.SearchLog;

public class SearchMother implements Observer {
	SearchLog log;

	public SearchMother(Game g, SearchLog sl) {
		log = sl;
		g.addObserver(this);
	}

	public void update(Observable g, Object o) {
		if (g == null || !(g instanceof Game))
			return;
		Game game = (Game) g;
		Board board = game.getBoard();
		if (!game.humanToMove(board) && !board.gameOver()) {
			Fanorona.showMessage(log, "Computer is searching", true);
			if (board.previousPosition != null) {
				if (game.humanToMove(board.previousPosition))
					log.clear();
				else
					log.addItem("");
			}
			log.addItem("Searching...");

			int ply = 0;
			if (game.getParameter(Options.ONE_PLY))
				ply = 1;
			else if (game.getParameter(Options.TWO_PLY))
				ply = 2;
			else if (game.getParameter(Options.THREE_PLY))
				ply = 3;
			Search search = new Search(game, board, log, ply);
			if (game.getParameter(Options.FAST_SPEED))
				search.watcher.setSearchTime(250);
			else if (game.getParameter(Options.MEDIUM_SPEED))
				search.watcher.setSearchTime(1500);
			else if (game.getParameter(Options.SLOW_SPEED))
				search.watcher.setSearchTime(10000);
			else if (ply == 0)
				throw new IllegalArgumentException(
						"No speed setting in game parameters");
			search.start();
		}
	}
}
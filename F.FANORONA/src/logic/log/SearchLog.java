package logic.log;

import gui.widgets.TextPane;

import java.util.Observable;
import java.util.Observer;

import logic.engine.Game;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// Keep track of the results of searching
// We just store text and reset it when the game is reset; the search engine
// is responsible for determining what to put here.

public class SearchLog extends TextPane implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3260481744984560638L;

	// initialize, tell game to update us when it changes
	public SearchLog(Game g) {
		g.addObserver(this);
	}

	// whenever game is reset, clear the log
	public void update(Observable g, Object o) {
		if (g instanceof Game) {
			Game game = (Game) g;
			if (game.getBoard().previousPosition == null)
				clear();
		}
	}
}
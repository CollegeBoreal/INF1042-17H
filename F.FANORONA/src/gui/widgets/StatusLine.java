package gui.widgets;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// Prompt user to move by putting messages in the status line

import java.util.Observer;
import java.util.Observable;
import java.awt.Component;

import logic.Fanorona;
import logic.board.Board;
import logic.engine.Game;

public class StatusLine implements Observer {
	// component used to find applet status line
	Component component;

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component c) {
		component = c;
	}

	// initialize, tell game to update us when it changes
	public StatusLine(Component c, Game g) {
		g.addObserver(this);
		setComponent(c);
	}

	// whenever game state changes, make sure status line is synchronized
	public void update(Observable g, Object o) {
		if (!(g instanceof Game))
			throw new IllegalArgumentException(
					"Status line: updated object is not game");
		Game game = (Game) g;
		Board board = game.getBoard();

		// test if game is over
		if (board.gameOver()) {
			if (board.whiteWins())
				Fanorona.showMessage(getComponent(), "White has won!", true);
			else
				Fanorona.showMessage(getComponent(), "Black has won!", true);
			return;
		}

		// not over, prompt human to move
		if (!game.humanToMove())
			return;
		if (board.previousPosition == null || board.midCapture())
			return;
		if (board.whiteToMove())
			Fanorona.showMessage(getComponent(), "White to move", true);
		else
			Fanorona.showMessage(getComponent(), "Black to move", true);
	}

}

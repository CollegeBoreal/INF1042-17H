package gui.widgets.buttons;

import logic.Fanorona;
import logic.engine.Game;

class ResetButton extends GameButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4779471135792031861L;

	public ResetButton(Game g) {
		super(g, "New Game");
	}

	public boolean active() {
		return (game.getBoard().previousPosition != null);
	}

	public void action() {
		game.resetBoard();
		Fanorona.showMessage(this, "New game started", true);
	}

	public String status() {
		return "Reset the board to the starting position";
	}
}

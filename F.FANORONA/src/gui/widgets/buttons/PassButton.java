package gui.widgets.buttons;

import logic.board.Board;
import logic.engine.Game;

class PassButton extends GameButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8960504727766937294L;

	public PassButton(Game g) {
		super(g, "Pass");
	}

	public boolean active() {
		return game.humanToMove() && game.getBoard().midCapture();
	}

	public void action() {
		game.setBoard(new Board(game.getBoard(), 0L));
	}

	public String status() {
		return "Stop eating pieces and finish your turn";
	}
}

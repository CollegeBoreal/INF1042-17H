package gui.widgets.buttons;

import gui.StackLayout;

import java.awt.Color;
import java.awt.Event;
import java.awt.Panel;
import java.util.Observable;
import java.util.Observer;

import logic.Fanorona;
import logic.engine.Game;

// David Eppstein, UC Irvine, 11 Jun 1997
//
// Buttons to change the game state

public abstract class GameButton extends ThreeStateButton implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5623938354818651411L;
	Game game;

	public GameButton(Game g, String s) {
		super(s);
		game = g;
		g.addObserver(this);
		enabled = active();
	}

	public void update(Observable g, Object o) {
		if (enabled != active()) {
			if (active())
				enable();
			else
				disable();
		}
	}

	public boolean mouseEnter(Event e, int x, int y) {
		if (!enabled)
			return false;
		Fanorona.showMessage(this, status(), false);
		return true;
	}

	public abstract boolean active();

	public abstract String status();

	public static void buttonsPanel(Panel buttons, Game game, Color bgc,
			Color fgc, Color disc) {
		buttons.setLayout(new StackLayout(StackLayout.HORIZONTAL));
		new UndoButton(game).colorize(bgc, fgc, disc, buttons);
		new ResetButton(game).colorize(bgc, fgc, disc, buttons);
		new PassButton(game).colorize(bgc, fgc, disc, buttons);
	}
}

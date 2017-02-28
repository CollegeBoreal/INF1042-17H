package gui.widgets;

import gui.StackLayout;
import gui.widgets.buttons.GameButton;
import gui.widgets.panels.FlatPanel;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Event;
import java.awt.Panel;
import java.util.BitSet;

import logic.Fanorona;
import logic.engine.Game;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// UI for changing game parameters

public class Options extends FlatPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6960516569476845084L;
	// checkbox groups
	public static final int BOARD_ORIENTATION = 0;
	public static final int WHO_GOES_FIRST = 1;
	public static final int WHO_IS_WHITE = 2;
	public static final int WHO_IS_BLACK = 3;
	public static final int HOW_FAST = 4;
	public static final int MULTIPLE_CAPTURES = 5;
	public static final int NOTATION_TYPE = 6;
	public static final int NOTATION_LENGTH = 7;
	public static final int NUMBER_OF_GROUPS = 8;
	static CheckboxGroup groups[] = null;

	// checkbox parameters
	public static final int BLACK_AT_TOP = 0;
	public static final int WHITE_AT_TOP = 1;
	public static final int BLACK_GOES_FIRST = 2;
	public static final int WHITE_GOES_FIRST = 3;
	public static final int HUMAN_PLAYS_BLACK = 4;
	public static final int COMPUTER_PLAYS_BLACK = 5;
	public static final int HUMAN_PLAYS_WHITE = 6;
	public static final int COMPUTER_PLAYS_WHITE = 7;
	public static final int FAST_SPEED = 8;
	public static final int MEDIUM_SPEED = 9;
	public static final int SLOW_SPEED = 10;
	public static final int NO_MULTIPLE_CAPTURES = 11;
	public static final int MULTIPLE_CAPTURES_OK = 12;
	public static final int NOTATION_ALGEBRAIC = 13;
	public static final int NOTATION_NUMERIC = 14;
	public static final int NOTATION_LONG = 15;
	public static final int NOTATION_SHORT = 16;
	public static final int ONE_PLY = 17;
	public static final int TWO_PLY = 18;
	public static final int THREE_PLY = 19;
	public static final int NUMBER_OF_PARAMS = 20;
	public static Option gameOptions[] = {
			new Option("Human plays the black pieces", HUMAN_PLAYS_BLACK,
					false, WHO_IS_BLACK),
			new Option("Computer plays the black pieces", COMPUTER_PLAYS_BLACK,
					true, WHO_IS_BLACK),
			new Option("Human plays the white pieces", HUMAN_PLAYS_WHITE, true,
					WHO_IS_WHITE),
			new Option("Computer plays the white pieces", COMPUTER_PLAYS_WHITE,
					false, WHO_IS_WHITE),
			new Option("Black's pieces start at top of board", BLACK_AT_TOP,
					true, BOARD_ORIENTATION),
			new Option("White's pieces start at top of board", WHITE_AT_TOP,
					false, BOARD_ORIENTATION),
			new Option("Black makes the first move", BLACK_GOES_FIRST, false,
					WHO_GOES_FIRST),
			new Option("White makes the first move", WHITE_GOES_FIRST, true,
					WHO_GOES_FIRST),
			new Option("Algebraic notation", NOTATION_ALGEBRAIC, true,
					NOTATION_TYPE),
			new Option("Numeric notation", NOTATION_NUMERIC, false,
					NOTATION_TYPE),
			new Option("Short move log format", NOTATION_SHORT, true,
					NOTATION_LENGTH),
			new Option("Long move log format", NOTATION_LONG, false,
					NOTATION_LENGTH),
			new Option("No multiple captures on first move",
					NO_MULTIPLE_CAPTURES, true, MULTIPLE_CAPTURES),
			new Option("Multiple captures on first move ok",
					MULTIPLE_CAPTURES_OK, false, MULTIPLE_CAPTURES), };
	public static Option levelOptions[] = {
			new Option("One-ply fixed depth search", ONE_PLY, true, HOW_FAST),
			new Option("Two-ply fixed depth search", TWO_PLY, false, HOW_FAST),
			new Option("Three-ply fixed depth search", THREE_PLY, false,
					HOW_FAST),
			new Option("1/4 to 1 1/2 seconds/move", FAST_SPEED, false, HOW_FAST),
			new Option("1 1/2 to 10 seconds/move", MEDIUM_SPEED, false,
					HOW_FAST),
			new Option("10 to 60 seconds/move", SLOW_SPEED, false, HOW_FAST), };

	// code to add matching pairs of left and right panels
	transient Panel left = null;
	transient Panel right = null;

	void switchSides() {
		if (right != null)
			finishPanels();
		Panel p = new FlatPanel();
		p.setLayout(new StackLayout(StackLayout.VERTICAL));
		if (left == null)
			left = p;
		else
			right = p;
	}

	Panel finishedPanel() {
		Panel p = new FlatPanel();
		p.setLayout(new StackLayout(StackLayout.HORIZONTAL));
		if (right != null) {
			p.add("Wide", left);
			p.add("Wide", right);
		} else
			p.add("Center", left);
		left = right = null;
		return p;
	}

	void finishPanels() {
		if (left == null)
			return;
		add("Wide", finishedPanel());
	}

	void addComponent(String s, Component c) {
		if (right != null)
			right.add(s, c);
		else {
			if (left == null)
				switchSides();
			left.add(s, c);
		}
	}

	void addOptions(Option optArray[], Container c, Game g, Color bg) {
	}

	// main inialization of options panel
	Game game;
	Option options[];

	public Options(Game g, Color bg, Color but, Option opts[]) {
		game = g;
		options = opts;

		setBackground(bg);
		setLayout(new StackLayout(StackLayout.VERTICAL));

		if (groups == null) {
			game.setParameters(new BitSet(NUMBER_OF_PARAMS));
			groups = new CheckboxGroup[NUMBER_OF_GROUPS];
			for (int i = 0; i < groups.length; i++) {
				groups[i] = new CheckboxGroup();
			}
		}

		// place all options in two-column layout, arranged by CheckboxGroup
		for (int i = 0; i < options.length; i++) {
			options[i].setCheckboxGroup(groups[options[i].group_index]);
			options[i].setGame(g);
			options[i].setBackground(bg);
			addComponent("Left", options[i]);
			if (i < options.length - 1
					&& options[i].group_index != options[i + 1].group_index)
				switchSides();
		}
		finishPanels(); // synchronize left/right

		// buttons
		switchSides(); // make new left panel for cancel
		new OptionCancel(game, this).colorize(but, Color.black, Color.gray,
				left);
		switchSides(); // make new right panel for ok
		new OptionOK(game, this).colorize(but, Color.black, Color.gray, right);
		finishPanels();

		// finish up
		setAllOptions();
		resetAllOptions(); // make checkbox groups synchronize up
	}

	void setAllOptions() {
		for (int i = 0; i < options.length; i++)
			options[i].set();
		if (game.getBoard().previousPosition == null)
			game.resetBoard();
		else
			game.updateParameters();
	}

	void resetAllOptions() {
		for (int i = 0; i < options.length; i++)
			options[i].reset();
		game.updateParameters(); // game's params havent changed but button
									// activity may have
	}

	boolean dirty() {
		for (int i = 0; i < options.length; i++)
			if (options[i].dirty())
				return true;
		return false;
	}

	// explain the need to use the buttons
	public void sayOk() {
		Fanorona.showMessage(this,
				"Confirm option changes by pressing OK button", false);
	}

	public boolean mouseEnter(Event e, int x, int y) {
		if (!dirty() || !isShowing())
			return false;
		sayOk();
		return true;
	}

	// called when a checkbox is changed. make buttons test active.
	public boolean action(Event e, Object what) {
		game.updateParameters();
		if (dirty())
			sayOk();
		return true;
	}
}

class OptionCancel extends GameButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5791530053011660339L;
	Options options;

	public OptionCancel(Game g, Options o) {
		super(g, "Cancel");
		options = o;
	}

	public boolean active() {
		if (options == null)
			return false;
		return options.dirty();
	}

	public void action() {
		options.resetAllOptions();
		Fanorona.showMessage(this, "Option changes cancelled", false);
	}

	public String status() {
		return "Cancel option changes";
	}
}

class OptionOK extends GameButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6409947239803088358L;
	Options options;

	public OptionOK(Game g, Options o) {
		super(g, "OK");
		options = o;
	}

	public boolean active() {
		if (options == null)
			return false;
		return options.dirty();
	}

	public void action() {
		options.setAllOptions();
		Fanorona.showMessage(this, "Options changed", false);
	}

	public String status() {
		return "Confirm option changes";
	}
}

class Option extends Checkbox {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7024410275630114931L;
	int index, group_index;
	Game game;

	void setGame(Game g) {
		game = g;
	}

	Game getGame() {
		return game;
	}

	Option(String t, int i, boolean b, int gi) {
		super(t, null, b);
		game = null;
		index = i;
		group_index = gi;
	}

	void set() {
		if (game == null)
			return;
		if (getState())
			game.getParameters().set(index);
		else
			game.getParameters().clear(index);
	}

	void reset() {
		if (game != null)
			setState(game.getParameters().get(index));
	}

	boolean dirty() {
		return (game != null && getState() != game.getParameters().get(index));
	}
}

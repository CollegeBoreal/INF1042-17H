package logic;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// Top of user interface code

//import java.awt.*;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Panel;

import logic.board.Board;
import logic.board.BoardDisplay;
import logic.engine.Game;
import logic.engine.SearchMother;
import logic.log.MoveLog;
import logic.log.SearchLog;
import gui.*;
import gui.controllers.Undo;
import gui.widgets.ImageComponent;
import gui.widgets.Options;
import gui.widgets.StatusLine;
import gui.widgets.buttons.GameButton;
import gui.widgets.buttons.ThreeStateButton;
import gui.widgets.buttons.UndoButton;
import gui.widgets.panels.FlatPanel;
import gui.widgets.panels.TabPanel;
import gui.widgets.panels.TextPanel;

public class 	Fanorona extends Applet implements Undo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5582368128771697950L;

	Game game = null;

	// TODO Faire la conversion en Scala

	public String getAppletInfo() {
		return "Game of Fanorona. David Eppstein, ICS, UC Irvine, June 1997.";
	}

	// put a short message in the status line
	String message = "Welcome to Fanorona!";

	public void setMessage(String s) {
		message = s;
		showStatus(message);
	}

	public String getMessage() {
		return message;
	}

	public static void showMessage(Component c, String s, boolean permanent) {
		while (c != null) {
			if (c instanceof Fanorona) {
				Fanorona f = (Fanorona) c;
				if (permanent)
					f.setMessage(s);
				else
					f.showStatus(s);
				return;
			}
			c = c.getParent();
		}
	}

	public boolean mouseEnter(Event e, int x, int y) {
		showStatus(getMessage());
		return true;
	}

	// undo the last move
	public void undo() {
		Board b = UndoButton.retro(game);
		if (b != null)
			game.setBoard(b);
	}

	// start up again after browser comes back to our page from another one
	public void start() {
		game.resetBoard();
		setMessage("Welcome to Fanorona!");
		repaint();
	}

	// rules text, also used to determine appropriate font size
	static final String rules[] = {
			"The object of Fanorona is to eat or immobilize all your opponent's pieces.",
			"",
			"Fanorona pieces move by sliding onto adjacent empty positions; to move a piece,",
			"click and drag it, or (if it can only move one way) just click it without dragging.",
			"",
			"There are two ways to eat, forwards and backwards. When your piece moves on a line",
			"containing a contiguous group of opposing pieces, that are next to the empty position",
			"you are moving to, you may eat the whole group. You may also eat a contiguous group",
			"next to one of your pieces, by moving away from it.  However, you may not eat both",
			"ways with the same move; if you move away from some pieces and towards others, you",
			"may eat only one group. Choose which by dragging your piece onto one of its victims.",
			"",
			"On any turn (except possibly the first, depending on the options set) you can move",
			"one piece several times, as long as each move is in a different direction from the",
			"previous one, eats more pieces, and does not return to previously occupied positions.",
			"",
			"If you are able to eat some pieces on your move, you must do so. But once you",
			"have eaten, you do not have to keep eating; you may pass and give up the turn to",
			"your opponent. If you can't eat on your turn, you must instead move one of your",
			"pieces to an adjacent square without eating. If this is impossible, you lose." };

	public TabPanel tab = null;

	// set up applet
	public void init() {
		// give UI precedence over search
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		if (game != null) {
			// already initialized?
			game.resetBoard();
			return;
		}
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		game = new Game();
		new StatusLine(this, game);

		// start setting up UI components
		Color tan = new Color(255, 255, 153);
		Color fawn = new Color(204, 153, 102);
		setLayout(new StackLayout());
		setBackground(Color.white);
		setForeground(Color.black);

		// find a font small enough for the rules to fit on-screen
		int fontDim = 12;
		Font plainFont, boldFont;
		int lsz = -1;
		do {
			plainFont = new Font("Helvetica", Font.PLAIN, fontDim);
			boldFont = new Font("Helvetica", Font.BOLD, fontDim);
			FontMetrics fm = getFontMetrics(plainFont);
			lsz = 0;
			if (fm != null)
				for (int i = 0; i < rules.length; i++) {
					int ruleLength = fm.stringWidth(rules[i]);
					if (ruleLength > lsz)
						lsz = ruleLength;
				}
			fontDim--;
		} while (lsz > 480);

		setFont(plainFont);
		ThreeStateButton.font = boldFont;
		ThreeStateButton.metric = PatchFontMetrics
				.patch(getFontMetrics(boldFont));
		tab = new TabPanel();
		tab.setTabFont(boldFont);
		tab.setBackground(tan);
		tab.setShade(fawn);
		tab.setForeground(Color.black);
		add("Fill", tab);

		// make main game board panel
		Panel board = new FlatPanel();
		board.setBackground(tan);
		board.add("Width=440 Tall", new FlatPanel()); // ugly way of getting
														// some filler
		board.setLayout(new StackLayout(StackLayout.VERTICAL));
		Panel space = new FlatPanel();
		space.setBackground(Color.black);
		space.setLayout(new StackLayout(StackLayout.VERTICAL));
		space.add("Center", new BoardDisplay(this, game));
		board.add("Width=440 Height=265", space);

		Panel buttonsPanel = new FlatPanel();
		GameButton.buttonsPanel(buttonsPanel, game, fawn, Color.black,
				Color.gray);
		board.add("Width=440 Tall", buttonsPanel);
		tab.add("Board", board);

		// make rule page
		Panel rulesPanel = new FlatPanel();
		tab.add("Rules", rulesPanel);
		rulesPanel.setBackground(tan);
		rulesPanel.setLayout(new StackLayout(StackLayout.VERTICAL));
		TextPanel rulesText = new TextPanel(tan, Color.black);
		rulesPanel.add("", rulesText);
		for (int i = 0; i < rules.length; i++)
			rulesText.addLine(rules[i]);

		// make options panels
		tab.add("Options", new Options(game, tan, fawn, Options.gameOptions));
		tab.add("Level", new Options(game, tan, fawn, Options.levelOptions));

		// Log panels.
		tab.add("Move Log", new MoveLog(game));
		SearchLog searchLog = new SearchLog(game);
		tab.add("Search Log", searchLog);

		// make about box
		Panel about = new FlatPanel();
		tab.add("About", about);
		about.setBackground(tan);
		about.setLayout(new StackLayout(StackLayout.VERTICAL));
		ImageComponent banner = new ImageComponent(getImage(getDocumentBase(),
				"images/Fanorona.jpg"));
		about.add("Left", banner);
		TextPanel aboutText = new TextPanel(tan, Color.black);
		about.add("Center", aboutText);
		aboutText.addLine("Fanorona, the royal game of Madagascar");
		aboutText.addLine("");
		aboutText
				.addLine("Programmed by David Eppstein, Dept. Inf. & Comp. Sci., UC Irvine");
		aboutText.addLine("http://www.ics.uci.edu/~eppstein/");
		aboutText.addLine("");
		aboutText
				.addLine("Thanks to Bruce Miller for the Stack Layout and Tabbed Panel code");
		aboutText.addLine("");
		aboutText
				.addLine("Rules and terminology taken from \"Fanorona: History, Rules, and");
		aboutText
				.addLine("Strategy\", Leonard Fox, director, International Fanorona Assn.,");
		aboutText.addLine("published by Northwest Corner, Inc., 1987");

		// get computer searcher ready to go
		new SearchMother(game, searchLog);
	}

	// no image no flash
	public void update(Graphics g) {
		paint(g);
	}
}

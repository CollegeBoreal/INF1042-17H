package logic.board;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// Code for showing current state of the board

//import java.awt.*;
import gui.widgets.ImageUnavailable;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.util.Observable;
import java.util.Observer;

import logic.Fanorona;
import logic.engine.Game;
import logic.engine.MoveGenerator;

class BoardDisplayRow {
	public int top, bottom, left, right;

	public BoardDisplayRow(int t, int b, int l, int r) {
		top = t;
		bottom = b;
		left = l;
		right = r;
	}

	public boolean containsRow(int row) {
		return row >= top && row <= bottom;
	}

	public int indexOfCol(int col) {
		return ((col - left) * 9) / (right + 1 - left);
	}

	public int leftCol(int sq) {
		return left + sq * (right + 1 - left) / 9;
	}

	public int rightCol(int sq) {
		return leftCol(sq + 1) - 1;
	}
}

class BoardDisplayCell {
	BoardDisplay display;
	int row, col;
	Image empty, white, black;
	int top, bottom, left, right;
	static MediaTracker tracker = null;

	static void track() {
		if (tracker != null) {
			try {
				tracker.waitForID(0);
			} catch (InterruptedException e) {
				throw new ImageUnavailable();
			}
		}
		tracker = null;
	}

	public BoardDisplayCell(BoardDisplay b, int r, int c) {
		display = b;
		row = r;
		col = c;
		top = b.rows[row].top;
		bottom = b.rows[row].bottom;
		left = b.rows[row].leftCol(col);
		right = b.rows[row].rightCol(col);
		ImageFilter f = new CropImageFilter(left, top, right - left + 1, bottom
				- top + 1);
		empty = b.createImage(new FilteredImageSource(b.emptyBoard.getSource(),
				f));
		white = b.createImage(new FilteredImageSource(b.whiteBoard.getSource(),
				f));
		black = b.createImage(new FilteredImageSource(b.blackBoard.getSource(),
				f));
		if (tracker == null)
			tracker = new MediaTracker(b);
		tracker.addImage(empty, 0);
		tracker.addImage(white, 0);
		tracker.addImage(black, 0);
	}

	public void paint(Graphics g) {
		track();
		long m = display.game.getBoard().myPieces;
		long o = display.game.getBoard().opponentPieces;
		long b = Bits.at(row, col);
		Image i;
		if (((m | o) & b) == 0)
			i = empty;
		else if (((m & b) == 0) ^ ((m & Bits.IS_WHITE) != 0))
			i = white;
		else
			i = black;
		g.drawImage(i, left, top, display);
	}
}

public class BoardDisplay extends Canvas implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7368546043446271376L;
	Game game;
	long mouseDown;

	public Image emptyBoard, whiteBoard, blackBoard;
	public BoardDisplayRow rows[] = { new BoardDisplayRow(16, 46, 38, 361),
			new BoardDisplayRow(47, 77, 35, 364),
			new BoardDisplayRow(78, 110, 32, 367),
			new BoardDisplayRow(111, 145, 28, 371),
			new BoardDisplayRow(146, 180, 25, 374) };
	public BoardDisplayCell cells[][];

	public BoardDisplay(java.applet.Applet applet, Game g) {
		game = g;
		game.addObserver(this);

		java.net.URL documentBase = applet.getDocumentBase();
		emptyBoard = applet.getImage(documentBase, "images/Board.png");
		whiteBoard = applet.getImage(documentBase, "images/White.png");
		blackBoard = applet.getImage(documentBase, "images/Black.png");

		// make sure we have images before trying to crop them
		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(emptyBoard, 0);
		tracker.addImage(whiteBoard, 1);
		tracker.addImage(blackBoard, 2);
		try {
			tracker.waitForID(0);
			tracker.waitForID(1);
			tracker.waitForID(2);
		} catch (InterruptedException e) {
			throw new ImageUnavailable();
		}

		cells = new BoardDisplayCell[5][];
		for (int i = 0; i < 5; i++) {
			cells[i] = new BoardDisplayCell[9];
			for (int j = 0; j < 9; j++)
				cells[i][j] = new BoardDisplayCell(this, i, j);
		}
	}

	public Dimension minimumSize() {
		// return new Dimension(emptyBoard.getWidth(this),
		// emptyBoard.getHeight(this));
		return new Dimension(400, 225); // otherwise Metrowerks Pro 2 Java skooz
										// up
	}

	public Dimension preferredSize() {
		return minimumSize();
	}

	// update screen display when a move is made
	private Board lastUpdate = null;

	public void update(Observable updatedGame, Object updatedObject) {
		if (updatedGame != game)
			throw new IllegalArgumentException(
					"Board display: updated object is not game");
		if (!(updatedObject instanceof Board))
			return;
		if (!isShowing())
			return;
		Board b = (Board) updatedObject;
		if (b.previousPosition != lastUpdate || !b.wasPass())
			repaint();
		lastUpdate = b;
	}

	// no image no flash
	public void update(Graphics g) {
		paint(g);
	}

	public void paintPieces(Graphics g, long pieces) {
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 9; j++)
				if ((pieces & Bits.at(i, j)) != 0)
					cells[i][j].paint(g);
	}

	Image offscreenImage = null;
	Graphics offscreenGraphics = null;

	public void paint(Graphics g) {
		if (offscreenImage == null) {
			offscreenImage =
				createImage(emptyBoard.getWidth(this), emptyBoard.getHeight(this));
			offscreenGraphics = offscreenImage.getGraphics();
		}
		offscreenGraphics.drawImage(emptyBoard, 0, 0, this);
		Board board = game.getBoard();
		paintPieces(offscreenGraphics, board.myPieces | board.opponentPieces);
		g.drawImage(offscreenImage, 0, 0, this);
	}

	// put a short message in the status line
	public void showMessage(String s, boolean p) {
		Fanorona.showMessage(this, s, p);
	}

	// translate mouse clicks into bit representing board cell
	// returns zero if click was off-board
	long translateMouse(int x, int y) {
		for (int i = 0; i < 5; i++)
			if (rows[i].containsRow(y)) {
				int j = rows[i].indexOfCol(x);
				if (j < 0 || j >= 9)
					return 0;
				return Bits.at(i, j);
			}
		return 0;
	}

	// handle mouse events. mouse down just stores info for later mouse up.
	public boolean mouseDown(Event e, int x, int y) {
		if (!game.humanToMove()) {
			mouseDown = 0;
			return true;
		}
		mouseDown = translateMouse(x, y);
		return true;
	}

	// mouse up tries to find a move matching the clicked pieces
	public boolean mouseUp(Event e, int x, int y) {
		if (mouseDown == 0 || !game.humanToMove())
			return false;
		long mouseUp = translateMouse(x, y);
		if (mouseUp == 0)
			return true;
		long bits = mouseDown | mouseUp;

		Board board = game.getBoard();
		MoveGenerator mg = new MoveGenerator(board);
		Board newBoard = null;
		if (!mg.hasMoreElements()) {
			showMessage("Game over, no more moves available", false);
			return true;
		}
		while (mg.hasMoreElements()) {
			long changed = mg.nextElement();
			if ((bits & changed) == bits) {
				if (newBoard != null) {
					if (mouseDown == mouseUp)
						showMessage(
								"Ambiguous move; click and drag a piece to a new location",
								false);
					else
						showMessage(
								"Ambiguous move; drag your piece onto a piece you wish to eat",
								false);
					return true;
				}
				newBoard = new Board(board, changed);
			}
		}
		if (newBoard == null)
			showMessage("Illegal move", false);
		else {
			if (game.mustPass(newBoard))
				newBoard = new Board(newBoard, 0); // forced pass?
			game.setBoard(newBoard); // make move (and forced pass)
			if (newBoard.midCapture())
				showMessage("Continue eating with the same piece, or pass",
						true);
		}
		return true;
	}
}

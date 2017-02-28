package gui.widgets.buttons;

import gui.PatchFontMetrics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

// David Eppstein, UC Irvine, 11 Jun 1997
//
// Disableable button with 3d appearance

public abstract class ThreeStateButton extends Canvas {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1086999693593372459L;
	public static Font font = null; // for button labels
	public static FontMetrics metric = null;
	static final int margin = 4;

	String text;
	int width, height;
	boolean depressed;
	boolean enabled;
	Color disColor;

	public ThreeStateButton(String s) {
		if (font == null) {
			font = new Font("Helvetica", Font.BOLD, 12);
			metric = PatchFontMetrics.patch(getFontMetrics(font));
		}
		text = s;
		width = metric.stringWidth(s) + 2 * margin;
		height = metric.getHeight() + 2 * margin;
		depressed = false;
		enabled = true;
	}

	public Dimension minimumSize() {
		return new Dimension(width, height);
	}

	public Dimension preferredSize() {
		return minimumSize();
	}

	public void enable() {
		enabled = true;
		repaint();
	}

	public void disable() {
		enabled = false;
		repaint();
	}

	public void setDisableColor(Color c) {
		disColor = c;
	}

	boolean inRange(int x, int y) {
		return (x >= 0 && x < width && y >= 0 && y < height);
	}

	public boolean mouseDown(Event ev, int x, int y) {
		if (inRange(x, y) && enabled) {
			depressed = true;
			repaint();
		}
		return true;
	}

	public boolean mouseUp(Event ev, int x, int y) {
		if (inRange(x, y) && depressed && enabled)
			action();
		depressed = false;
		repaint();
		return true;
	}

	public void paint(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(1, 1, width - 3, height - 3);
		g.draw3DRect(1, 1, width - 3, height - 3, !depressed);
		Color c;
		if (enabled)
			c = getForeground();
		else
			c = disColor;
		g.setColor(c);
		g.drawRect(0, 0, width - 1, height - 1);
		g.setFont(font);
		g.drawString(text, margin, height - margin - metric.getDescent());
	}

	// shortcut for combining standard initialization routines
	public void colorize(Color bg, Color fg, Color dc, Container c) {
		setBackground(bg);
		setForeground(fg);
		setDisableColor(dc);
		c.add("", this);
	}

	// do whatever it is the button is supposed to do
	public abstract void action();
}

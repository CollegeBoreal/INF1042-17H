package gui.widgets;

// David Eppstein, UC Irvine, 11 Jun 1997
//
// Netscape makes Label take a lot of room. Attempt to hack around it.

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Label;

public class ThinLabel extends Label {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8680243271276645753L;

	public ThinLabel(String s) {
		super(s);
	}

	public Dimension minimumSize() {
		Dimension d = super.minimumSize();
		// FontMetrics metric =
		// PatchFontMetrics.patch(getFontMetrics(getFont()));
		// return new Dimension(d.width, metric.getHeight()+1);
		return new Dimension(d.width, 15);
	}

	public Dimension preferredSize() {
		return minimumSize();
	}

	public void update(Graphics g) {
		paint(g);
	}
}

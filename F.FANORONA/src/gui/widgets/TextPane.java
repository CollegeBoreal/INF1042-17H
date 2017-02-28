package gui.widgets;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// Pane of text added to line by line, for search and move logs
// User interface looks like List
//
// TextArea has the following bugs: once viewed in Metrowerks Java, it will not notice
// if it becomes invisible again, and keep displaying.  In Netscape 3/Mac, the scroll
// bars will never be activated.  Trying to use setText() instead of appendText() just
// makes it worse: it starts scrolling the screen while invisible, even if it has never
// been displayed yet (and even in MRJ 2.0).  In Netscape 3/Solaris, it throws up all sorts of subprocess
// diagnostic windows about Xt problems with the horizontal scrollbars.
//
// Using List instead of TextArea is only marginally better; it doesn't scroll horizontally
// even when lines get too long, and one can no longer select pieces of text.
// Morover, adding to TextAreas or Lists is very slow, slow enough that at 1/4 sec/move
// most of the time gets used up putting a dozen lines in the search log, and only a
// fraction of the time actually gets used in the search.
//
// My current strategy is to use setText() anyway, but only when the window becomes visible.
// While searching etc, new text is kept in a string buffer to be added later.

import java.awt.Event;
import java.awt.Graphics;
import java.awt.TextArea;

public class TextPane extends TextArea {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6394032941453155357L;
	boolean dirty = false;
	StringBuffer buffer = new StringBuffer();

	synchronized public void addItem(String s) {
		buffer.append(s + System.getProperty("line.separator"));
		dirty = true;
	}

	public void clear() {
		buffer.setLength(0);
		dirty = true;
	}

	public boolean isEditable() {
		return true;
	}

	public void clean() {
		if (dirty) {
			dirty = false;
			setText(buffer.toString());
		}
	}

	// gross hack to interface with TabPanel
	public boolean postEvent(Event e) {
		if (e.id == Event.WINDOW_EXPOSE)
			clean();
		return super.postEvent(e);
	}

	// avoid redisplay of text that's about to be changed
	// (note it doesn't help to call clean(), that only increases amount of
	// flashing)
	public void paint(Graphics g) {
		if (!dirty)
			super.paint(g);
	}
}

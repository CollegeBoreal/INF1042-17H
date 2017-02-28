package gui.widgets.panels;

// David Eppstein, UC Irvine, 11 Jun 1997
//
// Panel without annoying redisplay flashes

import java.awt.Graphics;
import java.awt.Panel;

public class FlatPanel extends Panel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7804914198505780882L;

	// no image no flash
	public void update(Graphics g) {
		if (isVisible())
			paint(g);
	}
}

package gui.widgets.panels;

import java.awt.Panel;

// David Eppstein, UC Irvine, 11 Jun 1997
//
// HotJava 1.0 doesnt automatically use the background color when redrawing components.
// Probably it's just doing a clearRect() which uses the browser's color not the component's.
// So anyway, I'm attempting to fix it by replacing clearRect() by fillRect()
// and by overriding update() (the default version of which also calls clearRect).

class OpaquePanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3339312686519082993L;
	// public void paint(Graphics g) {
	// g.setColor(getBackground());
	// Rectangle r = bounds();
	// g.fillRect(r.x, r.y, r.width, r.height);
	// }
	// public void update(Graphics g) { paint(g); }
}

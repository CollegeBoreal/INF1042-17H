/*****************************************************************************
 gui.TabPanel

 Container for a set of Tab Cards or Tab Folders.

 bruce.miller@nist.gov
 Contribution of the National Institute of Standards and Technology,
 not subject to copyright.
 
 
 HACKED BY DAVID EPPSTEIN, UC IRVINE, 15 JUNE 1997
 SEE CHANGES MARKED "[DE]"

 *****************************************************************************/
package gui.widgets.panels;

import gui.PatchFontMetrics;

import java.applet.*;
import java.awt.*;
import java.util.*;

/**
 * **************************************************************************
 * TabPanel is a container for a set of tabbed cards, lying atop each other, but
 * with the labelled tabs exposed at the top. That is, the classic Tab Folder.
 * Each card is an awt.component of whatever design you wish. The topmost card
 * can be selected programmatically (Using first(), last(), next(), previous(),
 * or show(name)), or by clicking on the tab with the mouse.
 * <P>
 * Components should be added using add(name,component)); the name is used to
 * label the tab. If you set the layout manager, it should be a subclass of
 * CardLayout. You probably want to setBackground() to a color contrasting that
 * of the parent and the components.
 * <P>
 * Whenever a card is selected (whether by software or mouse), an event with id
 * = Event.WINDOW_EXPOSE is sent to the selected component. Handling this event
 * may be useful for deferred initialization.
 * 
 * @author Bruce R. Miller (bruce.miller@nist.gov)
 * @author Contribution of the National Institute of Standards and Technology,
 * @author not subject to copyright.
 */
public class TabPanel extends Panel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3019265267366374006L;

	/** The width of the margins around the cards. */
	public int margin = 3; // width of margins around cards

	Font tabFont; // for tab labels
	FontMetrics metric;
	int nCards = 0; // total # of cards
	Vector<String> names = new Vector<String>(10, 10); // contains the
														// (interned) card names
	int pos[], width[]; // position & width of each tab
	int selected = 0; // index of selected (displayed) card
	int offset = 0; // left shift to allow for `too many' tabs
	int tabH; // height of tab (set from tabFont)
	int tabN = 12, // #points along edges: must = (2*int + 2)
			tabLeft[][] = new int[2][tabN], // coordinates of tab edge curves
			tabRight[][] = new int[2][tabN];
	// begin changes [DE]
	//      
	// additional color for background tabs
	Color shade = null;

	public void setShade(Color c) {
		shade = c;
	}

	public Color getShade() {
		if (shade != null)
			return shade;
		else
			return getBackground();
	}

	//
	// end changes [DE]

	/** Creates an empty TabPanel. */
	public TabPanel() {
		setLayout(new CardLayout());
		setTabFont(new Font("Helvetica", Font.BOLD, 12));
	}

	/***************************************************
	 * internals
	 */

	int findComponent(Component c) { // find index of a given component
		for (int i = 0; i < nCards; i++)
			if (getComponent(i) == c)
				return i;
		return -1;
	}

	/***************************************************
	 * Adding & Removing components
	 */

	/** Add a card, component, to the TabPanel with a given name. */
	public Component add(String name, Component component) {
		name = name.intern();
		super.add(name, component); // Let layout manager do its job
		// component.setBackground(getBackground()); // use same background
		// color.
		if (!names.contains(name)) { // if name isn't already present
			names.addElement(name); // but record our part
			nCards++;
			if (isShowing()) { // already showing? better rebuild!
				computeTabs();
				repaint();
			}
		}
		return component;
	}

	/** remove the card, component, from the TabPanel. */
	public void remove(Component component) {
		int i = findComponent(component);
		super.remove(component); // Let layout manager do its job
		names.removeElementAt(i); // but we'll record our part.
		nCards--;
		if (i < selected) {
			setSelected(selected - 1, true);
		} else if ((i == selected) && (nCards > 0)) { // was selected, select
														// another
			setSelected(selected % nCards, true);
		}
		if (isShowing()) { // already showing? better rebuild!
			computeTabs();
			repaint();
		}
	}

	/** remove the card having the given name from the TabPanel. */
	public void remove(String name) {
		int i = names.indexOf(name.intern());
		if (i != -1)
			remove(getComponent(i));
	}

	/** remove all cards from the TabPanel. */
	public void removeAll() {
		super.removeAll();
		names.removeAllElements();
		repaint();
	}

	/***************************************************
	 * Component Selection
	 */

	void setSelected(int i, boolean force) {
		if (force || ((i != selected) && (i >= 0) && (i < nCards))) {
			if (nCards > 0) {
				selected = i % nCards;
			}
			((CardLayout) getLayout()).show(this, (String) names.elementAt(i));
			repaint();
			Component c = getComponent(i);
			c.postEvent(new Event(c, Event.WINDOW_EXPOSE, this));
		}
	}

	/** Select the first card in the Panel. */
	public void first() {
		setSelected(0, false);
	}

	/** Select the last card in the Panel. */
	public void last() {
		setSelected(nCards - 1, false);
	}

	/** Select the next card in the Panel. */
	public void next() {
		setSelected((selected + 1) % nCards, false);
	}

	/** Select the previous card in the Panel. */
	public void previous() {
		setSelected((selected - 1 + nCards) % nCards, false);
	}

	/** Select the named card in the Panel. */
	public void show(String name) {
		setSelected(names.indexOf(name.intern()), false);
	}

	/** Select the card component in the Panel. */
	public void show(Component component) {
		setSelected(findComponent(component), false);
	}

	int cardAt(int x, int y) {
		if (y <= tabH) { // inside tab section?
			x += offset;
			for (int i = 0; i < nCards; i++)
				if ((pos[i] <= x) && (x < pos[i + 1]))
					return i;
		}
		return -1;
	}

	/**
	 * Return a mouse documentation string for selecting this card. (ie. the
	 * applet status line (if there is an applet), for when the mouse is over
	 * the tab). This may be overridden by a subclass, if desired. The default
	 * is to use the "Select tab card " + name.
	 */
	public String documentCard(String name) {
		return "Select tab card " + name;
	}

	Applet applet = null;

	/** Handle mouse clicks and documentation line for Tab selection. */
	public boolean handleEvent(Event e) {
		if (e.id == Event.MOUSE_DOWN) {
			int i = cardAt(e.x, e.y);
			if (i != -1) {
				setSelected(i, false);
				return true;
			}
		} else if (e.id == Event.MOUSE_MOVE) {
			if (applet == null) {
				Component c = getParent();
				while (c != null) {
					if (c instanceof Applet)
						applet = (Applet) c;
					c = c.getParent();
				}
			}
			if (applet != null) {
				int i = cardAt(e.x, e.y);
				if (i != -1)
					applet
							.showStatus(documentCard((String) names
									.elementAt(i)));
			}
		} else if (e.id == Event.MOUSE_EXIT) {
			if (applet != null)
				applet.showStatus("");
		}
		return super.handleEvent(e);
	}

	/***************************************************
	 * Methods involving size and layout
	 */

	/** Allocates extra margins to give the cards some `body'. */
	public Insets insets() {
		return new Insets(tabH + margin, margin, margin, margin);
	}

	/**
	 * Specify the Font to be used for labeling the Tabs. This avoids getting in
	 * the way of cards inheriting default fonts from the TabPanel's container.
	 */
	public void setTabFont(Font font) {
		tabFont = font;
		metric = PatchFontMetrics.patch(getFontMetrics(font));
		int r = (metric.getHeight() + 1) / 2;
		tabH = 2 * r;
		// Compute boundaries for the tab edges.
		int c, s, nn = (tabN - 2) / 2;
		double a;
		for (int i = 0; i <= nn; i++) {
			a = Math.PI * i / 2 / nn;
			c = (int) (r * Math.cos(a));
			s = (int) (r * Math.sin(a));
			tabLeft[0][i] = s;
			tabLeft[1][i] = r + c;
			tabLeft[0][i + nn] = tabH - c;
			tabLeft[1][i + nn] = r - s;
		}
		tabLeft[0][2 * nn + 1] = tabH;
		tabLeft[1][2 * nn + 1] = tabH;
		for (int i = 0; i < tabN; i++) {
			tabRight[0][i] = -tabLeft[0][i];
			tabRight[1][i] = tabLeft[1][i];
		}
	}

	void computeTabs() { // Compute positions of the tabs.
		if ((pos == null) || (pos.length <= nCards)) {
			width = new int[nCards + 1];
			pos = new int[nCards + 1];
		} // make sure pos & width are big enough.
		int x = tabH / 2;
		for (int i = 0; i < nCards; i++) { // size the tabs & reshape the panes.
			pos[i] = x;
			width[i] = tabH + metric.stringWidth((String) names.elementAt(i));
			x += width[i];
		}
		pos[nCards] = x;
	}

	/** Computes tab geometry while laying out the panels components. */
	public void layout() {
		super.layout();
		computeTabs();
	} // make sure tabs are computed.

	/***************************************************
	 * Painting the tabs
	 */

	//
	// start changed code [DE]
	//
	// drawPolygon is documented to draw a *CLOSED* polygon, but here we want an
	// open one
	void openPolygon(Graphics g, int xPoints[], int yPoints[], int nPoints) {
		for (int i = 0; i < nPoints - 1; i++)
			g.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
	}

	//
	// add extra color args to paintTabEdge and paintTab
	//
	void paintTabEdge(Graphics g, int x, int edges[][], Color c) {
		g.translate(x, 0);
		g.setColor(c);
		g.fillPolygon(edges[0], edges[1], tabN);
		// fillPolygon doesn't quite get everything in Apple MRJ 1.5b1
		// appletViewer [DE]
		g.drawLine(edges[0][tabN - 2], edges[1][tabN - 2], edges[0][tabN - 1],
				edges[1][tabN - 1]);
		g.setColor(getForeground());
		// g.drawPolygon(edges[0],edges[1],tabN-1); // leave off corner on
		// outline!
		// use my new openPolygon routine [DE]
		openPolygon(g, edges[0], edges[1], tabN - 1);
		g.translate(-x, 0);
	}

	void paintTab(Graphics g, int x, int p, Color c) {
		int r = tabH / 2, w = width[p];
		paintTabEdge(g, x - r, tabLeft, c);
		paintTabEdge(g, x + w + r, tabRight, c);
		// for some reason, this draws in the wrong place on window `repair'!!!
		// g.clearRect(x+r,0,w-tabH,tabH);
		g.setColor(c);
		g.fillRect(x + r, 0, w - tabH, tabH);
		g.setColor(getForeground());
		g.drawLine(x + r, 0, x + w - r, 0);
		g.setFont(tabFont);
		g.drawString((String) names.elementAt(p), x + r, tabH
				- metric.getDescent());
	}

	/** Paint the tabs in a row atop the cards. */
	public void paint(Graphics g) {
		Dimension sz = getSize();
		int x, w = sz.width - 1, h = sz.height - 1, r = tabH / 2;
		int j, s = selected;
		// These come into play when the tabs span more than the panel width.
		// Show some `shadow' tabs at the ends to represent those not displayed.
		int shadow = 4; // how wide of a shadow (pixels) to show
		int nShadows = 3; // max # of shadow tabs to show.

		// Fill in tab area in the PARENT's color
		g.setColor(getParent().getBackground());
		g.fillRect(0, 0, w + 1, tabH);
		g.setColor(getForeground());
		if (nCards == 0)
			g.drawLine(0, tabH, w, tabH);
		else {
			// Possibly adjust the offset, so at least the selected tab is
			// visible
			int offmax = pos[s] - r - Math.min(nShadows, s) * shadow, offmin = pos[s + 1]
					- w + r + Math.min(nCards - s, nShadows) * shadow;
			if ((offset < offmin) || (offset > offmax))
				offset = Math.min(Math.max(0, (offmin + offmax) / 2),
						pos[nCards] + r - w);

			// Draw first tabs from the left (offscreen ones only partly
			// visible)
			for (j = 0, x = offset + r; (j < s) && (pos[j] <= x); j++)
				; // find visible
			if (j > 0) {
				x = 0;
				// add extra color args to paintTabEdge and paintTab [DE]
				//
				for (int i = Math.max(0, j - nShadows); i < j - 1; i++, x += shadow)
					paintTabEdge(g, x, tabLeft, getShade());
				paintTab(g, x + r, j - 1, getShade());
			}
			for (int i = j; i < s; i++)
				paintTab(g, pos[i] - offset, i, getShade());

			// Draw last tabs from the right (offscreen ones only partly
			// visible)
			for (j = nCards - 1, x = offset + w - r; (j > s)
					&& (pos[j + 1] >= x); j--)
				;
			if (j < nCards - 1) {
				x = w;
				for (int i = Math.min(nCards - 1, j + nShadows); i > j + 1; i--, x -= shadow)
					paintTabEdge(g, x, tabRight, getShade());
				paintTab(g, x - r - width[j + 1], j + 1, getShade());
			}
			for (int i = j; i > s; i--)
				paintTab(g, pos[i] - offset, i, getShade());

			// now draw the selected tab on top of the others.
			paintTab(g, pos[s] - offset, s, getBackground());
			// and fixup the baseline so the selected is on `top'.
			g.drawLine(0, tabH, pos[s] - r - offset, tabH);
			//
			// This seems to make a gray line between tab and panel. [DE]
			// Instead I want it painted in our background color.
			//
			// g.clearRect(pos[s]-r-offset,tabH,width[s]+tabH,1);
			//
			g.setColor(getBackground());
			g.fillRect(pos[s] - r - offset + 2, tabH, width[s] + tabH - 3, 1);
			g.setColor(getForeground());

			g.drawLine(pos[s + 1] + r - offset, tabH, w, tabH);
			g.drawLine(w, tabH, w, h);
			g.drawLine(w, h, 0, h);
			g.drawLine(0, h, 0, tabH);
		}
	}

	// Double buffered redisplay [DE]

	// Double buffer [DE]
	Image offscreenImage = null;
	Graphics offscreenGraphics = null;
	int offscreenWidth = -1;
	int offscreenHeight = -1;

	public void update(Graphics g) {
		Dimension d = getSize();
		if (d.width != offscreenWidth || tabH + 1 != offscreenHeight) {
			offscreenWidth = d.width;
			offscreenHeight = d.height; // tabH+1 if we omit last three
										// drawLines
			if (offscreenGraphics != null)
				offscreenGraphics.dispose();
			offscreenImage = createImage(offscreenWidth, offscreenHeight);
			offscreenGraphics = offscreenImage.getGraphics();
		}
		offscreenGraphics.clearRect(0, 0, offscreenWidth, offscreenHeight);
		paint(offscreenGraphics);
		g.drawImage(offscreenImage, 0, 0, this);
	}
}

package gui.widgets.panels;

import gui.StackLayout;
import gui.widgets.ThinLabel;

import java.awt.Color;
import java.awt.Label;

// David Eppstein, UC Irvine, 11 Jun 1997
//
// Collection of lines of text *WITHOUT SCROLLBAR*

public class TextPanel extends FlatPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7095542771028978153L;

	public TextPanel(Color background, Color foreground) {
		super();
		setBackground(background);
		setForeground(foreground);
		setLayout(new StackLayout(StackLayout.VERTICAL, 0));
	}

	public void addLine(String s) {
		Label lab = new ThinLabel(s);
		lab.setBackground(getBackground());
		lab.setForeground(getForeground());
		add("Left", lab);
	}
}

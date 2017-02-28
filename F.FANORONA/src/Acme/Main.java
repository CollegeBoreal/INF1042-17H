package Acme;
// Wrapper for Fanorona application
// temporarily extended with menu hackery

import java.awt.MenuItem;
import java.awt.MenuShortcut;

import logic.Fanorona;
import logic.board.Board;
import logic.engine.EndgameDatabase;

public class Main {
	static Fanorona fan = null;

	public static void main(String args[]) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);	// make UI run fast
		Acme.MainFrame.quitItem = new MenuItem("Quit", new MenuShortcut('Q'));
		Acme.MainFrame.newItem = new MenuItem("New Game", new MenuShortcut('N'));
		Acme.MainFrame.undoItem = new MenuItem("Undo", new MenuShortcut('Z'));
		fan = new Fanorona();
		new Acme.MainFrame(fan,args,500,350);							// set up applet
		new EndgameBuilder().start();										// and slowly set up ekpbdb

		// enable "About Fanorona" menu item in MRJ apple menu
		//com.apple.mrj.MRJApplicationUtils.registerAboutHandler(new RunAbout());
	}
}

//class RunAbout implements com.apple.mrj.MRJAboutHandler {	
//	public void handleAbout() {
//		if (Main.fan != null && Main.fan.tab != null) Main.fan.tab.show("About");
//	}
//}

// Separate thread to build endgame database on the back burner
class EndgameBuilder extends Thread {
   public void run() {
   	setPriority(MIN_PRIORITY);
   	yield();
		Board.endgameDatabase = new EndgameDatabase();
   }
}

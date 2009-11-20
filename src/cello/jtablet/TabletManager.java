package cello.jtablet;

import java.awt.Component;

import cello.jtablet.events.TabletListener;
import cello.jtablet.impl.TabletInterface;
import cello.jtablet.impl.jpen.JPenDirectTabletManager;

/**
 * Manages tablet implementations.
 * 
 * @author marcello
 */
public class TabletManager {
	
	private static TabletInterface tabletInterface = new JPenDirectTabletManager();
	
	/**
	 * @param l
	 */
	public static void addScreenTabletListener(TabletListener l) {
		tabletInterface.addScreenTabletListener(l);
	}

	/**
	 * @param l
	 */
	public static void removeScreenTabletListener(TabletListener l) {
		tabletInterface.removeScreenTabletListener(l);
	}

	/**
	 * Add a TabletListener to given Component
	 * @param c 
	 * @param l
	 */
	public static void addTabletListener(Component c, TabletListener l) {
		tabletInterface.addTabletListener(c,l);
	}	
	/**
	 * Remove a TabletListener from this Component
	 * @param c 
	 * @param l
	 */
	public static void removeTabletListener(Component c, TabletListener l) {
		tabletInterface.removeTabletListener(c,l);
	}
}

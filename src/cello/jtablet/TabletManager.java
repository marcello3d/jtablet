package cello.jtablet;

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
	 * @return tablet interface 
	 */
	public static TabletInterface getManager() {
		return tabletInterface;
	}
}

package cello.jtablet;

import cello.jtablet.impl.TabletInterface;
import cello.jtablet.impl.jpen.JPenTabletManager;

/**
 * Manages tablet implementations.
 * 
 * @author marcello
 */
public class TabletManager {
	
	private static TabletInterface tabletInterface = new JPenTabletManager();
	
	/**
	 * @return tablet interface 
	 */
	public static TabletInterface getManager() {
		return tabletInterface;
	}
}

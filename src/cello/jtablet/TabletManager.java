package cello.jtablet;

import java.util.Collections;
import java.util.Map;

import cello.jtablet.impl.jpen.JPenTabletManager;

/**
 * Manages tablet implementations.
 * 
 * @author marcello
 */
public class TabletManager {
	
	private static TabletInterface tabletInterface = createManager(Collections.EMPTY_MAP);
	
	/**
	 * @return tablet interface 
	 */
	public static TabletInterface getManager() {
		return tabletInterface;
	}
	/**
	 * @return tablet interface 
	 */
	public static TabletInterface createManager(Map<String,Object> hints) {
		return new JPenTabletManager(hints);
	}
}

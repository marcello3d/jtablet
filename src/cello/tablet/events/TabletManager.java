package cello.tablet.events;

import java.awt.Component;

import cello.tablet.events.jpen.JPenTabletManager;

/**
 * Manages tablet implementations.
 * 
 * @author marcello
 */
public abstract class TabletManager {
	
	
	/**
	 * @param component
	 * @return build a TabletManager for this system
	 */
	public static TabletManager getManager(Component component) {
		return new JPenTabletManager(component);
	}
	

	/**
	 * Add a TabletListener to this Component
	 * @param l
	 */
	public abstract void addTabletListener(TabletListener l);
	
	/**
	 * Remove a TabletListener from this Component
	 * @param l
	 */
	public abstract void removeTabletListener(TabletListener l);
}

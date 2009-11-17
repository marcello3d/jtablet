package cello.jtablet;

import java.awt.Component;
import java.util.Map;
import java.util.WeakHashMap;

import cello.jtablet.events.TabletListener;
import cello.jtablet.impl.jpen.JPenTabletManager;

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
	
	private static final Map<Component,TabletManager> managers = new WeakHashMap<Component, TabletManager>();

	/**
	 * @param l
	 */
	public static void addScreenTabletListener(TabletListener l) {
		addTabletListener0(null,l);
	}

	/**
	 * @param l
	 */
	public static void removeScreenTabletListener(TabletListener l) {
		removeTabletListener0(null,l);
	}

	/**
	 * Add a TabletListener to given Component
	 * @param c 
	 * @param l
	 */
	public static void addTabletListener(Component c, TabletListener l) {
		if (c == null) {
			throw new IllegalArgumentException("Component is null");
		}
		addTabletListener0(c, l);
	}

	private static void addTabletListener0(Component c, TabletListener l) {
		synchronized (managers) {
			TabletManager manager = managers.get(c);
			if (manager == null) {
				manager = getManager(c);
				managers.put(c, manager);
			}
			manager.addTabletListener(l);
		}
	}
	
	/**
	 * Remove a TabletListener from this Component
	 * @param c 
	 * @param l
	 */
	public static void removeTabletListener(Component c, TabletListener l) {
		if (c == null) {
			throw new IllegalArgumentException("Component is null");
		}
		removeTabletListener0(c, l);
	}

	private static void removeTabletListener0(Component c, TabletListener l) {
		synchronized (managers) {
			TabletManager manager = managers.get(c);
			if (manager != null) {
				manager.removeTabletListener(l);
			}
		}
	}
	protected abstract void addTabletListener(TabletListener l);
	protected abstract void removeTabletListener(TabletListener l);
}

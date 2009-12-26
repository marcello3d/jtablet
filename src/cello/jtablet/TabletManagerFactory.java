/*!
 * Copyright (c) 2009 Marcello Bastéa-Forte (marcello@cellosoft.com)
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 * 
 *     2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 * 
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */

package cello.jtablet;

import cello.jtablet.event.TabletAdapter;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.jpen.JPenTabletManager;

/**
 * Provides {@link TabletManager} instances based on the current platform. Example usage:
 * <pre>
 *  // Get tablet manager
 *  {@link TabletManager} manager = TabletManagerFactory.getManager();
 *  
 *  // Add tablet listener to component
 *  manager.{@link TabletManager#addTabletListener(java.awt.Component, cello.jtablet.event.TabletListener) addTabletListener}(component, new {@link TabletAdapter}() {
 *      public void {@link TabletListener#cursorDragged(TabletEvent) cursorDragged}({@link TabletEvent} event) {
 *          // Print out tablet drag events as they occur to system output
 *          System.out.println("dragged " + event);
 *      }
 *  });
 * </pre>
 * 
 * @author marcello
 */
public class TabletManagerFactory {
	
	private TabletManagerFactory() {}
	
	private static TabletManager tabletManager = getManager(Hints.DEFAULTS);
	
	/**
	 * Returns a shared tablet manager with the default settings.
	 * @return tablet manager 
	 */
	public static TabletManager getManager() {
		return tabletManager;
	}
	/**
	 * Creates a new tablet manager with the specified hints. 
	 * 
	 * @param hints a map of hints to 
	 * @return tabletmanager 
	 */
	private static TabletManager getManager(Hints hints) {
//		Class<? extends TabletManager> tabletManagerClass = hints.getManagerClass();
		return new JPenTabletManager(hints);
	}
	
	/**
	 * Used to specify hints for manager creation (not currently available).
	 * 
	 * @author marcello
	 */
	public static class Hints {
		private static final Hints DEFAULTS = new Hints();
		
//		private Class<? extends TabletManager> managerClass = JPenTabletManager.class;
//
//		public Class<? extends TabletManager> getManagerClass() {
//			return managerClass;
//		} 
	}
}

package cello.jtablet;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import cello.jtablet.events.TabletListener;

/**
 * @author marcello
 */
public interface TabletInterface {
	/**
	 * Adds a TabletListener to the entire screen.
	 * @param listener
	 */
	public void addScreenTabletListener(TabletListener listener);
	/**
	 * Removes a TabletListener previously added with {@link #addScreenTabletListener(TabletListener)}.
	 * @param listener
	 */
	public void removeScreenTabletListener(TabletListener listener);
	/**
	 * Adds a TabletListener to a specific Component. This works very much like adding a {@link MouseListener} and 
	 * {@link MouseMotionListener} on the component, meaning:
	 * <ul>
	 * 	<li>Events will have coordinates relative to the component</li>
	 *  <li>Events will only be received when: 
	 *   <ul>
	 *     <li>the mouse is over the component's bounds,</li>
	 *     <li>or a drag was initiated on the component</li>
	 *   </ul>
	 *  </li>
	 *  <li>Enter and exit events will occur both:
	 *   <ul>
	 *    <li>when the tablet stylus enters/exits proximity,
	 *    <li>and when the cursor enters/exits the component bounds</li>
	 *   </ul>
	 *  </li>
	 * </ul>
	 * @param component the component to listen on
	 * @param listener
	 */
	public void addTabletListener(Component component, TabletListener listener);
	/**
	 * Removes a TabletListener previously added to a specific component with 
	 * {@link #addTabletListener(Component,TabletListener)}.
	 * @param component
	 * @param listener
	 */
	public void removeTabletListener(Component component, TabletListener listener);
}

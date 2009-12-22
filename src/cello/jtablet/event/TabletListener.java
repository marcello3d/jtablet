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


package cello.jtablet.event;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletManager;


/**
 * The listener interface for receiving tablet events on a component (similar to {@link MouseListener} and
 * {@link MouseMotionListener}).
 * 
 * <p>You can add listeners using {@link TabletManager#addTabletListener(java.awt.Component, TabletListener)} and
 * {@link TabletManager#addScreenTabletListener(TabletListener)}.</p>  
 * 
 * @see TabletEvent
 * @see TabletManager
 * @see TabletManager#addTabletListener(Component, TabletListener)
 * @see TabletManager#addScreenTabletListener(TabletListener)
 * @see TabletDevice
 * @see TabletAdapter
 * @see TabletFunneler
 * @see MouseEvent
 * @author Marcello
 */
public interface TabletListener {
	
	/**
	 * Invoked when the {@link TabletDevice} changes
	 * @param event
	 */
	public void newDevice(TabletEvent event);
	
	/**
	 * Invoked when a button is pressed
	 * @see MouseListener#mousePressed(MouseEvent)
	 * @param event 
	 */
	public void cursorPressed(TabletEvent event);
	
	/**
	 * Invoked when a button is released
	 * @see MouseListener#mouseReleased(MouseEvent)
	 * @param event
	 */
	public void cursorReleased(TabletEvent event);
	
	/**
	 * Invoked either when the cursor enters the given component, either spatially or through proximity.
	 * @see MouseListener#mouseEntered(MouseEvent) 
	 * @param event
	 */
	public void cursorEntered(TabletEvent event);
	
	/**
	 * Invoked either when the cursor exits the given component, either spatially or through proximity.
	 * @see MouseListener#mouseExited(MouseEvent) 
	 * @param event
	 */
	public void cursorExited(TabletEvent event);
	
	/**
	 * Invoked when the cursor is moved.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 * @param event
	 */
	public void cursorMoved(TabletEvent event);
	
	/**
	 * Invoked when the cursor is dragged (pressed+moved).
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 * @param event
	 */
	public void cursorDragged(TabletEvent event);
	
	/**
	 * Invoked when the user scrolls.
	 * @see TabletEvent.Type#SCROLLED
	 * @see TabletEvent#getScrollX()
	 * @see TabletEvent#getScrollY()
	 * @param event
	 */
	public void cursorScrolled(TabletEvent event);

	/**
	 * Invoked when the user executes a gesture.
	 * @see TabletEvent.Type#SWIPED
	 * @see TabletEvent.Type#ROTATED
	 * @see TabletEvent.Type#ZOOMED
	 * @see TabletEvent#getScrollX()
	 * @see TabletEvent#getScrollY()
	 * @see TabletEvent#getRotation()
	 * @see TabletEvent#getZoomFactor()
	 * @param event
	 */
	public void cursorGestured(TabletEvent event);
	
	/**
	 * Invoked when a level has changed. This will occur if some level (pressure, side pressure, tilt, rotation) changes
	 * without the actual position changing. 
	 * @param event
	 */
	public void levelChanged(TabletEvent event);
	
}

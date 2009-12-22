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


package cello.jtablet.events;


/**
 * Adds consumption features to PenListener
 * 
 * @author Marcello
 */
public interface TabletListener {
	/**
	 * Called when the input device changes
	 * @param ev
	 */
	public void newDevice(TabletEvent ev);
	/**
	 * Called when the stylus tip is pressed
	 * @param ev
	 */
	public void cursorPressed(TabletEvent ev);
	/**
	 * Called when the stylus tip is released
	 * @param ev
	 */
	public void cursorReleased(TabletEvent ev);
	/**
	 * Called either when the cursor enters the given component, either spatially or through proximity. 
	 * @param ev
	 */
	public void cursorEntered(TabletEvent ev);
	/**
	 * Called either when the cursor exits the given component, either spatially or through proximity. 
	 * @param ev
	 */
	public void cursorExited(TabletEvent ev);
	/**
	 * Called when the cursor is moved
	 * @param ev
	 */
	public void cursorMoved(TabletEvent ev);
	/**
	 * Called when the cursor is dragged (pressed+moved)
	 * @param ev
	 */
	public void cursorDragged(TabletEvent ev);
	/**
	 * Called when the cursor is scrolled
	 * @param ev
	 */
	public void cursorScrolled(TabletEvent ev);

	/**
	 * Called when the cursor is gestured
	 * @param ev
	 */
	public void cursorGestured(TabletEvent ev);
	/**
	 * Called when a pressure level has changed
	 * @param ev
	 */
	public void levelChanged(TabletEvent ev);
	
}

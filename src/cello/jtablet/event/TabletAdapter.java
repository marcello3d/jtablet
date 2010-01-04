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

import java.awt.event.MouseAdapter;

/**
 * This class is a convenience class that implements {@link TabletListener} as empty methods. You can use it to only
 * implement methods you care about.
 * 
 * @see MouseAdapter 
 * @author marcello
 */
public abstract class TabletAdapter implements TabletListener {
	public void cursorEntered(TabletEvent ev) {}
	public void cursorExited(TabletEvent ev) {}
	public void cursorPressed(TabletEvent ev) {}
	public void cursorReleased(TabletEvent ev) {}
	public void cursorMoved(TabletEvent ev) {}
	public void cursorDragged(TabletEvent ev) {}
	public void levelChanged(TabletEvent ev) {}
	public void cursorGestured(TabletEvent ev) {}
	public void cursorScrolled(TabletEvent ev) {}
}

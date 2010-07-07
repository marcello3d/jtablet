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

import java.awt.Component;
import java.awt.Point;

/**
 * The {@link ScreenComponent} class provides a fake component which
 * purports to take up the entire screen. This class can be used by
 * {@link TabletListener}s wishing to capture events from the entire
 * screen.
 *
 * <p>Note that since this component is not actually realized, it is
 * possible that events occurring outside the Java application will
 * <b>NOT</b> be passed on. Such functionality requires native code
 * to have been loaded to observe the tablet outside of Java's sandbox.</p>
 * 
 * @author marcello
 */
public class ScreenComponent extends Component {
	private static final Point POINT = new Point(0,0);

	public static ScreenComponent INSTANCE = new ScreenComponent();

	private ScreenComponent() {}
	/*
	private java.awt.GraphicsConfiguration getMainScreen() {
		java.awt.GraphicsDevice[] gs = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		if (gs.length > 0) {
			return gs[0].getDefaultConfiguration();
		}
		return null;
	}
	*/
	@Override
	public Point getLocationOnScreen() {
		return POINT;
	}
	@Override
	public java.awt.Rectangle bounds() {
		//return getMainScreen().getBounds();
		return new java.awt.Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
	}
	@Override
	public java.awt.Rectangle getBounds(java.awt.Rectangle rv) {
		if (rv == null) {
			return bounds();
		}
		rv.setBounds(bounds());
		return rv;
	}
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}


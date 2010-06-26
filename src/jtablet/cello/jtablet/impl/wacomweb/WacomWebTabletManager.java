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

package cello.jtablet.impl.wacomweb;

import java.applet.Applet;
import java.awt.Component;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.TabletDevice.Type;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.AbstractTabletDevice;
import cello.jtablet.impl.Architecture;
import cello.jtablet.impl.MouseTabletManager;
import cello.jtablet.impl.NativeLoaderException;
import cello.jtablet.impl.NativeTabletManager;

/**
 * Wrapper for wacom web tablet plugin.
 * @author marcello
 */
public class WacomWebTabletManager extends MouseTabletManager implements NativeTabletManager {

	private WacomWebPlugin plugin;

	public Architecture getArchitecture() {
		return null;
	}

	public boolean isSystemSupported(String os) {
		try {
			Class<?> clazz = Class.forName("netscape.javascript.JSObject");
			return clazz != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public void load() throws NativeLoaderException {
		// We can't do anything until we have an Applet to work with 
	}
	
	private Applet getAppletRoot(Component c) {
		while (true) {
			if (c == null) {
				return null;
			}
			if (c instanceof Applet) {
				return (Applet)c;
			}
			c = c.getParent();
		}
	}
	
	protected MagicListener transformTabletListener(TabletListener l) {
		return new ExtremelyMagicListener(l);
	}

	protected class ExtremelyMagicListener extends MagicListener {
		public ExtremelyMagicListener(TabletListener listener) {
			super(listener);
		}
		@Override
		protected void fireEvent(TabletEvent ev) {
			synchronized (plugin) {
				if (plugin != null) {
					ev = new TabletEvent(
						ev.getComponent(), 
						ev.getType(), 
						ev.getWhen(), 
						ev.getModifiersEx(), 
						0, 
						getDevice(plugin.getPointerType()), 
						ev.getX(), 
						ev.getY(), 
						plugin.getPressure(), 
						plugin.getTiltX(), 
						plugin.getTiltY(), 
						plugin.getTangentialPressure(), 
						plugin.getRotation(), 
						ev.getButton()
					);
				}
			}
			super.fireEvent(ev);
		}
	}


	@Override
	public void addTabletListener(Component c, TabletListener l) {
		// Find an applet associated with this component
		Applet a = getAppletRoot(c);
		if (a != null) {
			synchronized (this) {
				if (plugin != null) {
					plugin = new WacomWebPlugin(a,"wacom-embed");
				}
			}
		}
		super.addTabletListener(c, l);
	}
	
	protected static class WacomWebTabletDevice extends AbstractTabletDevice {
		protected WacomWebTabletDevice(
				Type type, 
				Support pressureSupport,
				Support rotationSupport, 
				Support sidePressureSupport,
				Support tiltSupport) {
			super(type, type.name(), null, Support.NO, Support.NO, Support.NO,
					pressureSupport, rotationSupport, sidePressureSupport, tiltSupport);
		}
	}

	private static final WacomWebTabletDevice ERASER_DEVICE = new WacomWebTabletDevice(
		TabletDevice.Type.ERASER,
		Support.YES,
		Support.UNKNOWN,
		Support.UNKNOWN,
		Support.UNKNOWN
	);

	private static final WacomWebTabletDevice STYLUS_DEVICE = new WacomWebTabletDevice(
		TabletDevice.Type.STYLUS,
		Support.YES,
		Support.UNKNOWN,
		Support.UNKNOWN,
		Support.UNKNOWN
	);

	private static final WacomWebTabletDevice UNKNOWN_DEVICE = new WacomWebTabletDevice(
		TabletDevice.Type.UNKNOWN,
		Support.UNKNOWN,
		Support.UNKNOWN,
		Support.UNKNOWN,
		Support.UNKNOWN
	);
	
	private static final WacomWebTabletDevice MOUSE_DEVICE = new WacomWebTabletDevice(
		TabletDevice.Type.MOUSE,
		Support.NO,
		Support.NO,
		Support.NO,
		Support.NO
	);
	
	protected TabletDevice getDevice(Type type) {
		switch (type) {
			case ERASER:
				return ERASER_DEVICE;
			case MOUSE:
				return MOUSE_DEVICE;
			case STYLUS:
				return STYLUS_DEVICE;
			case UNKNOWN:
				return UNKNOWN_DEVICE;
		}
		return null;
	}
}

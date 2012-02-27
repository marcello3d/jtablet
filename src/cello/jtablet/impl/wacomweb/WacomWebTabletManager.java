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
import java.util.EnumMap;

import cello.jtablet.DriverStatus;
import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.TabletDevice.Type;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.AbstractTabletDevice;
import cello.jtablet.impl.MouseTabletManager;

/**
 * Wrapper for wacom web tablet plugin.
 * @author marcello
 */
public class WacomWebTabletManager extends MouseTabletManager {

	private WacomWebPlugin plugin = null;
    private Applet applet;

    public WacomWebTabletManager() {
        System.out.println("Starting up...");
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

	@Override
    protected MagicListener makeMagicListener(TabletListener l) {
		return new ExtremelyMagicListener(l);
	}

	protected class ExtremelyMagicListener extends MagicListener {
		public ExtremelyMagicListener(TabletListener listener) {
			super(listener);
		}
		@Override
		protected void fireEvent(TabletEvent ev) {
            if (applet == null) {
                System.out.println("Looking for applet from "+ev.getComponent());
                // Find an applet associated with this component
                applet = getAppletRoot(ev.getComponent());
                System.out.println("Applet = "+ applet);
                if (applet != null) {
                    plugin = new WacomWebPlugin(applet,"wacom-embed");
                }
            }
            if (plugin != null) {
                plugin.poll();
                Type pointerType = plugin.getPointerType();
                if (pointerType != Type.UNKNOWN) {
//                    float sysX = plugin.getSysX();
//                    float sysY = plugin.getSysY();
                    ev = new TabletEvent(
                        ev.getComponent(),
                        ev.getType(),
                        ev.getWhen(),
                        ev.getModifiersEx(),
                        0,
                        getDevice(pointerType),
                        ev.getX(),// + (sysX - (int)sysX),
                        ev.getY(),// + (sysY - (int)sysY),
                        plugin.getPressure(),
                        0, //plugin.getTiltX(),
                        0, //plugin.getTiltY(),
                        0, //plugin.getTangentialPressure(),
                        0, //plugin.getRotation(),
                        ev.getButton()
                    );
//                    System.out.println("replaced ev with "+ev);
                }
			}
			super.fireEvent(ev);
		}
	}


	@Override
	public void addTabletListener(Component c, TabletListener listener) {
		super.addTabletListener(c, listener);
	}
	
	protected static class WacomWebTabletDevice extends AbstractTabletDevice {
		protected WacomWebTabletDevice(
                Type type,
                Support floatSupport, Support pressureSupport,
                Support rotationSupport,
                Support sidePressureSupport,
                Support tiltSupport) {
			super(type, type.name(), null, floatSupport, Support.NO, Support.NO,
					pressureSupport, rotationSupport, sidePressureSupport, tiltSupport);
		}
	}

	private static final WacomWebTabletDevice ERASER_DEVICE = new WacomWebTabletDevice(
		TabletDevice.Type.ERASER,
        Support.YES,
        Support.YES,
		Support.UNKNOWN,
		Support.UNKNOWN,
		Support.UNKNOWN
    );

	private static final WacomWebTabletDevice STYLUS_DEVICE = new WacomWebTabletDevice(
		TabletDevice.Type.STYLUS,
        Support.YES,
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
		Support.UNKNOWN,
		Support.UNKNOWN
    );
	
	private static final WacomWebTabletDevice MOUSE_DEVICE = new WacomWebTabletDevice(
		TabletDevice.Type.MOUSE,
        Support.UNKNOWN,
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

    @Override
    public DriverStatus getDriverStatus() {
        return plugin != null ?
                new DriverStatus(DriverStatus.State.WEB_PLUGIN) :
                new DriverStatus(DriverStatus.State.NOT_INSTALLED);
    }
}

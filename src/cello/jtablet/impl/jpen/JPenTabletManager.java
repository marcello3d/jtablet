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

package cello.jtablet.impl.jpen;

import java.awt.Component;
import java.util.Map;

import cello.jtablet.TabletManager;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.PhysicalTabletInterface;
import cello.jtablet.impl.jpen.platform.NativeCocoaInterface;
import cello.jtablet.impl.jpen.platform.NativeWinTabInterface;
import cello.jtablet.impl.jpen.platform.NativeXInputInterface;
import cello.jtablet.impl.platform.NativeCursorDevice;
import cello.jtablet.impl.platform.NativeDeviceException;

public class JPenTabletManager implements TabletManager {

	private final Class<?> interfaces[] = {
		NativeCocoaInterface.class,
		NativeWinTabInterface.class,
		NativeXInputInterface.class,
		JPenTranslationInterface.class
	};
	private final PhysicalTabletInterface cursorDevice;
	private Exception exception;
	private DriverStatus driverStatus; 
	
	public JPenTabletManager(Map<String, Object> hints) {
		String os = System.getProperty("os.name").toLowerCase();
		
		PhysicalTabletInterface chosenDevice = null;
		driverStatus = DriverStatus.OS_NOT_SUPPORTED;
		for (Class<?> cdClazz : interfaces) {
			try {
				PhysicalTabletInterface cd = (PhysicalTabletInterface)cdClazz.newInstance();
				cd.setHints(hints);
				if (cd instanceof NativeCursorDevice) {
					NativeCursorDevice nsd = (NativeCursorDevice)cd;
					if (nsd.isSystemSupported(os)) {
						try {
							nsd.load();
							chosenDevice = nsd;
							driverStatus = DriverStatus.TABLET_FOUND;
							break;
						} catch (SecurityException e) {
							exception = e;
							driverStatus = DriverStatus.SECURITY_EXCEPTION;
						} catch (NativeDeviceException e) {
							exception = e;					
							driverStatus = DriverStatus.LIBRARY_LOAD_EXCEPTION;
						}
					}
				} else {
					chosenDevice = cd;
					break;
				}
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}
		this.cursorDevice = chosenDevice;
	}

	public enum DriverStatus {
		
		SECURITY_EXCEPTION,
		LIBRARY_LOAD_EXCEPTION,
		DEPENDENT_LIBRARY_LOAD_EXCEPTION,
		OS_NOT_SUPPORTED,
		NO_TABLET_FOUND,
		TABLET_FOUND,
		
	}
	public DriverStatus getDriverStatus() {
		return driverStatus;
	}
	public Exception getDriverException() {
		return exception;
	}
	
	public void addScreenTabletListener(TabletListener l) {
		cursorDevice.addScreenTabletListener(l);
	}
	public void removeScreenTabletListener(TabletListener l) {
		cursorDevice.addScreenTabletListener(l);
	}

	public void addTabletListener(Component c, TabletListener l) {
		cursorDevice.addTabletListener(c,l);
	}
	public void removeTabletListener(Component c, TabletListener l) {
		cursorDevice.removeTabletListener(c,l);
	}
	
}

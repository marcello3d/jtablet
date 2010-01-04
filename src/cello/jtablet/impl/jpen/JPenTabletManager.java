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

import cello.jtablet.TabletManager;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.MouseTabletManager;
import cello.jtablet.impl.jpen.platform.NativeCocoaInterface;
import cello.jtablet.impl.jpen.platform.NativeWinTabInterface;
import cello.jtablet.impl.jpen.platform.NativeXInputInterface;
import cello.jtablet.impl.platform.NativeException;
import cello.jtablet.impl.platform.NativeTabletManager;

/**
 * 
 * @author marcello
 */
public class JPenTabletManager extends TabletManager {

	private final Class<?> interfaces[] = {
		NativeCocoaInterface.class,
		NativeWinTabInterface.class,
		NativeXInputInterface.class,
		MouseTabletManager.class
	};
	private final TabletManager tabletManager;
	private Throwable exception;
	private DriverStatus driverStatus; 
	
	/**
	 * 
	 * @param hints
	 */
	public JPenTabletManager() {
		String os = System.getProperty("os.name").toLowerCase();
		
		TabletManager chosenManager = null;
		driverStatus = DriverStatus.OS_NOT_SUPPORTED;
		for (Class<?> cdClazz : interfaces) {
			try {
				TabletManager manager = (TabletManager)cdClazz.newInstance();
//				cd.setHints(hints);
				if (manager instanceof NativeTabletManager) {
					NativeTabletManager nsd = (NativeTabletManager)manager;
					if (nsd.isSystemSupported(os)) {
						try {
							nsd.load();
							chosenManager = manager;
							driverStatus = DriverStatus.TABLET_FOUND;
							break;
						} catch (SecurityException e) {
							exception = e;
							driverStatus = DriverStatus.SECURITY_EXCEPTION;
						} catch (UnsatisfiedLinkError e) {
							exception = e;				
							driverStatus = DriverStatus.LIBRARY_LOAD_EXCEPTION;
						} catch (NativeException e) {
							exception = e;					
							driverStatus = DriverStatus.LIBRARY_LOAD_EXCEPTION;
						}
					}
				} else {
					chosenManager = manager;
					break;
				}
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}
		System.out.println("Loaded TabletManager:"+chosenManager);
		this.tabletManager = chosenManager;
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
	public Throwable getDriverThrowable() {
		return exception;
	}
	
	public void addScreenTabletListener(TabletListener l) {
		tabletManager.addScreenTabletListener(l);
	}
	public void removeScreenTabletListener(TabletListener l) {
		tabletManager.addScreenTabletListener(l);
	}

	public void addTabletListener(Component c, TabletListener l) {
		tabletManager.addTabletListener(c,l);
	}
	public void removeTabletListener(Component c, TabletListener l) {
		tabletManager.removeTabletListener(c,l);
	}
	
}

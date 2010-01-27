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

package cello.jtablet.impl;

import java.awt.Component;

import cello.jtablet.DriverStatus;
import cello.jtablet.TabletManager;
import cello.jtablet.event.TabletListener;

/**
 * This class 
 * 
 * @author marcello
 */
public class TabletManagerImpl extends TabletManager {

	private final TabletManager tabletManager;
	private final DriverStatus tabletStatus; 
	
	/**
	 */
	public TabletManagerImpl() {
		String os = System.getProperty("os.name").toLowerCase();
		DriverStatus tabletStatus = new DriverStatus(DriverStatus.State.UNSUPPORTED_OS);;
		TabletManager chosenManager = null;
		
		NativeLoader loader = null;
		try {
			loader = new NativeLoader();
		} catch (Throwable t) {
			t.printStackTrace();
			tabletStatus = new DriverStatus(DriverStatus.State.UNEXPECTED_EXCEPTION, t);
		}
		
		
		Class<?> interfaces[] = {
			ScreenMouseTabletManager.class, // supports screen listeners but requires extra security permissions
			MouseTabletManager.class
		};
		if (loader != null) {
			try {
				interfaces = new Class<?>[] {
					loader.loadClass("CocoaTabletManager"),
					loader.loadClass("WinTabTabletManager"),
					loader.loadClass("XInputTabletManager"),
					ScreenMouseTabletManager.class, // supports screen listeners but requires extra security permissions
					MouseTabletManager.class
				};
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		for (Class<?> cdClazz : interfaces) {
			try {
				TabletManager manager = (TabletManager)cdClazz.newInstance();
				if (manager instanceof NativeTabletManager) {
					NativeTabletManager nsd = (NativeTabletManager)manager;
					if (nsd.isSystemSupported(os)) {
						try {
							nsd.load(loader);
							chosenManager = manager;
							tabletStatus = new DriverStatus(DriverStatus.State.LOADED);
							break;
						} catch (SecurityException e) {
							tabletStatus = new DriverStatus(DriverStatus.State.SECURITY_EXCEPTION, e);
						} catch (UnsatisfiedLinkError e) {
							tabletStatus = new DriverStatus(DriverStatus.State.NATIVE_EXCEPTION, e);
						} catch (NativeLoader.Exception e) {
							tabletStatus = new DriverStatus(DriverStatus.State.NATIVE_EXCEPTION, e);
						}
					}
				} else {
					chosenManager = manager;
					break;
				}
			} catch (Throwable t) {
				tabletStatus = new DriverStatus(DriverStatus.State.UNEXPECTED_EXCEPTION, t);
			}
		}
		this.tabletStatus = tabletStatus;
		this.tabletManager = chosenManager;
	}

	@Override
	public DriverStatus getDriverStatus() {
		return tabletStatus;
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

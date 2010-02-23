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
import java.security.AccessController;
import java.security.PrivilegedAction;

import cello.jtablet.DriverStatus;
import cello.jtablet.TabletManager;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.jpen.CocoaTabletManager;
import cello.jtablet.impl.jpen.WinTabTabletManager;
import cello.jtablet.impl.jpen.XInputTabletManager;

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
		
		final NativeLoader loader;
		NativeLoader tempLoader;
		try {
			tempLoader = new NativeLoader();
		} catch (Throwable t) {
			t.printStackTrace();
			tabletStatus = new DriverStatus(DriverStatus.State.UNEXPECTED_EXCEPTION, t);
			tempLoader = null;
		}
		loader = tempLoader;
		
		
		Class<?> interfaces[] = {
			ScreenMouseTabletManager.class, // supports screen listeners but requires extra security permissions
			MouseTabletManager.class
		};
		if (loader != null) {
			interfaces = new Class<?>[] {
				WinTabTabletManager.class,
				CocoaTabletManager.class,
				XInputTabletManager.class,
				ScreenMouseTabletManager.class, // supports screen listeners but requires extra security permissions
				MouseTabletManager.class
			};
		}
		for (Class<?> cdClazz : interfaces) {
			try {
				TabletManager manager = (TabletManager)cdClazz.newInstance();
				if (manager instanceof NativeTabletManager) {
					final NativeTabletManager nsd = (NativeTabletManager)manager;
					if (nsd.isSystemSupported(os)) {
						try {

							NativeLoaderException e = AccessController.doPrivileged(new PrivilegedAction<NativeLoaderException>() {
					            public NativeLoaderException run() {
					            	try {
										nsd.load(loader);
									} catch (NativeLoaderException e) {
										return e;
									}
					            	return null;
					            }
					        });
							if (e != null) {
								throw e;
							}
							chosenManager = manager;
							tabletStatus = new DriverStatus(DriverStatus.State.LOADED);
							break;
						} catch (SecurityException e) {
							tabletStatus = new DriverStatus(DriverStatus.State.SECURITY_EXCEPTION, e);
						} catch (UnsatisfiedLinkError e) {
							tabletStatus = new DriverStatus(DriverStatus.State.NATIVE_EXCEPTION, e);
						} catch (NativeLoaderException e) {
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

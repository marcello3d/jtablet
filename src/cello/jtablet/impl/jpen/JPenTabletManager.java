package cello.jtablet.impl.jpen;

import java.awt.Component;
import java.io.IOException;
import java.util.Map;

import cello.jtablet.TabletInterface;
import cello.jtablet.events.TabletListener;
import cello.jtablet.impl.CursorDevice;
import cello.jtablet.impl.jpen.platform.NativeCocoaInterface;
import cello.jtablet.impl.jpen.platform.NativeWinTabInterface;
import cello.jtablet.impl.jpen.platform.NativeXInputInterface;
import cello.jtablet.impl.platform.NativeCursorDevice;
import cello.jtablet.impl.platform.NativeDeviceException;

public class JPenTabletManager implements TabletInterface {

	private final Class<?> interfaces[] = {
		NativeCocoaInterface.class,
		NativeWinTabInterface.class,
		NativeXInputInterface.class,
		JPenTranslationInterface.class
	};
	private final CursorDevice cursorDevice;
	private Exception exception;
	private DriverStatus driverStatus; 
	
	public JPenTabletManager(Map<String, Object> hints) {
		String os = System.getProperty("os.name").toLowerCase();
		
		CursorDevice chosenDevice = null;
		driverStatus = DriverStatus.OS_NOT_SUPPORTED;
		for (Class<?> cdClazz : interfaces) {
			try {
				CursorDevice cd = (CursorDevice)cdClazz.newInstance();
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

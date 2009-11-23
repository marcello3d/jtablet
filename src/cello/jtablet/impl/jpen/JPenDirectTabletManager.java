package cello.jtablet.impl.jpen;

import java.awt.Component;

import cello.jtablet.events.TabletListener;
import cello.jtablet.impl.CursorDevice;
import cello.jtablet.impl.MouseListenerInterace;
import cello.jtablet.impl.TabletInterface;
import cello.jtablet.impl.jpen.platform.NativeCocoaInterface;
import cello.jtablet.impl.jpen.platform.NativeWinTabInterface;
import cello.jtablet.impl.jpen.platform.NativeXInputInterface;
import cello.jtablet.impl.platform.NativeCursorDevice;
import cello.jtablet.impl.platform.NativeDeviceException;

public class JPenDirectTabletManager implements TabletInterface {

	private final CursorDevice interfaces[] = {
		new NativeCocoaInterface(),
		new NativeWinTabInterface(),
		new NativeXInputInterface(),
		new MouseListenerInterace()
	};
	private final CursorDevice cursorDevice;
	private NativeDeviceException exception; 
	
	public JPenDirectTabletManager() {
		String os = System.getProperty("os.name").toLowerCase();
		
		CursorDevice chosenDevice = null; 
		for (CursorDevice cd : interfaces) {
			if (cd instanceof NativeCursorDevice) {
				NativeCursorDevice nsd = (NativeCursorDevice)cd;
				if (nsd.isSystemSupported(os)) {
					try {
						nsd.load();
						chosenDevice = nsd;
						break;
					} catch (NativeDeviceException e) {
						exception = e;					
					}
				}
			} else {
				chosenDevice = cd;
				break;
			}
		}
		this.cursorDevice = chosenDevice;
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

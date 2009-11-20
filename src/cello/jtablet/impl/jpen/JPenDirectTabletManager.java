package cello.jtablet.impl.jpen;

import java.awt.Component;

import cello.jtablet.events.TabletListener;
import cello.jtablet.impl.MouseListenerInterace;
import cello.jtablet.impl.NativeDeviceException;
import cello.jtablet.impl.NativeSystemDevice;
import cello.jtablet.impl.TabletInterface;
import cello.jtablet.impl.jpen.system.NativeCocoaInterface;
import cello.jtablet.impl.jpen.system.NativeWinTabInterface;
import cello.jtablet.impl.jpen.system.NativeXInputInterface;

public class JPenDirectTabletManager implements TabletInterface {

	private final NativeSystemDevice interfaces[] = {
		new NativeCocoaInterface(),
		new NativeWinTabInterface(),
		new NativeXInputInterface()
	};
	private final NativeSystemDevice nsi;
	private final MouseListenerInterace mouseInterface = new MouseListenerInterace();
	private NativeDeviceException exception; 
	
	public JPenDirectTabletManager() {
		String os = System.getProperty("os.name").toLowerCase();
		
		NativeSystemDevice chosenNsi = null; 
		for (NativeSystemDevice nsi : interfaces) {
			if (nsi.isSystemSupported(os)) {
				try {
					nsi.load();
					chosenNsi = nsi;
				} catch (NativeDeviceException e) {
					exception = e;					
				}
			}
		}
		this.nsi = chosenNsi;
	}

	public void addScreenTabletListener(TabletListener l) {
		nsi.addScreenTabletListener(l);
	}
	public void removeScreenTabletListener(TabletListener l) {
		nsi.addScreenTabletListener(l);
	}

	public void addTabletListener(Component c, TabletListener l) {
		nsi.addTabletListener(c,l);
		if (!nsi.overridesMouseListener()) {
			mouseInterface.addTabletListener(c,l);
		}
	}
	public void removeTabletListener(Component c, TabletListener l) {
		nsi.removeTabletListener(c,l);
		if (!nsi.overridesMouseListener()) {
			mouseInterface.removeTabletListener(c,l);
		}
	}
	
}

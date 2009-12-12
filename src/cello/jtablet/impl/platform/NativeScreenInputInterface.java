package cello.jtablet.impl.platform;

import cello.jtablet.impl.ScreenInputInterface;
import jpen.provider.NativeLibraryLoader;
import jpen.provider.NativeLibraryLoader.LoadException;

/**
 * @author marcello
 *
 */
public abstract class NativeScreenInputInterface extends ScreenInputInterface implements NativeCursorDevice {

	private boolean loaded = false;
	
	
	public void load() throws NativeDeviceException {
		try {
			getLoader().load();
			loaded = true;
		} catch (LoadException ex) {
			throw new NativeDeviceException(ex);
		}
	}
	protected abstract NativeLibraryLoader getLoader();
	
	@Override
	public boolean isDeviceAvailable() {
		return loaded;
	}
}

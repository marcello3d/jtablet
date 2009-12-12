package cello.jtablet.impl.jpen.platform;

import jpen.provider.NativeLibraryLoader;
import cello.jtablet.impl.platform.NativeScreenInputInterface;


public class NativeXInputInterface extends NativeScreenInputInterface {

	private static final NativeLibraryLoader LIB_LOADER=new NativeLibraryLoader(new String[]{""},
			new String[]{"x86_64", "ia64"},
			Integer.valueOf(jpen.Utils.getModuleProperties().getString("jpen.provider.xinput.nativeVersion")));

	public boolean isSystemSupported(String os) {
		return os.contains("linux");
	}

	public boolean isDeviceAvailable() {
		return false;
	}

	@Override
	protected void start() {
	}

	@Override
	protected void stop() {
	}

	@Override
	protected NativeLibraryLoader getLoader() {
		return LIB_LOADER;
	}
}

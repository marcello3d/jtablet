package cello.jtablet.impl.jpen.platform;

import cello.jtablet.impl.platform.NativeScreenInputInterface;


public class NativeXInputInterface extends NativeScreenInputInterface {

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

}

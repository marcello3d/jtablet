package cello.jtablet.impl.jpen.system;

import cello.jtablet.impl.NativeScreenInputInterface;


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

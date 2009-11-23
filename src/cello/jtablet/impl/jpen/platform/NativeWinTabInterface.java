package cello.jtablet.impl.jpen.platform;

import cello.jtablet.impl.platform.NativeScreenInputInterface;


public class NativeWinTabInterface extends NativeScreenInputInterface {

	public boolean isSystemSupported(String os) {
		return os.contains("windows");
	}
	@Override
	protected void start() {
		
	}

	@Override
	protected void stop() {
		
	}

}

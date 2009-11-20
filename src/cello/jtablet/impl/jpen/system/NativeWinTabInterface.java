package cello.jtablet.impl.jpen.system;

import cello.jtablet.impl.NativeScreenInputInterface;


public class NativeWinTabInterface extends NativeScreenInputInterface {

	public boolean isSystemSupported(String os) {
		return os.contains("windows");
	}
	@Override
	protected void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void stop() {
		// TODO Auto-generated method stub
		
	}

}

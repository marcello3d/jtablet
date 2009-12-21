package cello.jtablet.impl;

import java.util.Map;

import cello.jtablet.TabletInterface;
import cello.jtablet.events.TabletListener;

public interface CursorDevice extends TabletInterface {
	public boolean isDeviceAvailable();
	public void setHints(Map<String, Object> hints);
}

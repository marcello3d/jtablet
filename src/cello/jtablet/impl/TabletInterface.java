package cello.jtablet.impl;

import java.awt.Component;

import cello.jtablet.events.TabletListener;

public interface TabletInterface {
	public void addScreenTabletListener(TabletListener l);
	public void removeScreenTabletListener(TabletListener l);
	public void addTabletListener(Component c, TabletListener l);
	public void removeTabletListener(Component c, TabletListener l);
}

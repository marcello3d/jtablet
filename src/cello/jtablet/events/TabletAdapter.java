package cello.jtablet.events;

/**
 * @author marcello
 */
public abstract class TabletAdapter implements TabletListener {
	public void cursorEntered(TabletEvent ev) {}
	public void cursorExited(TabletEvent ev) {}
	public void cursorPressed(TabletEvent ev) {}
	public void cursorReleased(TabletEvent ev) {}
	public void cursorMoved(TabletEvent ev) {}
	public void cursorDragged(TabletEvent ev) {}
	public void levelChanged(TabletEvent ev) {}
	public void newDevice(TabletEvent ev) {}
	public void cursorGestured(TabletEvent ev) {}
	public void cursorScrolled(TabletEvent ev) {}
}

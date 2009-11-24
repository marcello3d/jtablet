package cello.jtablet.events;

/**
 * Funnels all tablet events into a single method.
 * 
 * @author marcello
 */

public abstract class TabletFunneler implements TabletListener {

	public void cursorDragged(TabletEvent ev) {
		handleEvent(ev);
	}
	public void cursorEntered(TabletEvent ev) {
		handleEvent(ev);
	}
	public void cursorExited(TabletEvent ev) {
		handleEvent(ev);
	}
	public void cursorGestured(TabletEvent ev) {
		handleEvent(ev);
	}
	public void cursorMoved(TabletEvent ev) {
		handleEvent(ev);
	}
	public void cursorPressed(TabletEvent ev) {
		handleEvent(ev);
	}
	public void cursorReleased(TabletEvent ev) {
		handleEvent(ev);
	}
	public void cursorScrolled(TabletEvent ev) {
		handleEvent(ev);
	}
	public void levelChanged(TabletEvent ev) {
		handleEvent(ev);
	}
	public void newDevice(TabletEvent ev) {
		handleEvent(ev);
	}
	protected abstract void handleEvent(TabletEvent ev);
}

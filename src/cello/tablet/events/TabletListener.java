package cello.tablet.events;


/**
 * Adds consumption features to PenListener
 * 
 * @author Marcello
 */
public interface TabletListener {
	/**
	 * Called when the input device changes
	 * @param ev
	 */
	public void newDevice(TabletEvent ev);
	/**
	 * Called when a button/stylus tip is pressed
	 * @param ev
	 */
	public void cursorPressed(TabletEvent ev);
	/**
	 * Called when a button/stylus tip is released
	 * @param ev
	 */
	public void cursorReleased(TabletEvent ev);
	/**
	 * Called when the cursor is moved
	 * @param ev
	 */
	public void cursorMoved(TabletEvent ev);
	/**
	 * Called when the cursor is dragged (pressed+moved)
	 * @param ev
	 */
	public void cursorDragged(TabletEvent ev);
	/**
	 * Called when a pressure level has changed
	 * @param ev
	 */
	public void levelChanged(TabletEvent ev);
	
}

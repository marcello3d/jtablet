package cello.jtablet.events;


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
	 * Called when the stylus tip is pressed
	 * @param ev
	 */
	public void cursorPressed(TabletEvent ev);
	/**
	 * Called when the stylus tip is released
	 * @param ev
	 */
	public void cursorReleased(TabletEvent ev);
	/**
	 * Called either when the cursor enters the given component, either spatially or through proximity. 
	 * @param ev
	 */
	public void cursorEntered(TabletEvent ev);
	/**
	 * Called either when the cursor exits the given component, either spatially or through proximity. 
	 * @param ev
	 */
	public void cursorExited(TabletEvent ev);
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
	 * Called when the cursor is scrolled
	 * @param ev
	 */
	public void cursorScrolled(TabletEvent ev);

	/**
	 * Called when the cursor is gestured
	 * @param ev
	 */
	public void cursorGestured(TabletEvent ev);
	/**
	 * Called when a pressure level has changed
	 * @param ev
	 */
	public void levelChanged(TabletEvent ev);
	
}

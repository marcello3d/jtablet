/**
 * 
 */
package cello.tablet.event;

import java.awt.event.MouseEvent;

import cello.tablet.JTabletCursor;

/**
 * @author Thor Harald Johansen
 *
 */
public class JTabletEvent extends MouseEvent {
	protected JTabletCursor cursor;
	
	/** Constructs a JTabletEvent from a MouseEvent and a JTabletCursor 
	 * @param event 
	 * @param cursor */
	public JTabletEvent(MouseEvent event, JTabletCursor cursor) {
		super(event.getComponent(), event.getID(), event.getWhen(),event.getModifiers(), event.getX(), event.getY(), event.getClickCount(), event.isPopupTrigger()); 
		this.cursor = cursor;
	}
	
	/** Gets tablet cursor for this event.
	 * @return JTabletCursor */
	public JTabletCursor getCursor() {
		return cursor;
	}
}

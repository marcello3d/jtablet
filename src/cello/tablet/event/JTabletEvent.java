/**
 * 
 */
package cello.tablet.event;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;

import cello.tablet.JTabletCursor;

/**
 * @author Thor Harald Johansen
 * 
 */
public class JTabletEvent extends AWTEvent {
    public final static int JTABLET = AWTEvent.RESERVED_ID_MAX + 1983;
    
    protected JTabletCursor cursor;

    /**
     * Constructs a JTabletEvent from a MouseEvent and a JTabletCursor
     * 
     * @param source
     * @param cursor
     */
    public JTabletEvent(Object source, JTabletCursor cursor) {
        super(source, JTABLET);
    }

    /**
     * Gets tablet cursor for this event.
     * 
     * @return JTabletCursor
     */
    public JTabletCursor getCursor() {
        return cursor;
    }
}

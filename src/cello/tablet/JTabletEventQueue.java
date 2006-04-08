/**
 * 
 */
package cello.tablet;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;

import cello.tablet.event.JTabletEvent;

/**
 * JTablet Event Queue for push()ing into System Event Queue.
 * 
 * @author Thor Harald Johansen
 * 
 */
public class JTabletEventQueue extends EventQueue {
    protected JTablet jtablet;

    protected JTabletEvent jtevent;

    protected JTabletEventQueue() {

    }

    public JTabletEventQueue(JTablet jtablet) {
        this.jtablet = jtablet;
    }

    protected void dispatchEvent(AWTEvent event) {
        try {
            if (event instanceof MouseEvent && jtablet.poll()
                    && jtablet.hasCursor()) {

            }
        } catch (JTabletException e) {

        }
        super.dispatchEvent(event);
    }
}

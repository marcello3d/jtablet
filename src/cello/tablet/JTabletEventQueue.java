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
    protected static JTablet jtablet = null;
    protected static boolean jtabletHealthy = true;
    
    protected static JTabletCursor mouseCursor;
    
    public JTabletEventQueue() {
        this(false);
    }

    public JTabletEventQueue(boolean fullControl) {
        if(jtabletHealthy && jtablet == null) {
            try {
                jtablet = new JTablet(fullControl);
            } catch(JTabletException e) {
                jtabletHealthy = false;
            }
        
            mouseCursor.cursorName = null;
            mouseCursor.physicalId = 0;
            mouseCursor.cursorTypeGeneral = JTabletCursor.TYPE_PUCK;
            mouseCursor.cursorTypeSpecific = JTabletCursor.TYPE_UNKNOWN;
            mouseCursor.dataTime = 0;
        }
    }

    protected void dispatchEvent(AWTEvent event) {
        try {
            if (event instanceof MouseEvent) {
                jtablet.poll();

            }
        } catch (JTabletException e) {

        }
        super.dispatchEvent(event);
    }
}

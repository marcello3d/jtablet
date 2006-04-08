/**
 * 
 */
package cello.tablet;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;

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
        if (jtabletHealthy && jtablet == null) {
            try {
                jtablet = new JTablet(fullControl);
            } catch (JTabletException e) {
                jtabletHealthy = false;
            }
        }
    }

protected void dispatchEvent(AWTEvent ae) {
        try {
            if (ae instanceof MouseEvent) {
                /* Cast to MouseEvent and store. */
                MouseEvent me = (MouseEvent)ae;
                
                /* Set default pressure. */
                int pressure = 1;
                
                /* Set default pressure extent. */
                int pressureExtent = 1;
                
                if(jtabletHealthy && jtablet.poll() && jtablet.hasCursor()) {
                    /* Get tablet cursor if any. */
                    JTabletCursor cursor = jtablet.getCursor();
                    
                    /* Get and set real pressure. */
                    pressure = cursor.getPressure();
                    
                    /* Get and set real pressure extent. */
                    pressureExtent = cursor.getPressureExtent();
                    
                }

                /* Hijack 'when' field for tablet data. */
                long when = (pressure << 32) | pressureExtent;
                
                /* Replace old mouse event with new one. */
                ae = new MouseEvent(me.getComponent(), me.getID(), when,
                        me.getModifiers(), me.getX(), me.getY(), me.getClickCount(),
                        me.isPopupTrigger(), me.getButton());
            }
        } catch (JTabletException e) {
            /* Do nothing special here. */
        }
        
        super.dispatchEvent(ae);
    }}

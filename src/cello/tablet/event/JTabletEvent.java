/**
 * 
 */
package cello.tablet.event;

import java.awt.event.MouseEvent;

/** JTablet event
 * @author Thor Harald Johansen
 * 
 */
public class JTabletEvent {
    protected int pressure;
    protected int pressureExtent;
    /**
     * Constructs a JTabletEvent from a mouse event.
     * 
     * @param e Mouse event to decode.
     */
    public JTabletEvent(MouseEvent e) {
        /* Get 'when' data. */
        long when = e.getWhen();
        
        /* Decode pressure. */
        pressure = (int)(when >> 32);
        
        /* Decode pressure extent. */
        pressureExtent = (int)(when & 0xFFFFFFFF);
    }
    
    public int getPressure() {
        return pressure;
    }
    
    public int getPressureExtent() {
        return pressureExtent;
    }
}

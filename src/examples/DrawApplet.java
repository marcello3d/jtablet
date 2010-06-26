import java.applet.Applet;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import cello.jtablet.*;
import cello.jtablet.event.*;
import cello.jtablet.installer.JTabletExtension;

public class DrawApplet extends Applet {
    
    BufferedImage image;
    Graphics2D graphics;
    
    public void init() {
        // Make sure the user has a compatible version of JTablet (or none)
        if (!JTabletExtension.checkCompatibility(this, "1.2.0")) {
            return;
        }
        
        // Create a drawing canvas
        image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        
        // Paint it white
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, getWidth(), getHeight());
        
        // Enable anti-aliasing and sub-pixel rendering 
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                  RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                                  RenderingHints.VALUE_STROKE_PURE);
        
        // Create the tablet listener
        TabletListener listener = new TabletAdapter() {
            // Draw circles when the cursor is dragged
            public void cursorDragged(TabletEvent event) {
                float radius = event.getPressure() * 10;
                
                graphics.fill(new Ellipse2D.Float(
                    event.getFloatX() - radius,
                    event.getFloatY() - radius,
                    radius * 2,
                    radius * 2
                ));
                
                repaint();
            }
            
            // Detect when a new cursor enters the canvas
            public void cursorEntered(TabletEvent event) {
                // Set the color to white if the device is an eraser
                if (event.getDevice().getType() == TabletDevice.Type.ERASER) {
                    graphics.setColor(Color.WHITE);
                } else {
                    graphics.setColor(Color.BLACK);
                }
            }
        };     
    
        // Add the listener to the component (this Applet) when the Applet starts
        TabletManager.getDefaultManager().addTabletListener(this, listener);
    }
    
    // Paint the canvas to the screen
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }
}

import java.awt.Frame;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import cello.jtablet.TabletManagerFactory;
import cello.jtablet.event.TabletAdapter;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;

/**
 * @author marcello
 */

public class Example1 {

	// Defines a window on the screen
	Frame frame = new Frame("JTablet 2.0 Example 1");
	// An AWT text label 
	Label label = new Label();
	
	// Create a new listener
	TabletListener tabletListener = new TabletAdapter() {
		public void cursorMoved(TabletEvent ev) {
			label.setText("Cursor moved: "+ev.getRealX()+", "+ev.getRealY());
		}

		public void cursorDragged(TabletEvent ev) {
			label.setText("Cursor dragged: "+ev.getRealX()+", "+ev.getRealY()+" pressure="+ev.getPressure());
		}
	};
	
	Example1() {
		// Setup the frame
		frame.setSize(400,400);
		frame.add(label);
		
		// Add the tablet listener
		TabletManagerFactory.getManager().addTabletListener(label, tabletListener);
		
		// Show the frame on the screen
		frame.setVisible(true);

		// Exit when the close button is clicked
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public static void main(String[] args) {
		new Example1();
	}
}

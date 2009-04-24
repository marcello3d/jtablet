/**
 * 
 */
package cello.tablet.events;

import java.io.Serializable;

/**
 * @author marcello
 */
public abstract class TabletDevice implements Serializable {


	private TabletDevice() {}
	
	public static final TabletDevice STYLUS = new Stylus();
	public static final TabletDevice STYLUS_ERASER = new StylusEraser();
	public static final TabletDevice MOUSE = new Mouse();

	public static class Stylus extends TabletDevice { }
	public static class StylusEraser extends TabletDevice { }
	public static class Mouse extends TabletDevice { }
}

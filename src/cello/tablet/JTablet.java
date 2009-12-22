/*!
 * Copyright (c) 2009 Marcello Bastéa-Forte (marcello@cellosoft.com)
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 * 
 *     2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 * 
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */

package cello.tablet;

import java.util.LinkedList;

import cello.jtablet.TabletManagerFactory;
import cello.jtablet.events.TabletAdapter;
import cello.jtablet.events.TabletEvent;
import cello.jtablet.events.TabletListener;

/**
 * This class is provided as a compatibility wrapper for old 0.9.x-based JTablet applications/applets.
 * 
 * @see cello.jtablet.TabletManagerFactory for the new implementation
 * 
 * @version 0.9.x 12/18/2009
 * @author Marcello Bastea-Forte
 */

@Deprecated
public class JTablet {

    private JTabletCursor currentCursor = null;

    private boolean pollModeLatest = true;
    
    private final LinkedList<JTabletCursor> cursorQueue = new LinkedList<JTabletCursor>();
    

    /**
     * @throws JTabletException 
     */
    @Deprecated
    public JTablet() throws JTabletException {
        this(false);
    }
	/**
     * Creates a new JTablet object.
     * 
     * @param fullControl Tries to access the tablet with full control for
     *            digitizing purposes.
     * @exception JTabletException if the native library could not be loaded or
     *                no tablet is available
     * @since 0.9.1
     */
    @Deprecated
    public JTablet(boolean fullControl) throws JTabletException {
		System.out.println("JTablet loaded");
    	TabletManagerFactory.getManager().addScreenTabletListener(tabletListener);
    }


    /**
     * 
     */
    private final TabletListener tabletListener = new TabletAdapter() {
		public void cursorDragged(TabletEvent ev) {
			handle(ev);
		}
		public void cursorMoved(TabletEvent ev) {
			handle(ev);
		}
		public void cursorPressed(TabletEvent ev) {
			handle(ev);
		}
		public void cursorReleased(TabletEvent ev) {
			handle(ev);
		}
		public void levelChanged(TabletEvent ev) {
			handle(ev);
		}
		public void newDevice(TabletEvent ev) {
			handle(ev);
		}
		public void cursorEntered(TabletEvent ev) {
			handle(ev);
		}
		public void cursorExited(TabletEvent ev) {
			handle(ev);
		}
    }; 

    protected void handle(TabletEvent ev) {
    	if (pollModeLatest) {
    		cursorQueue.clear();
    	}
    	cursorQueue.add(new JTabletCursor(ev));
	}

    /**
     * Checks if JTablet has a cursor. There will be no cursor until poll is
     * called.
     * 
     * @return if the current cursor is null
     * @since 0.9.1
     */
    @Deprecated
    public boolean hasCursor() {
        return currentCursor != null;
    }

    /**
     * Returns the last JTabletCursor that was polled from
     * 
     * @return the JTabletCursor object
     * @since 0.9.1
     */
    @Deprecated
    public JTabletCursor getCursor() {
        return currentCursor;
    }

    /**
     * Returns if Poll mode is retrieving the latest information.
     * 
     * @see #setPollModeLatest(boolean)
     * @return the JTabletCursor object
     * @since 0.9.1
     */
    @Deprecated
    public boolean isPollModeLatest() {
        return pollModeLatest;
    }

    /**
     * @param b 
     */
    @Deprecated
    public void setPollModeLatest(boolean b) {
        pollModeLatest = b;
    	if (pollModeLatest && !cursorQueue.isEmpty()) {
    		JTabletCursor last = cursorQueue.getLast();
    		cursorQueue.clear();
    		cursorQueue.add(last);
    	}
    }

    /**
     * 
     */
    public void finalize() throws JTabletException {
        close();
    }

    /**
     */
    @Deprecated
    public void close() {
    	TabletManagerFactory.getManager().removeScreenTabletListener(tabletListener);
    }

    /**
     * Retrieves the current JTablet version.
     * 
     * @return a String containing version information.
     * @since 0.2
     */
    @Deprecated
    public String getVersion() {
        return getLibraryVersion();
    }

    /**
     * Retrieves the current JTablet version of the loaded library. If this
     * doesn't match the return of JTablet.getVersion(), results may be
     * inaccurate.
     * 
     * @return a String containing version information.
     * @since 0.9.2
     */
    @Deprecated
    public static String getLibraryVersion() {
    	return "0.9.9-jtablet2compatibility";
    }

    /**
     * Polls the tablet for the latest tablet information. This should be done
     * as frequently as possible for the best results. If poll mode is set to
     * latest, then this will return the last known information from the tablet,
     * skipping anything since the last poll.
     * 
     * @see #isPollModeLatest
     * @see #setPollModeLatest(boolean)
     * @return true if any new information was retrieved
     * @exception JTabletException if there was an error reading the tablet
     */
    @Deprecated
    public boolean poll() throws JTabletException {
        if (cursorQueue.isEmpty()) {
            return false;
        }
        currentCursor = cursorQueue.removeFirst();
        return true;
    }


	/**
     * Retrieves the current pressure of the tablet as an int. Use
     * <code>getPressureExtent()</code> to find the maximum value.
     * 
     * @return Current pressure.
     * @see cello.tablet.JTabletCursor#getPressure
     * @see #getPressureExtent
     */
    @Deprecated
    public int getPressure() {
        if(!hasCursor())
            return 0;
        return getCursor().getPressure();
    }

    /**
     * Retrieves the maximum possible pressure of <code>getPressure()</code>.
     * 
     * @return the pressure extent value
     * @see cello.tablet.JTabletCursor#getPressureExtent
     * @see #getPressure
     */
    @Deprecated
    public int getPressureExtent() {
        if(!hasCursor())
            return 0;
        return getCursor().getPressureExtent();
    }

    /**
     * Retrieves the angle of the tablet. As of version 0.9.1, this feature
     * always returns 0.
     * 
     * @deprecated
     * @return returns 0
     */
    @Deprecated
    public int getAngle() {
        return 0;
    }

    /**
     * As of version 0.9.1, this feature always returns 0.
     * 
     * @deprecated
     * @return returns 0
     */
    @Deprecated
    public int getX() {
        return 0;
    }

    /**
     * As of version 0.9.1, this feature always returns 0.
     * 
     * @deprecated
     * @return returns 0
     */
    @Deprecated
    public int getY() {
        return 0;
    }

    /**
     * As of version 0.9.1, this feature always returns 0.
     * 
     * @deprecated
     * @return returns 0
     */
    @Deprecated
    public int getZ() {
        return 0;
    }

    /**
     * Retrieves the orientation of the tablet. If this value is negative, the
     * user has turned their stylus upside-down and is using it as an eraser. As
     * of v0.9.1, JTabletCursor.getData() should be used instead.
     * 
     * @see cello.tablet.JTabletCursor#getData(int)
     * @deprecated
     * @return the orientation value
     */
    @Deprecated
    public int getOrientation() {
        if(!hasCursor())
            return 0;
        return getCursor().getData(JTabletCursor.DATA_ORIENTATION_ALTITUDE);
    }

    /**
     * Retrieves the buttons from the tablet. As of v0.9.1,
     * JTabletCursor.getData() should be used instead.
     * 
     * @see cello.tablet.JTabletCursor#getData(int)
     * @deprecated
     * @return the buttons value
     */
    @Deprecated
    public int getButtons() {
        if(!hasCursor())
            return 0;
        return getCursor().getData(JTabletCursor.DATA_BUTTONS);
    }

}
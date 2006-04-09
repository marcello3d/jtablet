/***************************************************************
 *
 * JTablet is an open-source native Tablet library for Java by
 *	Marcello Bastéa-Forte (marcello@cellosoft.com
 *
 *   You are free to modify this code as you wish, but any
 *   useful/significant changes should be contributed back to
 *   original project.  *This entire message should remain intact*
 *
 *  If you are interested in using JTablet in commercial
 *  projects, please contact me at marcello@cellosoft.com
 *
 ***************************************************************/

package cello.tablet;

/**
 * The entrance JTablet class. Tablet information is stateless, but this class
 * allows it to function as a class entity.
 * <p>
 * To construct a JTablet object, you must catch <code>JTabletException</code>.
 * 
 * <pre>
 *     try {
 *   	JTablet jtablet = new JTablet();
 *     } catch (JTabletException jte) {
 *   	System.err.println(&quot;Could not load JTablet! (&quot; + jte.toString() + &quot;).&quot;);
 *     }
 *    `
 * </pre>
 * 
 * @version 0.9.3 3/6/2004
 * @author Marcello Bastea-Forte
 * @see cello.tablet.JTabletCursor
 * @see cello.tablet.JTabletException
 */

public class JTablet {

    static boolean library_loaded = false;

    static int access_count = 0;
    static {
        // Load system library
        try {
            try {
                Class.forName("java.security.AccessController");
                JTabletLoadNative.loadNative();
            } catch(Exception e) {
                System.loadLibrary("jtablet");
            }
            library_loaded = true;
        } catch(Exception e) {
            System.err.println("JTablet could not be loaded (" + e.toString()
                    + ").");
        }
    }

    protected JTabletCursor currentCursor = null;

    protected boolean pollModeLatest = true;

    /**
     * Creates a new JTablet object without full control.
     * 
     * @exception JTabletException if the native library could not be loaded or
     *                no tablet is available.
     * @see #JTablet(boolean)
     */
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
    public JTablet(boolean fullControl) throws JTabletException {
        if(!library_loaded)
            throw new JTabletException("Could not load JTablet native library.");
        if(!tabletAvailable())
            throw new JTabletException("Tablet not available.");
        initializeTablet(fullControl);
        access_count++;
    }

    /**
     * Checks if JTablet has a cursor. There will be no cursor until poll is
     * called.
     * 
     * @return if the current cursor is null
     * @since 0.9.1
     */
    public boolean hasCursor() {
        return currentCursor != null;
    }

    /**
     * Returns the last JTabletCursor that was polled from
     * 
     * @return the JTabletCursor object
     * @since 0.9.1
     */
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
    public boolean isPollModeLatest() {
        return pollModeLatest;
    }

    /**
     * If this option is enabled (by default), whenever JTablet.poll() is called
     * the latest information will be retrieved, rather than the next available
     * information. It may be useful to disable this option if you want to
     * retrieve an extended amount of information.
     * 
     * @param b Whether or not to enable poll mode.
     * @see #isPollModeLatest
     * @see #poll
     * @since 0.9.1
     */
    public void setPollModeLatest(boolean b) {
        pollModeLatest = b;
    }

    /**
     * Unloads the native driver for JTablet
     * 
     * @exception JTabletException if there was an error unloading the tablet
     */
    public void finalize() throws JTabletException {
        close();
    }

    /**
     * Unloads the native driver for JTablet
     */
    public void close() {
        if(access_count <= 1)
            closeTablet();
        access_count--;
    }

    /**
     * Retrieves the current JTablet version.
     * 
     * @return a String containing version information.
     * @since 0.2
     */
    public String getVersion() {
        return "0.9.5 BETA";
    }

    /**
     * Retrieves the current JTablet version of the loaded library. If this
     * doesn't match the return of JTablet.getVersion(), results may be
     * inaccurate.
     * 
     * @return a String containing version information.
     * @since 0.9.2
     */
    public static native String getLibraryVersion();

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
    public boolean poll() throws JTabletException {
        JTabletCursor newCursor = pollCursor(pollModeLatest);
        if(newCursor == null)
            return false;
        if(currentCursor != newCursor) {
            currentCursor = newCursor;
            // TODO: trigger some listener perhaps?
        }
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
    public int getAngle() {
        return 0;
    }

    /**
     * As of version 0.9.1, this feature always returns 0.
     * 
     * @deprecated
     * @return returns 0
     */
    public int getX() {
        return 0;
    }

    /**
     * As of version 0.9.1, this feature always returns 0.
     * 
     * @deprecated
     * @return returns 0
     */
    public int getY() {
        return 0;
    }

    /**
     * As of version 0.9.1, this feature always returns 0.
     * 
     * @deprecated
     * @return returns 0
     */
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
    public int getButtons() {
        if(!hasCursor())
            return 0;
        return getCursor().getData(JTabletCursor.DATA_BUTTONS);
    }

    // Private methods
    private static native boolean tabletAvailable();

    private static synchronized native void initializeTablet(boolean fullControl)
            throws JTabletException;

    private static synchronized native void closeTablet();

    private static synchronized native JTabletCursor pollCursor(
            boolean pollToLatest) throws JTabletException;

    // Package method
    static native synchronized void getCursorData(JTabletCursor cursor, int type)
            throws JTabletException;

}
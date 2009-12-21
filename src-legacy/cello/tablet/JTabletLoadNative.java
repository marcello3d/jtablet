/***************************************************************
 *
 * JTablet is an open-source native Tablet library for Java by
 *	Marcello Bast�a-Forte (marcello@cellosoft.com
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

import java.security.*;

/**
 * Wrapper class for Java 2 security handling. This class allows the JTablet
 * library to be loaded on Java 2 applets if installed properly.
 * 
 * @version 0.2 2/18/2003
 * @author Marcello Bastea-Forte
 */

@Deprecated
public class JTabletLoadNative {
    /**
     * 
     */
    public static final void loadNative() {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                System.loadLibrary("jtablet");
                return null;
            }
        });
    }

}
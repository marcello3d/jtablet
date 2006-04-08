/***************************************************************
 *
 * JTablet is an open-source native Tablet library for Java by
 *    Marcello Bastéa-Forte (marcello@cellosoft.com
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
 * Class that handles JTablet exceptions.  This class is used internally
 * by JTablet to handle hardware and software errors.
 *
 * @version 0.2 2/18/2003
 * @author Marcello Bastea-Forte
 */
 
public class JTabletException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 2162749738051232777L;

    /**
     * Constructs a new JTabletException with a given error message.
     */
    public JTabletException(String message) {
        super(message);
    }
}
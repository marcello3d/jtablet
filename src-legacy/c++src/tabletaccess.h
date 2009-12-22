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

/***************************************************************
 *
 * File information:
 *
 *   tabletaccess.h - The standard function headers used by
 * cello_tablet_JTablet.cpp to access the tablet.  
 *
 *   Ports for each platform will implement these.
 *
 *  Last update: March 8, 2004
 *  For version: 0.9.4
 *
 ***************************************************************/

#ifndef _TABLETACCESS_H
#define _TABLETACCESS_H

#include "JTablet.h"

// For all of these functions, you should look at wintabaccess.cpp's implementations.  It
// gives a nearly complete implementation of all features in JTablet.

// A fast function to check if the tablet is available (does not need to be accurate)
bool TabletAvailable();

// Initializes the tablet with an optional hint to take over control of the tablet 
//  On WinTab this allows you higher resolution access and avoids the tablet moving the mouse
//  which could cause problems for certain types of programs.
//
// This function should reinitialize the tablet if called after the tablet has already been
// initialized.
bool InitializeTablet(bool fullControl);

// Closes the tablet.  This function should be safe to call even if the tablet has not been
// initialized with InitializeTablet();
void CloseTablet();

// Gets the latest information from the tablet as a JTabletCursor object.  The 
// JTablet_getCursorObject and JTablet_makeCursor functions from JTablet.h should be used
// to actually create and get these cursors based on cursor and physical id of the tablet.
//
// pollToLatest should get the most current information from the tablet.  You are not
// obligated to do any differently if pollToLatest is false.
jobject ReadTablet(JNIEnv *env, bool pollToLatest);

// Should attempt to retrieve support and range information of the tablet and store it in
// the JTabletCursor object passed to the function using JTablet_getCursorDataInfo and 
// JTablet_getCursorDataInfo from JTablet.h
//
// You are not required to actually retrieve this information if you cannot.
void GetCursorData(JNIEnv *env, jobject cursorObject, jint dataType);

#endif

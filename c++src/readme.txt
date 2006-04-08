Source Porting information:

The JTablet plugin is designed to be portable to any system that supports C++ and Java Native Interface (JNI).

The following files are all cross platform and can be used as-is:
	cello_tablet_JTablet.cpp
	cello_tablet_JTablet.h
	cello_tablet_JTabletCursor.h
	JTablet.cpp
	JTablet.h
	tabletaccess.h

To port the code, you simply need to implement the functions defined in tabletaccess.h and generate a shared library file that can be used with JNI.

In the example of windows, the standard port created for the original JTablet distribution, wintabaccess.cpp and wintabaccess.h use the WinTab drivers to implement all the necessary functions in tabletaccess.h.  The shared library is a DLL file compiled and copied to the windows system directory.

You will notice that all the windows-specific includes and code is in those two files, and neither one is referenced in the 6 files listed above.

Once you have a binary shared library, you will need to give instructions on how to install (or create an installer) that steps one through the process of copying the .jar file to the appropriate jre/lib/ext/ folder.

In the example of the windows installer, Nullsoft Install System is used to auto-detect installations of Java and copy the jtablet.jar and jtablet.dll files to the appropriate directories automatically.

--

The JTablet.cpp/.h files include a number of useful and essential functions for accessing standard functions in the JTablet java classes.  JTablet.h lists all the functions and has short descriptions of each.

In addition wintabaccess.cpp should be a guide to how each function is used.

--

October 1, 2003

Marcello Bastéa-Forte

marcello@cellosoft.com

http://cellosoft.com/sketchstudio/


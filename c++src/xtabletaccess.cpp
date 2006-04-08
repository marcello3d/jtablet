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
 *   xtabletaccess.cpp - The linux X-Windows version of JTablet.
 *
 *  Last update: March 8, 2004
 *  For version: 0.9.4
 *
 * Contributed by Ben Davis (http://bdavis.strangesoft.net/)
 *
 ***************************************************************/

#include <X11/extensions/XInput.h>
//#include <X11/Intrinsic.h>
#include "tabletaccess.h"
#include "cello_tablet_JTabletCursor.h"
#include <stdio.h> // temp



static Display *display = 0;
static int numTablets = 0;
static XDevice **device = 0;
static Time lastTime;



bool TabletAvailable()
{
	return true;
	/* We'll do it properly in InitializeTablet(). */
}



bool InitializeTablet(bool /*fullControl*/)
{
	CloseTablet();

	display = XOpenDisplay(0);
	if (!display) return false;

	Atom tablet = 121;//XInternAtom(display, XI_TABLET, True);

	int i, j;

	int numDevices;
	XDeviceInfo *deviceInfo = XListInputDevices(display, &numDevices);

	fprintf(stderr, "tablet = %lu\n", tablet);

	numTablets = 0;
	for (i = 0; i < numDevices; i++) {
		if (deviceInfo[i].type == tablet) numTablets++;
		fprintf(stderr, "deviceInfo[%d].name = %s, type = %d\n", i, deviceInfo[i].name, deviceInfo[i].type);
	}

	fprintf(stderr, "numDevices=%d numTablets=%d\n", numDevices, numTablets);

	if (!numTablets) {
		XFreeDeviceList(deviceInfo);
		XCloseDisplay(display);
		display = 0;
		return false;
	}

	Window window = XRootWindow(display, 0);

	device = new XDevice *[numTablets];
	j = 0;
	for (i = 0; i < numDevices; i++) {
		if (deviceInfo[i].type == tablet) {
			device[j] = XOpenDevice(display, deviceInfo[i].id);
			if (device[j]) {
				int eventType; // should this be int?
				XEventClass eventClass;
				DeviceMotionNotify(device[j], eventType, eventClass);
				XSelectExtensionEvent(display, window, &eventClass, 1);
				j++;
			} else
				numTablets--;
		}
	}

	XFreeDeviceList(deviceInfo);

	if (!numTablets) {
		delete device;
		XCloseDisplay(display);
		display = 0;
		return false;
	}

	lastTime = CurrentTime;

	return true;
}



void CloseTablet()
{
	if (device) {
		for (int i = 0; i < numTablets; i++) delete device[i];
		delete device;
	}

	numTablets = 0;

	if (display) {
		XCloseDisplay(display);
		display = 0;
	}
}



static Bool checkIfEventPredicate(Display *, XEvent *, XPointer)
{
	return True;
}



// return null if no packet
jobject ReadTablet(JNIEnv *env, bool pollToLatest)
{
	if (!display) return 0;

	union {
		XEvent event;
		XDeviceMotionEvent dme;
	} event;

	//int eventType; // should this be int?
	//XEventClass eventClass;
	//DeviceMotionNotify(device[0], eventType, eventClass);

	//if (!XCheckTypedEvent(display, eventType, &event.event)) return 0;
	//if (pollToLatest) do { } while (XCheckTypedEvent(display, eventType, &event.event));
	if (!XCheckIfEvent(display, &event.event, &checkIfEventPredicate, 0)) return 0;
	if (pollToLatest) do { } while (XCheckIfEvent(display, &event.event, &checkIfEventPredicate, 0));

	int currentCursorId = 0;
	int currentCursorPhysicalId = 0;
	bool initializingCursor = false;

	fprintf(stderr, "%d\n", event.dme.deviceid);
	jobject currentCursorObject = JTablet_getCursorObject(currentCursorId, currentCursorPhysicalId);
	if (currentCursorObject == NULL) {
		currentCursorObject = JTablet_makeCursor(env, currentCursorId, currentCursorPhysicalId);
		if (currentCursorObject == NULL)
			return NULL;

		initializingCursor = true;
	}

	jintArray javaArray;
	jint *array;
	JTablet_getCursorDataValueRaw(env, currentCursorObject, javaArray, &array);

	//if (initializingCursor) {
	//	InitializeCursor(env, currentCursorObject, currentCursorId, cursorPhysicalId);
	//	array[cello_tablet_JTabletCursor_DATA_CURSOR] 			= p.pkCursor;
	//}

	array[cello_tablet_JTabletCursor_DATA_BUTTONS]				= event.dme.state;
	array[cello_tablet_JTabletCursor_DATA_X]					= event.dme.axis_data[0];
	array[cello_tablet_JTabletCursor_DATA_Y]					= event.dme.axis_data[1];
	array[cello_tablet_JTabletCursor_DATA_Z]					= 0;

	array[cello_tablet_JTabletCursor_DATA_PRESSURE]				= event.dme.axis_data[2];
	array[cello_tablet_JTabletCursor_DATA_TANGENT_PRESSURE]		= 0;

	array[cello_tablet_JTabletCursor_DATA_ORIENTATION_AZIMUTH]	= 0;
	array[cello_tablet_JTabletCursor_DATA_ORIENTATION_ALTITUDE]	= 0;
	array[cello_tablet_JTabletCursor_DATA_ORIENTATION_TWIST]	= 0;

	array[cello_tablet_JTabletCursor_DATA_ROTATION_PITCH]		= 0;
	array[cello_tablet_JTabletCursor_DATA_ROTATION_ROLL]		= 0;
	array[cello_tablet_JTabletCursor_DATA_ROTATION_YAW]			= 0;

	JTablet_releaseCursorDataValueRaw(env, javaArray, &array);

	return currentCursorObject;

/*
       Bool XCheckTypedEvent(display, event_type, event_return)
             Display *display;
             int event_type;
             XEvent *event_return;
*/
#if 0
	int numEvents, mode, axisCount;
	XDeviceTimeCoord **timeCoord = new XDeviceTimeCoord *[numTablets];
	int i;
	for (i = 0; i < numTablets; i++)
		timeCoord[i] = XGetDeviceMotionEvents(display, device[i], lastTime, CurrentTime, &numEvents, &mode, &axisCount);
	// TODO: does XGetDeviceMotionEvents always return something?

	XDeviceTimeCoord *
	if (pollToLatest) {

	for (i = 0; i < numTablets; i++)
		XFreeDeviceMotionEvents(timeCoord[i]);
	delete []timeCoord;
#endif
}



void GetCursorData(JNIEnv *env, jobject cursorObject, jint dataType)
{
	if (!display) return;

}

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
 *   wintabaccess.cpp - the Windows implementation of
 *   tabletaccess.h.
 *
 *  Last update: May 22, 2005
 *  For version: 0.9.5
 *
 ***************************************************************/

#include <string.h>
#include <windows.h>
#include <stdlib.h>

/* wintab */
#include <wintab.h>
#ifdef USE_X_LIB
#include <wintabx.h>
#endif

#define PACKETDATA	  (PK_TIME | PK_X | PK_Y | PK_Z | PK_CURSOR | PK_BUTTONS | PK_ORIENTATION | PK_NORMAL_PRESSURE | PK_TANGENT_PRESSURE)
 // | PK_ROTATION <- causes errors

#define PACKETMODE	0
#include <pktdef.h>

#include "wintabaccess.h"
#include "cello_tablet_JTabletCursor.h"

void PrsInit(void);
UINT PrsAdjust(PACKET p);

static HCTX ctx = NULL;
static PACKET *packet_queue = NULL;
int packet_queue_size = 0;


bool TabletAvailable() {
	// Suppress error box
	WORD errmode = SetErrorMode(SEM_NOOPENFILEERRORBOX | SEM_FAILCRITICALERRORS);
	
	// try wintab
	BOOL fResult = WTInfo(0,0,NULL);

	// restore previous error mode
	SetErrorMode(errmode);
	
	// Checks if there is any WinTab information to retrieve.
	return fResult ? true : false;
}


bool InitializeTablet(bool fullControl) {
	HWND hWnd;
	LOGCONTEXT lcMine;

	// Close tablet if already open
	CloseTablet();

	// This should never happen, but just in case.
	if (ctx != NULL) return false;

	// Grab pointer to use with WTOpen
	hWnd = GetDesktopWindow();
	if (hWnd == NULL) return false;

	// Get default region - DEFSYSCTX won't take over mouse movement
	WTInfo( fullControl ? WTI_DEFCONTEXT : WTI_DEFSYSCTX, 0, &lcMine);

	// Modify
	strcpy(lcMine.lcName, "JTablet");
	lcMine.lcPktData = PACKETDATA;
	lcMine.lcPktMode = PACKETMODE;
	lcMine.lcMoveMask = PACKETDATA;
	lcMine.lcBtnUpMask = lcMine.lcBtnDnMask;

	// Open the region
	ctx = WTOpen(hWnd, &lcMine, TRUE);

	// Return on failure
	if (ctx == NULL) return false;

	// Get packet queue size
	packet_queue_size = WTQueueSizeGet(ctx);

	DEBUG_MESSAGE2("packet size", packet_queue_size);

	// Trim, just in case
	if (packet_queue_size>1024)
		packet_queue_size=1024;

	if (packet_queue_size<=0)
		packet_queue_size=50;

	while ( packet_queue_size>0 && WTQueueSizeSet(ctx, packet_queue_size)==0) {
		int t = packet_queue_size*3/4;
		if (t<1) t = 1;
		packet_queue_size -= t;
	}

	DEBUG_MESSAGE2("packet size", packet_queue_size);

	if (packet_queue_size<=0) {
		CloseTablet();
		return false;
	}

	DEBUG_MESSAGE2("packet size", packet_queue_size);


	// Create new packet queue
	packet_queue = new PACKET[packet_queue_size];

	// Return success
	return true;
}


// Helper function for dealing with WinTab AXIS structs
void SetCursorData(JTabletCursorArrays &arrays, LOGCONTEXT &lc, AXIS &axis, jint dataType) {
	// Set maximum
	arrays.arrayMaximum[dataType] = (jint)axis.axMax;
	// Set minimum
	arrays.arrayMinimum[dataType] = (jint)axis.axMin;
	// Assuming that the data is not supported if both maximum and minimum are zero
	arrays.arraySupported[dataType] = axis.axMax || axis.axMin;
	// Set available flag so this data is not needlessly checked multiple times
	arrays.arrayIsAvailable[dataType] = true;
}

void GetCursorData(JNIEnv *env, jobject cursorObject, jint dataType) {
	// Check if initialized
	if (ctx == NULL) return;

	// Static memory to avoid unnecessary allocation
	static JTabletCursorArrays arrays;

	// Get access to the arrays
	JTablet_getCursorDataInfo(env, cursorObject, &arrays);


	// A certain amount of information is stored in the context itself
	LOGCONTEXT lc;
	BOOL ret =  WTGet(ctx, &lc);

	DEBUG_MESSAGE2("WTGet", ret);


	// AXIS structs to store information
	AXIS axis[3];

	// Switch on type of data to retrieve
	switch (dataType) {
		case cello_tablet_JTabletCursor_DATA_CURSOR:
		case cello_tablet_JTabletCursor_DATA_BUTTONS:
		// No range info.
			// Assume features are supported
			arrays.arraySupported[dataType] = true;
			// Set available flag so that GetCursorData is only called once
			arrays.arrayIsAvailable[dataType] = true;
			break;
		case cello_tablet_JTabletCursor_DATA_X:
		case cello_tablet_JTabletCursor_DATA_Y:
		case cello_tablet_JTabletCursor_DATA_Z:
			// Manually copy information from WinTab context into AXIS structs
			axis[0].axMax = lc.lcOutExtX;
			axis[0].axMin = lc.lcOutOrgX;

			axis[1].axMax = lc.lcOutExtY;
			axis[1].axMin = lc.lcOutOrgY;

			axis[2].axMax = lc.lcOutExtZ;
			axis[2].axMin = lc.lcOutOrgZ;

			// Call helper function to copy data from structs
			SetCursorData(arrays, lc, axis[0], cello_tablet_JTabletCursor_DATA_X);
			SetCursorData(arrays, lc, axis[1], cello_tablet_JTabletCursor_DATA_Y);
			SetCursorData(arrays, lc, axis[2], cello_tablet_JTabletCursor_DATA_Z);
			break;

		case cello_tablet_JTabletCursor_DATA_PRESSURE:
		case cello_tablet_JTabletCursor_DATA_TANGENT_PRESSURE:
			// Get information via WTInfo on pressure
			ret = WTInfo(WTI_DEVICES + lc.lcDevice, DVC_NPRESSURE, &axis);
			DEBUG_MESSAGE2("WTInfo(WTI_DEVICES + lc.lcDevice, DVC_NPRESSURE, &axis)", ret);
			SetCursorData(arrays, lc, axis[0], cello_tablet_JTabletCursor_DATA_PRESSURE);

			ret = WTInfo(WTI_DEVICES + lc.lcDevice, DVC_TPRESSURE, &axis);
			DEBUG_MESSAGE2("WTInfo(WTI_DEVICES + lc.lcDevice, DVC_TPRESSURE, &axis)", ret);
			SetCursorData(arrays, lc, axis[0], cello_tablet_JTabletCursor_DATA_TANGENT_PRESSURE);
			break;

		case cello_tablet_JTabletCursor_DATA_ORIENTATION_AZIMUTH:
		case cello_tablet_JTabletCursor_DATA_ORIENTATION_ALTITUDE:
		case cello_tablet_JTabletCursor_DATA_ORIENTATION_TWIST:
			// Get orientation information (fills 3 structs)
			WTInfo(WTI_DEVICES + lc.lcDevice, DVC_ORIENTATION, &axis);
			SetCursorData(arrays, lc, axis[0], cello_tablet_JTabletCursor_DATA_ORIENTATION_AZIMUTH);
			SetCursorData(arrays, lc, axis[1], cello_tablet_JTabletCursor_DATA_ORIENTATION_ALTITUDE);
			SetCursorData(arrays, lc, axis[2], cello_tablet_JTabletCursor_DATA_ORIENTATION_TWIST);

			break;

		case cello_tablet_JTabletCursor_DATA_ROTATION_PITCH:
		case cello_tablet_JTabletCursor_DATA_ROTATION_ROLL:
		case cello_tablet_JTabletCursor_DATA_ROTATION_YAW:
			// Get rotation information (fills 3 structs)
			WTInfo(WTI_DEVICES + lc.lcDevice, DVC_ROTATION, &axis);
			SetCursorData(arrays, lc, axis[0], cello_tablet_JTabletCursor_DATA_ROTATION_PITCH);
			SetCursorData(arrays, lc, axis[1], cello_tablet_JTabletCursor_DATA_ROTATION_ROLL);
			SetCursorData(arrays, lc, axis[2], cello_tablet_JTabletCursor_DATA_ROTATION_YAW);

			break;
	}

	// Remember to release the arrays so that Java VM does not die
	JTablet_releaseCursorDataInfo(env, &arrays);
}

void InitializeCursor(JNIEnv *env, jobject currentCursorObject, jint currentCursorId, jint cursorPhysicalId) {
	// Get name of cursor
	jchar *cursorName = new jchar[128];
	// Use wide version of function for Unicode support
	WTInfoW(WTI_CURSORS + currentCursorId, CSR_NAME, cursorName);
	// Convert to Java string
	jstring cursorNameString = env->NewString(cursorName, wcslen(cursorName));
	delete cursorName;

	// Get cursor type
	UINT cursorType = 0;
	if (WTInfo( WTI_CURSORS + currentCursorId, CSR_TYPE, &cursorType ) != sizeof(cursorType))
		cursorType = 0;

	// Convert to JTablet types
	jint
		cursorGeneralType = cello_tablet_JTabletCursor_TYPE_UNKNOWN,
		cursorSpecificType = cello_tablet_JTabletCursor_TYPE_UNKNOWN;

	// Using mask information, we can find the type of input and tip
	if (cursorType) {
		switch (cursorType & CSR_TYPE_SPECIFIC_MASK) {
			case CSR_TYPE_SPECIFIC_STYLUS_BITS:
				cursorSpecificType = cello_tablet_JTabletCursor_TYPE_STYLUS;
				break;

			case CSR_TYPE_SPECIFIC_AIRBRUSH_BITS:
				cursorSpecificType = cello_tablet_JTabletCursor_TYPE_AIRBRUSH;
				break;

			case CSR_TYPE_SPECIFIC_4DMOUSE_BITS:
				cursorSpecificType = cello_tablet_JTabletCursor_TYPE_4DMOUSE;
				break;

			case CSR_TYPE_SPECIFIC_LENSCURSOR_BITS:
				cursorSpecificType = cello_tablet_JTabletCursor_TYPE_LENS_CURSOR;
				break;
		}
		switch (cursorType & CSR_TYPE_GENERAL_MASK) {
			case CSR_TYPE_GENERAL_PENTIP:
				cursorGeneralType = cello_tablet_JTabletCursor_TYPE_PEN_TIP;
				break;

			case CSR_TYPE_GENERAL_PENERASER:
				cursorGeneralType = cello_tablet_JTabletCursor_TYPE_PEN_ERASER;
				break;

			case CSR_TYPE_GENERAL_PUCK:
				cursorGeneralType = cello_tablet_JTabletCursor_TYPE_PUCK;
				break;

		}
	}

	// Set the information in the current cursor object
	JTablet_setCursorInfo(env, currentCursorObject, cursorNameString, cursorPhysicalId, cursorGeneralType, cursorSpecificType);
}

// Static variables to make ReadTablet run smoothly
static UINT currentCursorId = -1, currentCursorPhysicalId = 0;
static jobject currentCursorObject = NULL;

jobject ReadTablet(JNIEnv *env, bool pollToLatest) {

	if (ctx == NULL) return NULL;

	int retrieved = 0, i;
	PACKET p;
	// Get packet
	if (pollToLatest) {
		// Retrieve all available packets until WTPacketsGet returns zero
		i = WTPacketsGet(ctx, packet_queue_size, packet_queue);
		DEBUG_MESSAGE2("WTPacketsGet(ctx, packet_queue_size, packet_queue)",i);
		while (i > 0) {
			retrieved = i;
			// Set the current packet to the last packet retrieved.
			p = packet_queue[retrieved-1];
			i = WTPacketsGet(ctx, packet_queue_size, packet_queue);
			DEBUG_MESSAGE2("WTPacketsGet(ctx, packet_queue_size, packet_queue)",i);
		}
	} else {
		// Retrieve a single packet
		if ((retrieved = WTPacketsGet(ctx, 1, packet_queue)) > 0)
			p = packet_queue[0];
	}

	DEBUG_MESSAGE2("retrieved", retrieved);

	// If we have not retrieved any packets, return NULL.
	if (!retrieved)
		return NULL;

	DEBUG_MESSAGE2("Cursor",p.pkCursor);
	// Return null on packets with invalid cursors
	if (p.pkCursor < 0)
		return NULL;

	// Get the physical ID of the cursor (not supported by all tablets)
	UINT cursorPhysicalId = 0;
	WTInfo( WTI_CURSORS + p.pkCursor, CSR_PHYSID, &cursorPhysicalId );

	DEBUG_MESSAGE2("Cursor ID",cursorPhysicalId);
	DEBUG_MESSAGE2("time",p.pkTime);

	// Cursors are stored by functions in the JTablet.h function files by physical and cursor id

	// If we have a different cursor or physical id than the last time ReadTablet was called...
	if (currentCursorPhysicalId != cursorPhysicalId || currentCursorId != p.pkCursor) {
		// Update stored ids
		currentCursorPhysicalId = cursorPhysicalId;
		currentCursorId = p.pkCursor;

		// Get the cursor object associated with the current cursor and physical id
		currentCursorObject = JTablet_getCursorObject(currentCursorId, currentCursorPhysicalId);
		if (currentCursorObject == NULL) {
			// If it is null, we make a new cursor
			currentCursorObject = JTablet_makeCursor(env, currentCursorId, currentCursorPhysicalId);

			// If that is null, we return NULL do to failure
			if (currentCursorObject == NULL)
				return NULL;

			// And we initialize the cursor
			InitializeCursor(env, currentCursorObject, currentCursorId, cursorPhysicalId);
		}
	}

	// Set the cursor time from packet (most tablets do not seem to support this)
	JTablet_setCursorTime(env, currentCursorObject, (jlong)p.pkTime);


	// Get the java array and actual integer array using a function from JTablet.h

	jintArray javaArray;
	jint *array = NULL;
	JTablet_getCursorDataValueRaw(env, currentCursorObject, javaArray, &array);

	// We can now copy the information to the array prtty easily:
	array[cello_tablet_JTabletCursor_DATA_CURSOR] 				= p.pkCursor;
	array[cello_tablet_JTabletCursor_DATA_BUTTONS]				= p.pkButtons;
	array[cello_tablet_JTabletCursor_DATA_X]					= p.pkX;
	array[cello_tablet_JTabletCursor_DATA_Y]					= p.pkY;
	array[cello_tablet_JTabletCursor_DATA_Z]					= p.pkZ;

	array[cello_tablet_JTabletCursor_DATA_PRESSURE]				= p.pkNormalPressure;
	array[cello_tablet_JTabletCursor_DATA_TANGENT_PRESSURE]		= p.pkTangentPressure;

	array[cello_tablet_JTabletCursor_DATA_ORIENTATION_AZIMUTH]	= p.pkOrientation.orAzimuth;
	array[cello_tablet_JTabletCursor_DATA_ORIENTATION_ALTITUDE]	= p.pkOrientation.orAltitude;
	array[cello_tablet_JTabletCursor_DATA_ORIENTATION_TWIST]	= p.pkOrientation.orTwist;


	// I have not been able to get rotation information to work correctly, so this information
	// is only copied if we are retrieving packets with this information.  (Otherwise the PACKET
	// struct will not even have these elements.)
#if (PACKETDATA & PK_ROTATION)
	array[cello_tablet_JTabletCursor_DATA_ROTATION_PITCH]		= p.pkRotation.roPitch;
	array[cello_tablet_JTabletCursor_DATA_ROTATION_ROLL]		= p.pkRotation.roRoll;
	array[cello_tablet_JTabletCursor_DATA_ROTATION_YAW]			= p.pkRotation.roYaw;
#endif

	// And we must remember to release the array so that the Java VM does not die
	JTablet_releaseCursorDataValueRaw(env, javaArray, &array);
	DEBUG_MESSAGE("ReadTablet)");

	return currentCursorObject;
}



void CloseTablet() {
	// Close context if available.
	if (ctx) {
		WTClose(ctx);
		ctx = NULL; // Fixes possible bug in 0.9.3
	}

	// Reset static variables
	currentCursorId = -1;
	currentCursorPhysicalId = 0;
	currentCursorObject = NULL; // This line fixed crash in v0.9.1/0.9.2

	// Delete the packet queue
	if (packet_queue != NULL) {
		delete packet_queue;
		packet_queue = NULL;
	}
}

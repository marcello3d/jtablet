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
 *   JTablet.h - A collection of cross-platform functions using
 * only JNI routines and standard or JNI datatypes.
 *
 *   All implementations of tabletaccess.h will need to use this
 * file.
 *
 *  Last update: March 8, 2004
 *  For version: 0.9.4
 *
 ***************************************************************/

#ifndef _JTABLET_H
#define _JTABLET_H

#include <jni.h>
#include <jni_md.h>

// Some possibly useful debugging functions used in the windows port.
#ifdef DEBUG
#include <stdio.h>

#define DEBUG_MESSAGE(msg)			printf("Debug: %s\n",msg)
#define DEBUG_MESSAGE2(msg,i)		printf("Debug: %s: %i\n",msg,i)
#else
#define DEBUG_MESSAGE
#define DEBUG_MESSAGE2
#endif

// Creates and throws a JTabletException with the specified message.
bool JTablet_ThrowException(JNIEnv *env, const char *message);

// Gets the class of the cursor object
jclass JTablet_getCursorClass(JNIEnv *env);

// Gets the cursor jobject based on a cursorId and physicalId
//  returns null if a cursor hasn't been made for for that cursor/physical id pair.
jobject JTablet_getCursorObject(jint cursorId, jint cursorPhysicalId);
// Constructs and returns a new cursor jobject, does NOT check if
//  there already is an existing cursor.
jobject JTablet_makeCursor(JNIEnv *env, jint cursorId, jint cursorPhysicalId);
// Deletes any cursor created with JTablet_makeCursor
void JTablet_deleteCursors(JNIEnv *env);

// Sets various static information on a particular cursor (generally this should only be done 
//  once at creation of a cursor object)
// if cursorName is zero, it won't set any name to the cursor
void JTablet_setCursorInfo(JNIEnv *env, jobject cursor, jstring cursorName, jint physicalId, jint cursorGeneralType, jint cursorSpecificType);

// Sets the current jlong (64bit int) packet time on the cursor
//  this should be called from every ReadPacket call when a valid packet is read
void JTablet_setCursorTime(JNIEnv *env, jobject cursor, jlong time);

// Retrieves access to the raw data array in the specified cursor object, this should be
//  called when a packet of information is ready to be inserted into the JTabletCursor
//  object, the following code from wintabaccess.cpp is an example of usage:
//
// 	jintArray javaArray;
//	jint *array = NULL;
//	JTablet_getCursorDataValueRaw(env, currentCursorObject, javaArray, &array);
//
//	array[cello_tablet_JTabletCursor_DATA_X] = ... ;
//	...
//
//	JTablet_releaseCursorDataValueRaw(env, javaArray, &array);
//
void JTablet_getCursorDataValueRaw(JNIEnv *env, jobject cursorObject, jintArray &javaArray, jint **array);
// Then you must call the release function once you're done.  You shouldn't touch
//  array once before you've called get, or after you call release.  See example
//  code above from wintabaccess.cpp.
void JTablet_releaseCursorDataValueRaw(JNIEnv *env, jintArray &javaArray, jint **array);


// A struct that holds all the necessary data to fill out data ranges from the GetCursorData
//  function.
struct JTabletCursorArrays {
	jintArray 		javaArrayMaximum,
					javaArrayMinimum;
					
	jbooleanArray	javaArraySupported,
					javaArrayIsAvailable;
				
	jint 			*arrayMaximum,
					*arrayMinimum;
			
	jboolean		*arraySupported,
					*arrayIsAvailable;
};

// This function must be called to set any data necessary in the GetCursorData function.
//  Pass in a pointer to a JTabletCursorArrays struct, and then access the individual
//  arrays inside the struct to assign variables.  Again, look at wintabaccess.cpp
//  for intended usage.
void JTablet_getCursorDataInfo(JNIEnv *env, jobject cursorObject, JTabletCursorArrays *arrays);
// Once you're done assigning variaibles, you must release the access again
//  by passing a pointer to the same struct into the following function.
void JTablet_releaseCursorDataInfo(JNIEnv *env, JTabletCursorArrays *arrays);

#endif

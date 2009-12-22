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
 *   cello_tablet_JTablet.h - The main entry point into the
 * native code for JTablet.  This function essentially wraps
 * all the functions in tabletaccess.h
 *
 *  Last update: March 8, 2004
 *  For version: 0.9.4
 *
 ***************************************************************/

#include <jni.h>
#include <jni_md.h>
#include "cello_tablet_JTablet.h"
#include "cello_tablet_JTabletCursor.h"
#include "JTablet.h"

#include "tabletaccess.h"

/*
 * Class:     cello_tablet_JTablet
 * Method:    tabletAvailable
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_cello_tablet_JTablet_tabletAvailable (JNIEnv * env, jclass jcls) {

	return (jboolean)TabletAvailable();
}

/*
 * Class:     cello_tablet_JTablet
 * Method:    getLibraryVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_cello_tablet_JTablet_getLibraryVersion (JNIEnv *env, jclass jcls) {
	  return env->NewStringUTF("0.9.5 BETA");
}


jboolean lock = false;

/*
 * Class:     cello_tablet_JTablet
 * Method:    initializeTablet
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cello_tablet_JTablet_initializeTablet (JNIEnv * env, jclass jcls, jboolean fullControl) {
	while (lock)
		;
	lock = true;

	if (!InitializeTablet(fullControl))
		JTablet_ThrowException(env,"Could not initialize tablet.");
	lock = false;
}

/*
 * Class:     cello_tablet_JTablet
 * Method:    closeTablet
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cello_tablet_JTablet_closeTablet (JNIEnv *env, jclass jcls) {
	while (lock)
		;
	lock = true;

	DEBUG_MESSAGE("Closing tablet");
	CloseTablet();
	DEBUG_MESSAGE("Deleting cursors");
	JTablet_deleteCursors(env);

	lock = false;
}

/*
 * Class:     cello_tablet_JTablet
 * Method:    pollCursor
 * Signature: ()Lcello/tablet/JTabletCursor;
 */
JNIEXPORT jobject JNICALL Java_cello_tablet_JTablet_pollCursor (JNIEnv *env, jclass jcls, jboolean pollToLatest) {
	while (lock)
		;
	lock = true;

	jobject obj = ReadTablet(env, pollToLatest);

	lock = false;

	return obj;
}

/*
 * Class:     cello_tablet_JTablet
 * Method:    getCursorData
 * Signature: (Lcello/tablet/JTabletCursor;I)V
 */
JNIEXPORT void JNICALL Java_cello_tablet_JTablet_getCursorData (JNIEnv *env, jclass jcls, jobject cursor, jint dataType) {
	while (lock)
		;
	lock = true;

	GetCursorData(env, cursor, dataType);

	lock = false;
}

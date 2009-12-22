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
 *   JTablet.cpp - Implementations of functions in JTablet.h
 *
 *  Last update: March 8, 2004
 *  For version: 0.9.4
 *
 ***************************************************************/

#include "JTablet.h"

///// Cursor node definition
struct JTabletCursor_s {
	jint cursorId;
	jint cursorPhysicalId;
	jobject javaObject;
	JTabletCursor_s *next;
};



///// Static variables
static JTabletCursor_s *cursorListHead = 0;
static jclass classJTabletCursor = 0;
jfieldID 
	field_dataValueRaw = 0,	
	field_dataMaximum = 0,	
	field_dataMinimum = 0,	
	field_dataIsAvailable = 0,
	field_dataSupported = 0,
	field_cursorName = 0,
	field_physicalId = 0,
	field_cursorTypeGeneral = 0,
	field_cursorTypeSpecific = 0,
	field_dataTime = 0;

///// Function implementations

jobject JTablet_getCursorObject(jint cursorId, jint cursorPhysicalId) {
	JTabletCursor_s *node = cursorListHead;
	while (node!=NULL) {
		if (node->cursorId == cursorId && node->cursorPhysicalId == cursorPhysicalId)
			return node->javaObject;
		node = node->next;
	}
	return NULL;
}

jclass JTablet_getCursorClass(JNIEnv *env) {
	//if (classJTabletCursor == 0) 
	{
		DEBUG_MESSAGE("env->FindClass(\"cello/tablet/JTabletCursor\")");
		
		classJTabletCursor = env->FindClass("cello/tablet/JTabletCursor");
		if (classJTabletCursor == 0)
			return 0;
		
		DEBUG_MESSAGE("find success");
		field_dataValueRaw			= env->GetFieldID(classJTabletCursor, "dataValueRaw",		"[I");	
		
		field_dataMaximum			= env->GetStaticFieldID(classJTabletCursor, "dataMaximum",		"[I");
		if (field_dataMaximum==0)
			DEBUG_MESSAGE("Failed to get static field dataMaximum");
		
		field_dataMinimum			= env->GetStaticFieldID(classJTabletCursor, "dataMinimum",		"[I");	
		if (field_dataMaximum==0)
			DEBUG_MESSAGE("Failed to get static field field_dataMinimum");
		
		field_dataIsAvailable		= env->GetStaticFieldID(classJTabletCursor, "dataIsAvailable",	"[Z");
		if (field_dataIsAvailable==0)
			DEBUG_MESSAGE("Failed to get static field field_dataIsAvailable");
		
		field_dataSupported			= env->GetStaticFieldID(classJTabletCursor, "dataSupported",		"[Z");
		if (field_dataSupported==0)
			DEBUG_MESSAGE("Failed to get static field field_dataSupported");
		
		field_cursorName			= env->GetFieldID(classJTabletCursor, "cursorName",			"Ljava/lang/String;");
		field_physicalId			= env->GetFieldID(classJTabletCursor, "physicalId",			"I");
		field_cursorTypeGeneral		= env->GetFieldID(classJTabletCursor, "cursorTypeGeneral",	"I");
		field_cursorTypeSpecific	= env->GetFieldID(classJTabletCursor, "cursorTypeSpecific",	"I");
		field_dataTime				= env->GetFieldID(classJTabletCursor, "dataTime",			"J");
	}

	return classJTabletCursor;
}


void JTablet_setCursorInfo(JNIEnv *env, jobject cursor, jstring string, jint physicalId, jint cursorGeneralType, jint cursorSpecificType) {
	if (string)
		env->SetObjectField(cursor, field_cursorName, 			string);
	env->SetIntField(cursor,	field_physicalId,			physicalId);
	env->SetIntField(cursor,	field_cursorTypeGeneral,	cursorGeneralType);
	env->SetIntField(cursor,	field_cursorTypeSpecific,	cursorSpecificType);
}

void JTablet_setCursorTime(JNIEnv *env, jobject cursor, jlong time) {
	env->SetLongField(cursor,	field_dataTime,				time);
}

void JTablet_getCursorDataValueRaw(JNIEnv *env, jobject cursorObject, jintArray &javaArray, jint **array) {
	javaArray = (jintArray)env->GetObjectField(cursorObject, field_dataValueRaw);
	if (javaArray)
		*array = env->GetIntArrayElements(javaArray, NULL);
}
void JTablet_releaseCursorDataValueRaw(JNIEnv *env, jintArray &javaArray, jint **array) {
	env->ReleaseIntArrayElements(javaArray, *array, 0);
}

void JTablet_getCursorDataInfo(JNIEnv *env, jobject cursorObject, JTabletCursorArrays *arrays) {
	arrays->javaArrayMaximum	 = (jintArray)		env->GetStaticObjectField(classJTabletCursor, field_dataMaximum);
	arrays->javaArrayMinimum	 = (jintArray)		env->GetStaticObjectField(classJTabletCursor, field_dataMinimum);
	arrays->javaArraySupported	 = (jbooleanArray)	env->GetStaticObjectField(classJTabletCursor, field_dataSupported);
	arrays->javaArrayIsAvailable = (jbooleanArray)	env->GetStaticObjectField(classJTabletCursor, field_dataIsAvailable);


	if (arrays->javaArrayMaximum)
		arrays->arrayMaximum		= env->GetIntArrayElements(arrays->javaArrayMaximum,			NULL);

	if (arrays->javaArrayMinimum)
		arrays->arrayMinimum		= env->GetIntArrayElements(arrays->javaArrayMinimum,			NULL);

	if (arrays->javaArraySupported)
		arrays->arraySupported		= env->GetBooleanArrayElements(arrays->javaArraySupported,		NULL);

	if (arrays->javaArrayIsAvailable)
		arrays->arrayIsAvailable	= env->GetBooleanArrayElements(arrays->javaArrayIsAvailable,	NULL);	
}
void JTablet_releaseCursorDataInfo(JNIEnv *env, JTabletCursorArrays *arrays) {
	env->ReleaseIntArrayElements(		arrays->javaArrayMaximum,		arrays->arrayMaximum,		0);
	env->ReleaseIntArrayElements(		arrays->javaArrayMinimum,		arrays->arrayMinimum,		0);
	env->ReleaseBooleanArrayElements(	arrays->javaArraySupported,		arrays->arraySupported,		0);
	env->ReleaseBooleanArrayElements(	arrays->javaArrayIsAvailable,	arrays->arrayIsAvailable,	0);
}

jobject JTablet_makeCursor(JNIEnv *env, jint cursorId, jint cursorPhysicalId) {
	DEBUG_MESSAGE("JTablet_makeCursor(");
	// Get class for JTabletCursor object
	if (JTablet_getCursorClass(env) == 0)
		return NULL;
		
	DEBUG_MESSAGE("AllocObject");
	// Allocate an instance of it 
	///// NOTE: Does not call constructor!
	jobject cursorObject = env->AllocObject(classJTabletCursor);
	if (cursorObject == 0)
		return NULL;
		
	DEBUG_MESSAGE("<init>");
	// Get constructor method for class
	jmethodID objectConstructor = env->GetMethodID(classJTabletCursor, "<init>", "()V");
	if (objectConstructor == 0)
		return NULL;
	// Call constructor
	env->CallVoidMethod(cursorObject, objectConstructor);

	DEBUG_MESSAGE("global ref");
	///// New Global Ref <
	// Make global reference - This lets us get back at the object at a later point without problems
	cursorObject = env->NewGlobalRef(cursorObject);
	if (cursorObject == 0)
		return NULL;
	
	DEBUG_MESSAGE("new node");
	/// New Node <
	JTabletCursor_s *new_node = new JTabletCursor_s;
	
	DEBUG_MESSAGE("fill node");
	// Fill data
	new_node->next = cursorListHead;
	new_node->javaObject = cursorObject;
	new_node->cursorId = cursorId;
	new_node->cursorPhysicalId = cursorPhysicalId;
	
	DEBUG_MESSAGE("insert node");
	// Insert node into linked list
	cursorListHead = new_node;
	
	DEBUG_MESSAGE("JTablet_makeCursor);");
	return cursorObject;
}
void JTablet_deleteCursors(JNIEnv *env) {
	JTabletCursor_s *node = cursorListHead, *temp_node;
	int count = 0;
	while (node!=NULL) {
		temp_node = node->next;
		
		///// Delete Global Ref >
		env->DeleteGlobalRef(node->javaObject);
		
		/// Delete Node >
		delete node;
		
		count++;
		
		node = temp_node;
	}
	cursorListHead = NULL;
	DEBUG_MESSAGE2("Deleted nodes",count);
}




bool JTablet_ThrowException(JNIEnv *env, const char *message) {
	env->ExceptionDescribe();
	env->ExceptionClear();

	jclass newExcCls = env->FindClass("cello/tablet/JTabletException");
	if (newExcCls == 0) // Unable to find the new exception class, give up.
		return false;
		
	env->ThrowNew(newExcCls, message);
	return true;
}



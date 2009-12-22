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
 *   wintabaccess.h - Header for windows JTablet implementation
 *
 *  Last update: March 8, 2004
 *  For version: 0.9.4
 *
 ***************************************************************/

#ifndef _WINTABACCESS_H
#define _WINTABACCESS_H

#include "tabletaccess.h"

void InitializeCursor(JNIEnv *env, jobject currentCursorObject, jint currentCursorId, jint cursorPhysicalId);

// The following was pulled from the DuoHand example in the wintab kit

//////////////////////////////////////////////////////////////////////////////
// The CSR_TYPE WTInfo data item is new to Wintab 1.2 and is not defined
// in the Wintab 1.26 SDK, so we have to define it.
#ifndef CSR_TYPE
#	define CSR_TYPE 20
#endif

// The upper two bits of the WTInfo value returned for 
// WTInfo( WTI_CURSORS + wCursorIndex, ...)identify general cursors types as
// shown here.

#define CSR_TYPE_GENERAL_MASK			( ( UINT ) 0xC000 )
#define CSR_TYPE_GENERAL_PENTIP			( ( UINT ) 0x4000 )
#define CSR_TYPE_GENERAL_PUCK			( ( UINT ) 0x8000 )
#define CSR_TYPE_GENERAL_PENERASER		( ( UINT ) 0xC000 )

// This mask can be used to more specifically identify a cursor using the
// CSR_TYPE value.

#define CSR_TYPE_SPECIFIC_MASK 					( ( UINT ) 0x0F06 )

#define CSR_TYPE_SPECIFIC_STYLUS_BITS			( ( UINT ) 0x0802 )
#define CSR_TYPE_SPECIFIC_AIRBRUSH_BITS			( ( UINT ) 0x0902 )
#define CSR_TYPE_SPECIFIC_4DMOUSE_BITS			( ( UINT ) 0x0004 )
#define CSR_TYPE_SPECIFIC_LENSCURSOR_BITS		( ( UINT ) 0x0006 )


#endif

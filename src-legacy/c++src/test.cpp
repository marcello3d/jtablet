
//#include <iostream>

#include <cstdio>
#include <string.h>
#include <windows.h>
#include <stdlib.h>

/* wintab */
#include <wintab.h>
#ifdef USE_X_LIB
#include <wintabx.h>
#endif


int main() {

	//cout << "Tablet test app by Marcello Bastea-Forte" << endl;

	printf("Tablet test app by Marcello Bastea-Forte\n");

	printf("WTInfo(0, 0, NULL) = %i\n",WTInfo(0, 0, NULL));

	//cout << "WTInfo(0, 0, NULL) = " << WTInfo(0, 0, NULL) << endl;

	HWND hWnd;



	hWnd = GetDesktopWindow();
	if (hWnd == NULL) {
		printf("Could not get desktop\n");
		//cout << "could not get desktop" << endl;
		return -1;
	}

	LOGCONTEXT lcMine;
	/* get default region - DEFSYSCTX won't take over mouse movement */
	WTInfo( WTI_DEFSYSCTX, 0, &lcMine);

	/* modify the digitizing region */
	strcpy(lcMine.lcName, "JTablet");
	lcMine.lcMoveMask = 0;
	lcMine.lcBtnUpMask = lcMine.lcBtnDnMask;

	/* open the region */
	HCTX ctx = NULL;
	ctx = WTOpen(hWnd, &lcMine, TRUE);

	printf("WTOpen(hWnd, &lcMine, TRUE) = %i\n", ctx);

	if (ctx) {
		printf("WTClose(ctx) = %i\n", WTClose(ctx));
	}



	return 0;
}
;/***************************************************************
; *
; * JTablet is an open-source native Tablet library for Java by
; *    Marcello Bastéa-Forte (marcello@cellosoft.com
; *
; *   You are free to modify this code as you wish, but any
; *   useful/significant changes should be contributed back to
; *   original project.  *This entire message should remain intact*
; *
; *  If you are interested in using JTablet in commercial 
; *  projects, please contact me at marcello@cellosoft.com
; *
; ***************************************************************/
;
;/***************************************************************
; *
; * File information:
; *
; *   jtablet2.nsi - Nullsoft Install System script that installs
; * JTablet for Windows painlessly.
; *
; *  Last update: March 8, 2004
; *  For version: 0.9.4
; *
; ***************************************************************/

SetCompressor lzma 

!define PRODUCT_NAME "JTablet"
!define PRODUCT_VERSION "v0.9.5"
!define PRODUCT_PUBLISHER "Cellosoft"
!define PRODUCT_WEB_SITE "http://cellosoft.com/sketchstudio/"
!define PRODUCT_FOLDER "${PRODUCT_PUBLISHER}\${PRODUCT_NAME}"
!define PRODUCT_FULL_NAME "${PRODUCT_PUBLISHER} ${PRODUCT_NAME} ${PRODUCT_VERSION}"


!include "MUI.nsh"
;!define UPGRADEDLL_NOREGISTER
;!include "UpgradeDLL.nsh"

!include Library.nsh



;--------------------------------
;Configuration

XPStyle on
  ;General
  Name "${PRODUCT_FULL_NAME}"
  OutFile "JTabletSetup${PRODUCT_VERSION}.exe"

  ;Folder selection page
  InstallDir "$PROGRAMFILES\Cellosoft\JTablet"
  
  ;Remember install folder
  InstallDirRegKey HKCU "Software\Cellosoft\JTablet" ""
  
  ;Remember the installer language
  !define MUI_LANGDLL_REGISTRY_ROOT "HKCU" 
  !define MUI_LANGDLL_REGISTRY_KEY "Software\Cellosoft\${PRODUCT_NAME}" 
  !define MUI_LANGDLL_REGISTRY_VALUENAME "Installer Language"

 ; !define MUI_BRANDINGTEXT "Nullsoft Install System "
  BrandingText "Nullsoft Install System"

;--------------------------------
;Modern UI Configuration

!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "jtablet-installer-header.bmp"

!define MUI_ABORTWARNING
!define MUI_COMPONENTSPAGE_SMALLDESC
  
; Welcome page
!insertmacro MUI_PAGE_WELCOME


!insertmacro MUI_PAGE_COMPONENTS

; Directory page
!insertmacro MUI_PAGE_DIRECTORY

!define MUI_FINISHPAGE_NOAUTOCLOSE

; Instfiles page
!insertmacro MUI_PAGE_INSTFILES


!define MUI_FINISHPAGE_LINK "Open the Sketchstudio Website"
!define MUI_FINISHPAGE_LINK_LOCATION http://cellosoft.com/sketchstudio/

!define MUI_FINISHPAGE_TEXT "${PRODUCT_FULL_NAME} has been installed on your computer.\r\n\r\nYou may need to restart your web browser before using JTablet.\r\n\r\nClick Finish to close this wizard."

!insertmacro MUI_PAGE_FINISH

; Uninstaller pages

!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!define MUI_UNABORTWARNING

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
  

;--------------------------------
;Languages


  !insertmacro MUI_LANGUAGE "English"
  !insertmacro MUI_LANGUAGE "French"
;  !insertmacro MUI_LANGUAGE "German"
  !insertmacro MUI_LANGUAGE "Spanish"
;  !insertmacro MUI_LANGUAGE "SimpChinese"
;  !insertmacro MUI_LANGUAGE "TradChinese"    
  !insertmacro MUI_LANGUAGE "Japanese"
;  !insertmacro MUI_LANGUAGE "Korean"
;  !insertmacro MUI_LANGUAGE "Italian"
;  !insertmacro MUI_LANGUAGE "Dutch"
;  !insertmacro MUI_LANGUAGE "Danish"
;  !insertmacro MUI_LANGUAGE "Greek"
;  !insertmacro MUI_LANGUAGE "Russian"
  !insertmacro MUI_LANGUAGE "PortugueseBR"
;  !insertmacro MUI_LANGUAGE "Polish"
;  !insertmacro MUI_LANGUAGE "Ukrainian"
;  !insertmacro MUI_LANGUAGE "Czech"
;  !insertmacro MUI_LANGUAGE "Slovak"
;  !insertmacro MUI_LANGUAGE "Croatian"
;  !insertmacro MUI_LANGUAGE "Bulgarian"
;  !insertmacro MUI_LANGUAGE "Hungarian"
;  !insertmacro MUI_LANGUAGE "Thai"
;  !insertmacro MUI_LANGUAGE "Romanian"
;  !insertmacro MUI_LANGUAGE "Macedonian"
;  !insertmacro MUI_LANGUAGE "Turkish"


;--------------------------------
;Language Strings

;Descriptions
LangString DESC_SecCore              ${LANG_ENGLISH} "Various required JTablet Plugin core files."
LangString DESC_SectionPlugins       ${LANG_ENGLISH} "Browsers and Java plugins to install JTablet into."
LangString DESC_SectionInstallIE     ${LANG_ENGLISH} "Installs JTablet into Internet Explorer's Trusted Java Plugins Folder."
LangString DESC_SectionInstallJava2  ${LANG_ENGLISH} "Installs JTablet into any currently versions of the Java 2 Plugin, JRE, and SDK."
LangString DESC_SectionShortcuts     ${LANG_ENGLISH} "Creates Start Menu shortcuts to useful JTablet-related websites and the JTablet Uninstaller."


;--------------------------------
;Reserve Files
  
  ;Things that need to be extracted on first (keep these lines before any File command!)
  ;Only useful for BZIP2 compression
  !insertmacro MUI_RESERVEFILE_LANGDLL
  
;--------------------------------
;Installer Sections

Section "Required Files" SecCore
  ; read-only
  SectionIn RO
  
  CreateDirectory $INSTDIR

  SetOverwrite on	
  SetOutPath $INSTDIR
  File "..\dist\jtablet.jar"
  File "jtablet.dll"
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  ; Install DLL
  SetOutPath $SYSDIR
  ;File "jtablet.dll"
  ;!insertmacro UpgradeDLL "jtablet.dll" "$SYSDIR\jtablet.dll" "$SYSDIR"

  !insertmacro InstallLib DLL NOTSHARED REBOOT_NOTPROTECTED jtablet.dll $SYSDIR\jtablet.dll $SYSDIR\temp

    

  ; add files / whatever that need to be installed here.
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Cellosoft\JTablet" "Install" $INSTDIR
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\JTablet" "DisplayName" "${PRODUCT_NAME}"
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\JTablet" "UninstallString" '"$INSTDIR\Uninstall.exe"'

SectionEnd

SubSection "Browser/Plugin Installations" SectionPlugins

	Section "Install for Internet Explorer" SectionInstallIE
	  SetOutPath "$WINDIR\java\trustlib\cello\tablet"
	  File "..\dist\classes\cello\tablet\JTablet.class"
	  File "..\dist\classes\cello\tablet\JTabletCursor.class"
	  File "..\dist\classes\cello\tablet\JTabletException.class"

	SectionEnd

	Section "Install for Java 2 Plugin" SectionInstallJava2
		Push $0
		Push $1
		Push $2
		Push $3
		IntOp $2 0 + 0
		InstallAgainJPLUG:
			ClearErrors
			EnumRegKey $3 HKEY_LOCAL_MACHINE "Software\JavaSoft\Java Plugin\" $1
			ReadRegStr $0 HKEY_LOCAL_MACHINE "Software\JavaSoft\Java Plugin\$3" "JavaHome"
			IfErrors AllDoneJPLUG
			DetailPrint "Found Java 2 Plugin v$3"
			IntOp $1 $1 + 1
			SetOutPath "$0\lib\ext\"
			File "..\dist\jtablet.jar"
			WriteINIStr "$INSTDIR\install.log" "files" "$2" "$0\lib\ext\"
			DetailPrint ""
			IntOp $2 $2 + 1
			Goto InstallAgainJPLUG
		AllDoneJPLUG:
		
		IntOp $1 0 + 0
		InstallAgainJRE:
			ClearErrors

			EnumRegKey $3 HKEY_LOCAL_MACHINE "Software\JavaSoft\Java Runtime Environment\" $1
			ReadRegStr $0 HKEY_LOCAL_MACHINE "Software\JavaSoft\Java Runtime Environment\$3" "JavaHome"
			IfErrors AllDoneJRE

			DetailPrint "Found Java 2 Runtime Environment v$3"
			IntOp $1 $1 + 1
			SetOutPath "$0\lib\ext\"
			File "..\dist\jtablet.jar"
			WriteINIStr "$INSTDIR\install.log" "files" "$2" "$0\lib\ext\"
			IntOp $2 $2 + 1

			Goto InstallAgainJRE
		AllDoneJRE:
		
		IntOp $1 0 + 0
		InstallAgainJSDK:
			ClearErrors
			EnumRegKey $3 HKEY_LOCAL_MACHINE "Software\JavaSoft\Java Development Kit\" $1
			ReadRegStr $0 HKEY_LOCAL_MACHINE "Software\JavaSoft\Java Development Kit\$3" "JavaHome"
			IfErrors AllDoneJSDK
			DetailPrint "Found Java 2 SDK v$3"
			IntOp $1 $1 + 1
			SetOutPath "$0\jre\lib\ext\"
			File "..\dist\jtablet.jar"
			WriteINIStr "$INSTDIR\install.log" "files" "$2" "$0\jre\lib\ext\"
			DetailPrint ""
			IntOp $2 $2 + 1
			Goto InstallAgainJSDK
		AllDoneJSDK:
	SectionEnd
SubSectionEnd

Section "Create Shortcuts in Start Menu" SectionShortcuts
  CreateDirectory "$SMPROGRAMS\Cellosoft\JTablet"
  CreateShortCut "$SMPROGRAMS\Cellosoft\JTablet\Uninstall JTablet Plugin.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\Cellosoft\JTablet\Cellosoft Website.lnk" "http://www.cellosoft.com/"
  CreateShortCut "$SMPROGRAMS\Cellosoft\JTablet\2draw Website.lnk" "http://2draw.net/"
  CreateShortCut "$SMPROGRAMS\Cellosoft\JTablet\Cellosoft Sketchstudio Website.lnk" "http://cellosoft.com/sketchstudio/"
SectionEnd

;Display the Finish header
;Insert this macro after the sections if you are not using a finish page
;!insertmacro MUI_SECTIONS_FINISHHEADER

;--------------------------------
;Installer Functions

Function .onInit

  !insertmacro MUI_LANGDLL_DISPLAY

FunctionEnd

;--------------------------------
;Descriptions

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecCore} $(DESC_SecCore)
  !insertmacro MUI_DESCRIPTION_TEXT ${SectionPlugins} $(DESC_SectionPlugins)
  !insertmacro MUI_DESCRIPTION_TEXT ${SectionInstallIE} $(DESC_SectionInstallIE)
  !insertmacro MUI_DESCRIPTION_TEXT ${SectionInstallJava2} $(DESC_SectionInstallJava2)
  !insertmacro MUI_DESCRIPTION_TEXT ${SectionShortcuts} $(DESC_SectionShortcuts)
!insertmacro MUI_FUNCTION_DESCRIPTION_END
 
;--------------------------------
;Uninstaller Section

Section "Uninstall"

	Push $0
	Push $1
	Push $2
	Push $3
	
	IntOp $1 0 + 0
	UnInstallAgain:
		ClearErrors
		ReadINIStr $0 "$INSTDIR\install.log" "files" $1
		IfErrors AllDone
		
		IntOp $1 $1 + 1
		Delete "$0jtablet.jar"
		ClearErrors
		IntOp $2 $2 + 1
		Goto UnInstallAgain
	AllDone:


	Delete "$INSTDIR\install.log"
	Delete "$INSTDIR\jtablet.jar"
	Delete "$INSTDIR\Uninstall.exe"
	RMDir /r "$INSTDIR"

	RMDir /r "$WINDIR\java\trustlib\cello\tablet"

	;Delete "$SYSDIR\jtablet.dll"
	!insertmacro UnInstallLib DLL NOTSHARED NOREBOOT_NOTPROTECTED $SYSDIR\jtablet.dll

	
	RMDir /r "$SMPROGRAMS\Cellosoft\JTablet\"

	DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Cellosoft\JTablet"
	DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\JTablet"

  Delete "$INSTDIR\Uninstall.exe"

  RMDir "$INSTDIR"

  DeleteRegKey /ifempty HKCU "Software\Cellosoft\${PRODUCT_NAME}"

  ;Display the Finish header
  ;!insertmacro MUI_UNFINISHHEADER

SectionEnd

;--------------------------------
;Uninstaller Functions

Function un.onInit

  ;Get language from registry
  ReadRegStr $LANGUAGE HKCU "Software\Cellosoft\${PRODUCT_NAME}" "Installer Language"
  
FunctionEnd
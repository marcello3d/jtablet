
SetCompressor /SOLID lzma

Name "Cellosoft JTablet %%JTABLET.VERSION%%"

# General Symbol Definitions
!define ID_NAME "Cellosoft JTablet 2"
!define REGKEY "SOFTWARE\Cellosoft\JTablet2"
!define VERSION %%JTABLET.VERSION%%
!define COMPANY Cellosoft
!define URL http://jtablet.cellosoft.com/

!define MIN_JAVA_VERSION "1.6"

# MultiUser Symbol Definitions
!define MULTIUSER_EXECUTIONLEVEL Admin
!define MULTIUSER_INSTALLMODE_COMMANDLINE
!define MULTIUSER_INSTALLMODE_INSTDIR "Cellosoft\JTablet 2"
!define MULTIUSER_INSTALLMODE_INSTDIR_REGISTRY_KEY "${REGKEY}"
!define MULTIUSER_INSTALLMODE_INSTDIR_REGISTRY_VALUE "Path"

# MUI Symbol Definitions
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall.ico"
!define MUI_UNFINISHPAGE_NOAUTOCLOSE

# Included files
!include MultiUser.nsh
!include Sections.nsh
!include MUI2.nsh
!include Library.nsh


# Installer text
!define MUI_WELCOMEPAGE_TITLE "$\r$\nCellosoft JTablet"
!define MUI_WELCOMEPAGE_TEXT "You are about to install the Cellosoft JTablet %%JTABLET.VERSION%% Plugin for Java. $\r$\n$\r$\nI'm quite excited!"

!define MUI_FINISHPAGE_TITLE "$\r$\nTa-da!"
!define MUI_FINISHPAGE_TEXT "Cellosoft JTablet 2 is now installed!"
!define MUI_FINISHPAGE_LINK "Visit the JTablet website"
!define MUI_FINISHPAGE_LINK_LOCATION "http://jtablet.cellosoft.com/"

# Installer pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH


# Uninstaller
!define MUI_WELCOMEPAGE_TITLE "$\r$\nCellosoft JTablet"
!define MUI_WELCOMEPAGE_TEXT "You are about to remove Cellosoft JTablet %%JTABLET.VERSION%%. $\r$\n$\r$\nIt's sad to see you go!"
!define MUI_FINISHPAGE_TITLE "$\r$\nOK!"

!define MUI_FINISHPAGE_TEXT "Cellosoft JTablet 2 should now be removed!"
!define MUI_FINISHPAGE_TEXT_REBOOT "Cellosoft JTablet is mostly removed, but some files are still being used by Java. $\r$\n$\r$\nI am afraid you will have to reboot to remove them."
!define MUI_FINISHPAGE_TEXT_REBOOTLATER "I'll reboot later"
!define MUI_FINISHPAGE_LINK "Visit the JTablet website"
!define MUI_FINISHPAGE_LINK_LOCATION "http://jtablet.cellosoft.com/"

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH


# Installer languages
!insertmacro MUI_LANGUAGE English

# Installer attributes
OutFile JTabletSetup.exe
InstallDir "$PROGRAMFILES\Cellosoft\JTablet 2"
CRCCheck on
XPStyle on
ShowInstDetails hide
VIProductVersion 2.0.0.0
VIAddVersionKey ProductName "Cellosoft JTablet"
VIAddVersionKey ProductVersion "${VERSION}"
VIAddVersionKey CompanyName "${COMPANY}"
VIAddVersionKey CompanyWebsite "${URL}"
VIAddVersionKey FileVersion "${VERSION}"
VIAddVersionKey FileDescription ""
VIAddVersionKey LegalCopyright ""
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails hide
RequestExecutionLevel admin


# Installer sections

; Copied from http://nsis.sourceforge.net/New_installer_with_JRE_check_(includes_fixes_from_'Simple_installer_with_JRE_check'_and_missing_jre.ini)
Function DetectJRE
  Exch $0   ; Get version requested  
        ; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1   ; $1 = Java version string (ie 1.5.0)
  Push $2   ; $2 = Javahome
  Push $3   ; $3 and $4 are used for checking the major/minor version of java
  Push $4
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  StrCmp $2 "" DetectTry2
  Goto GetJRE
 
DetectTry2:
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  StrCmp $1 "" NoFound
  DetailPrint "Found JDK $1"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  StrCmp $2 "" NoFound
 
GetJRE:
; $0 = version requested. $1 = version found. $2 = javaHome
  DetailPrint "Checking version of Java..."
  IfFileExists "$2\bin\java.exe" 0 NoFound
  StrCpy $3 $0 1            ; Get major version. Example: $1 = 1.5.0, now $3 = 1
  StrCpy $4 $1 1            ; $3 = major version requested, $4 = major version found
  IntCmp $4 $3 0 FoundOld FoundNew
  StrCpy $3 $0 1 2
  StrCpy $4 $1 1 2          ; Same as above. $3 is minor version requested, $4 is minor version installed
  IntCmp $4 $3 FoundNew FoundOld FoundNew
 
NoFound:
    DetailPrint "JRE not found." 
    Push ""
    Goto DetectJREEnd
 
FoundOld:
    DetailPrint "Found Java $1" 
;  Push ${TEMP2}
    Push "$1"
    Goto DetectJREEnd  
FoundNew:
    DetailPrint "Found Java $1" 
    Push "OK"
    Goto DetectJREEnd
DetectJREEnd:
    ; Top of stack is return value, then r4,r3,r2,r1
    Exch 5  ; => r4,r3,r2,r1,r0,rv
    Pop $4  ; => r3,r2,r1,r0,rv
    Pop $3  ; => r2,r1,r0,rv
    Pop $2  ; => r1,r0,rv
    Pop $1  ; => r0,rv
    Pop $0  ; => rv 
FunctionEnd

Function UninstallPrevious
    ; Check for uninstaller.
    ReadRegStr $R0 HKLM "SOFTWARE\Cellosoft\JTablet" "Install"
    
    ${If} $R0 == ""        
        Goto Done
    ${EndIf}
    DetailPrint "Found old version of JTablet."
    messageBox MB_OKCANCEL "To avoid problems we first need to remove your old version of JTablet." IDOK ok 
        Abort "Installation cancelled by user."
    ok:
        DetailPrint "Removing old JTablet installation..."
        ; Run the uninstaller silently.
        ExecWait '"$R0\Uninstall.exe" /S "_?=$R0"' $0
        
        ${If} $0 == "0"
            DetailPrint "Found old version of JTablet."
            Goto Done
        ${EndIf}
    
        Abort "Failed to uninstall old version of JTablet."
    Done:
FunctionEnd

Function CheckJREVersion 

    Push "${MIN_JAVA_VERSION}"
    Call DetectJRE 
    Pop $0
    ${If} $0 == ""
        Abort "JTablet 2 needs Java ${MIN_JAVA_VERSION}."
    ${EndIf}
    ${If} $0 != "OK"
        Abort "JTablet 2 needs Java ${MIN_JAVA_VERSION}. You have Java $0."
    ${EndIf}
    
FunctionEnd

Section -Main SEC0000
    ; Make sure the user has Java 6 installed ($WINDIR\Sun\Java\lib\ was added in Java 6)
    Call CheckJREVersion
    ; Remove legacy version of JTablet
    Call UninstallPrevious
    
    SetOverwrite on
    
    SetOutPath $WINDIR\Sun\Java\lib\ext
    File InstallFiles\jtablet.jar
    
    SetOutPath $WINDIR\Sun\Java\bin
    File InstallFiles\jtablet2.dll
    File InstallFiles\jtablet2-64.dll

    WriteRegStr HKLM "${REGKEY}\Components" Main 1
SectionEnd

Section -post SEC0001
    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    SetOutPath $INSTDIR
    WriteUninstaller $INSTDIR\Uninstall.exe
    
    ; Adds uninstaller to add/remove programs
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${ID_NAME}" DisplayName "$(^Name)"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${ID_NAME}" DisplayVersion "${VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${ID_NAME}" Publisher "${COMPANY}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${ID_NAME}" URLInfoAbout "${URL}"
;    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${ID_NAME}" DisplayIcon $INSTDIR\Uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${ID_NAME}" UninstallString $INSTDIR\Uninstall.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${ID_NAME}" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${ID_NAME}" NoRepair 1
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
    Push $R0
    ReadRegStr $R0 HKLM "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend

# Uninstaller sections
Section /o -un.Main UNSEC0000
    Delete /REBOOTOK $WINDIR\Sun\Java\lib\ext\jtablet.jar
    Delete /REBOOTOK $WINDIR\Sun\Java\bin\jtablet2.dll
    Delete /REBOOTOK $WINDIR\Sun\Java\bin\jtablet2-64.dll
    DeleteRegValue HKLM "${REGKEY}\Components" Main
SectionEnd

Section -un.post UNSEC0001
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${ID_NAME}"
    Delete /REBOOTOK $INSTDIR\Uninstall.exe
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    RmDir /REBOOTOK $INSTDIR
SectionEnd

# Installer functions
Function .onInit
    InitPluginsDir
    !insertmacro MULTIUSER_INIT
FunctionEnd

# Uninstaller functions
Function un.onInit
    !insertmacro MULTIUSER_UNINIT
    !insertmacro SELECT_UNSECTION Main ${UNSEC0000}
FunctionEnd


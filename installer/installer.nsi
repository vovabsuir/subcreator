!include "MUI2.nsh"
!include "LogicLib.nsh"
!include "FileFunc.nsh"
!include "WinCore.nsh"
!include "nsDialogs.nsh"

Name "SubCreator"
OutFile "SubCreatorSetup.exe"
InstallDir "$PROGRAMFILES\SubCreator"
ShowInstDetails show
RequestExecutionLevel admin

!define MUI_ICON "icon.ico"
!define MUI_WELCOMEPAGE_TITLE "SubCreator Setup"
!define MUI_WELCOMEPAGE_TEXT "This wizard will guide you through the installation of SubCreator.$\r$\n$\r$\nClick Next to continue."

!define MUI_FINISHPAGE_TITLE "Installation Complete"
!define MUI_FINISHPAGE_TEXT "SubCreator has been successfully installed on your computer.$\r$\n$\r$\nClick Finish to close this wizard."

!define MUI_ABORTWARNING

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
Page custom ApiKeyPage ApiKeyPageLeave
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

Var ApiKeyInput
Var ApiKey

Function ApiKeyPage
  nsDialogs::Create 1018
  Pop $0
  
  ${NSD_CreateLabel} 0 0 100% 24u "AssemblyAI API key is required for the application to work.$\n$\nEnter your AssemblyAI API key:"
  Pop $0
  
  ${NSD_CreateText} 0 30u 100% 12u ""
  Pop $ApiKeyInput
  
  nsDialogs::Show
FunctionEnd

Function ApiKeyPageLeave
  ${NSD_GetText} $ApiKeyInput $ApiKey
  ${If} $ApiKey == ""
    MessageBox MB_ICONEXCLAMATION|MB_OK "API key cannot be empty!"
    Abort
  ${EndIf}
  WriteRegExpandStr HKCU "Environment" "ASSEMBLY_AI_API_KEY" "$ApiKey"
  SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment"
FunctionEnd

Section "Main Section" SEC01
  SetOutPath "$INSTDIR"

  File "SubCreator.jar"
  File "icon.ico"

  SetOutPath "$INSTDIR\jre"
  File /r "jre\*.*"

  SetOutPath "$INSTDIR\ffmpeg"
  File /r "ffmpeg\*.*"

  CreateDirectory "$SMPROGRAMS\SubCreator"
  CreateShortCut "$SMPROGRAMS\SubCreator\SubCreator.lnk" "$INSTDIR\jre\bin\javaw.exe" \
    '-jar "$INSTDIR\SubCreator.jar"' "$INSTDIR\icon.ico" 0
  CreateShortCut "$SMPROGRAMS\SubCreator\Uninstall.lnk" "$INSTDIR\uninstall.exe"

  MessageBox MB_YESNO "Would you like to create a desktop shortcut?" IDNO noDesktopShortcut
  CreateShortCut "$DESKTOP\SubCreator.lnk" "$INSTDIR\jre\bin\javaw.exe" \
      '-jar "$INSTDIR\SubCreator.jar"' "$INSTDIR\icon.ico" 0
  noDesktopShortcut:

  WriteUninstaller "$INSTDIR\uninstall.exe"
  
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SubCreator" \
                   "DisplayName" "SubCreator"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SubCreator" \
                   "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SubCreator" \
                   "DisplayIcon" '"$INSTDIR\icon.ico"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SubCreator" \
                   "Publisher" "vovabsuir"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SubCreator" \
                   "DisplayVersion" "1.0.0"

  WriteRegExpandStr HKCU "Environment" "FFMPEG_PATH" "$INSTDIR\ffmpeg\bin"
  SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment"
SectionEnd

Section "Uninstall"
  Delete "$DESKTOP\SubCreator.lnk"
  RMDir /r "$SMPROGRAMS\SubCreator"

  Delete "$INSTDIR\SubCreator.jar"
  Delete "$INSTDIR\icon.ico"
  Delete "$INSTDIR\uninstall.exe"
  RMDir /r "$INSTDIR\jre"
  RMDIR /r "$INSTDIR\ffmpeg"
  RMDir /r "$INSTDIR"
  
  DeleteRegValue HKCU "Environment" "ASSEMBLY_AI_API_KEY"
  DeleteRegValue HKCU "Environment" "FFMPEG_PATH"
  SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment"
  
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SubCreator"
SectionEnd
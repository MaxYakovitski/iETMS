@echo off
setlocal

REM Native host launcher for iETMS
REM Installed under Program Files\iETMS\native-host

set APP_HOME=%~dp0
set JAVA_EXE="%APP_HOME%\runtime\bin\java.exe"

"%JAVA_EXE%" -jar "%APP_HOME%\iet-native-host.jar"
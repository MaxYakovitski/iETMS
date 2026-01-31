@echo off
setlocal

set APP_HOME=%~dp0

"%APP_HOME%\runtime\bin\java.exe" ^
  -Xrs ^
  -Djava.awt.headless=true ^
  -Dfile.encoding=UTF-8 ^
  -jar "%APP_HOME%\iet-native-host.jar"
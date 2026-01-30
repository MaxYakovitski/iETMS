@echo off
setlocal

set APP_HOME=%~dp0

java -Xrs -Djava.awt.headless=true ^
  -jar "%APP_HOME%\iet-native-host.jar"
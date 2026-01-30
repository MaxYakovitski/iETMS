@echo off

reg add ^
 "HKCU\Software\Google\Chrome\NativeMessagingHosts\com.mayak.iet.nativehost" ^
 /ve /t REG_SZ ^
 /d "C:\Program Files\iETMS\app\native-host\com.mayak.iet.nativehost.json" ^
 /f
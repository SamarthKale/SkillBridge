@echo off
set ADB_PATH="C:\Users\Aaron Fernandes\AppData\Local\Android\Sdk\platform-tools\adb.exe"

echo 1. Pulling database from emulator...
%ADB_PATH% exec-out "run-as com.skillbridge.app cat databases/SkillBridgeLocal.db" > SkillBridgeLocal.db

echo 2. Database saved to your project folder as 'SkillBridgeLocal.db'
echo You can open this file using "Database Inspector" in Android Studio!

@echo off
xcopy L:\Doc\SVN\Work\BltRfidDemo\trunk\src\BltRfidDemo\app\src\main L:\Doc\Git\AppInvTestBluetooth\app\src\main\ /S
xcopy L:\Doc\SVN\Work\BltRfidDemo\trunk\src\BltRfidDemo\app\libs L:\Doc\Git\AppInvTestBluetooth\app\libs\ /S
copy L:\Doc\SVN\Work\BltRfidDemo\trunk\src\BltRfidDemo\app\build.gradle L:\Doc\Git\AppInvTestBluetooth\app
pause

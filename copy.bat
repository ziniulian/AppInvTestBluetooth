@echo off
xcopy L:\Doc\SVN\Work\TestBluetoothDemo\trunk\src\TestBluetoothDemo\app\src\main L:\Doc\Git\AppInvTestBluetooth\app\src\main\ /S
xcopy L:\Doc\SVN\Work\TestBluetoothDemo\trunk\src\TestBluetoothDemo\app\libs L:\Doc\Git\AppInvTestBluetooth\app\libs\ /S
copy L:\Doc\SVN\Work\TestBluetoothDemo\trunk\src\TestBluetoothDemo\app\build.gradle L:\Doc\Git\AppInvTestBluetooth\app
pause

@echo off
:: 公共库
xcopy L:\Doc\SVN\Work\TestBluetoothDemo\trunk\src\TestBluetoothDemo\libs L:\Doc\Git\AppInvTestBluetooth\libs\ /S

:: 我的新项目
xcopy L:\Doc\SVN\Work\TestBluetoothDemo\trunk\src\TestBluetoothDemo\appTest\src\main L:\Doc\Git\AppInvTestBluetooth\appTest\src\main\ /S
copy L:\Doc\SVN\Work\TestBluetoothDemo\trunk\src\TestBluetoothDemo\appTest\build.gradle L:\Doc\Git\AppInvTestBluetooth\appTest

:: 赖学良的旧项目
xcopy L:\Doc\SVN\Work\TestBluetoothDemo\trunk\src\TestBluetoothDemo\app\src\main L:\Doc\Git\AppInvTestBluetooth\app\src\main\ /S
copy L:\Doc\SVN\Work\TestBluetoothDemo\trunk\src\TestBluetoothDemo\app\build.gradle L:\Doc\Git\AppInvTestBluetooth\app
pause

@echo off

set "WILDFLY_HOME=wildfly-10.1.0.Final"
set "BOUNDLE_TEST=Laurentius-test"
set "FOLDER_DEPLOY=test-deploy"
set "BOUNDLE_NAME=Laurentius-test"

echo Clear wildfly folder.
RMDIR "%FOLDER_DEPLOY%\%WILDFLY_HOME%" /S /Q
echo Unzip wildfly.
"C:\Program Files\7-Zip\7z.exe" x  "%WILDFLY_HOME%.zip"  -o%FOLDER_DEPLOY% > nul



echo Change dir to %FOLDER_DEPLOY%\%BOUNDLE_NAME%/widlfly-*
cd "%FOLDER_DEPLOY%\%BOUNDLE_NAME%/widlfly-10.1"

echo Run deployment script deploy-laurentius.bat
call deploy-laurentius.bat --init -w ..\..\%WILDFLY_HOME%

echo Change dir to "%WILDFLY_HOME%\bin."
cd "%WILDFLY_HOME%\bin"

echo "Run laurentius."
call laurentius-demo.bat --init -d test-laurentius.org







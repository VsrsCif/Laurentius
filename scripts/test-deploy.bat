@echo off
set "WILDFLY_HOME=wildfly-10.1.0.Final"
set "BOUNDLE_TEST=Laurentius-test"
set "FOLDER_DEPLOY=test-deploy"
set "BOUNDLE_NAME=Laurentius-test"
set "JAVA_HOME=C:\Program Files (x86)\Java\jre1.8.0_20"
set "SECMGR=true"


rem set "PATH=%PATH%;C:\Program Files (x86)\Java\jdk1.7.0_67\bin"
rem set "JAVA_HOME=C:\Program Files (x86)\Java\jdk1.7.0_67\"

echo Clear wildfly folder.
RMDIR "%FOLDER_DEPLOY%\%WILDFLY_HOME%" /S /Q
echo Unzip wildfly.
rem "C:\Program Files (X86)\7-Zip\7z.exe" x  "%WILDFLY_HOME%.zip"  -o%FOLDER_DEPLOY% > nul
"C:\Program Files (X86)\7-Zip\7z.exe" x  "..\..\settings\%WILDFLY_HOME%.zip"  -o%FOLDER_DEPLOY% > nul



echo Change dir to %FOLDER_DEPLOY%\%BOUNDLE_NAME%/wildfly-*
cd "%FOLDER_DEPLOY%\%BOUNDLE_NAME%/wildfly-10.1"

echo Run deployment script deploy-laurentius.bat
call deploy-laurentius.bat --init -w ..\..\%WILDFLY_HOME%

echo Change dir to "%WILDFLY_HOME%/bin"
cd ..\..\wildfly-10.1.0.Final\bin

echo "Run laurentius."
call laurentius-demo.bat --init -d test-laurentius.org


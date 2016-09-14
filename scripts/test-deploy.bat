@echo off

set "WILDFLY_HOME=wildfly-10.1.0.Final"
set "BOUNDLE_TEST=Laurentius-test"
set "FOLDER_DEPLOY=test-deploy"
set "BOUNDLE_NAME=Laurentius-test"



"C:\Program Files\7-Zip\7z.exe" x  "%LAU_BUNDLE%.zip"  -o%FOLDER_DEPLOY%




cd "%FOLDER_DEPLOY%\%BOUNDLE_NAME%/widlfly-10.1"

deploy-laurentius.bat --init -w "..\..\%WILDFLY_HOME%"

cd "..\..\%WILDFLY_HOME%\bin"

laurentius-demo.bat --init -d test-laurentius.org







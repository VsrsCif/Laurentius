@echo off
@if not "%ECHO%" == ""  echo %ECHO%

set "WILDFLY_HOME="
set "LAU_BUNDLE="
set "LAU_HOME="
set "INIT=false"

:loop
      if ["%~1"]==[""] (
        echo done.
        goto endParamReading
      )
	  
	  if ["%~1"]==["--init"] (
        set "INIT=true"
      )
	  
	  if ["%~1"]==["-w"] (
	    
		set "WILDFLY_HOME=%~2"
		echo WILDFLY_HOME = "%WILDFLY_HOME%".
      )
	  
	  if ["%~1"]==["-b"] (
	    shift
		set "LAU_BUNDLE=%~2"
		echo LAU_BUNDLE = "%LAU_BUNDLE%".
      )
	  
	  if ["%~1"]==["-l"] (
	    shift
		set "LAU_HOME=%~2"
		echo LAU_HOME = "%LAU_HOME%".
      )	  
      ::--------------------------
      shift
      goto loop
:endParamReading


pushd "%CD%\.."
	set "RESOLVED_LAU_BUNDLE=%CD%"
popd


if "x%WILDFLY_HOME%" == "x" (
  echo ERROR: WILDFLY_HOME folder is not setted!
  goto :quit
)

if "x%LAU_BUNDLE%" == "x" (
	set "LAU_BUNDLE=%RESOLVED_LAU_BUNDLE%"
)

if "x%LAU_HOME%" == "x" (
  set  "LAU_HOME=%WILDFLY_HOME%\standalone\data\"
)


echo *******************************.
echo WILDFLY_HOME = "%WILDFLY_HOME%".
echo LAU_BUNDLE = "%LAU_BUNDLE%".
echo LAU_HOME = "%LAU_HOME%".
echo INIT = "%INIT%".




rem  create module folder
if not exist %WILDFLY_HOME%\modules\si\laurentius\main\ (
	md  "%WILDFLY_HOME%\modules\si\laurentius\main\"
)
	  

rem  copy module libraries
copy "%LAU_BUNDLE%\modules\Laurentius-msh-xsd-1.0.jar" "%WILDFLY_HOME%\modules\si\laurentius\main\"
copy "%LAU_BUNDLE%\modules\Laurentius-wsdl-1.0.jar" "%WILDFLY_HOME%\modules\si\laurentius\main\"
copy "%LAU_BUNDLE%\modules\Laurentius-commons-1.0.jar" "%WILDFLY_HOME%\modules\si\laurentius\main\"
copy "%LAU_BUNDLE%\modules\Laurentius-lce-1.0.jar" "%WILDFLY_HOME%\modules\si\laurentius\main\"
copy "%LAU_BUNDLE%\modules\Laurentius-plugin-interfaces-1.0.jar" "%WILDFLY_HOME%\modules\si\laurentius\main\"


rem  copy module descriptor
copy "%LAU_BUNDLE%\modules\si.laurentius.module.xml" "%WILDFLY_HOME%\modules\si\laurentius\main\module.xml"
if not exist "%WILDFLY_HOME%\modules\org\" (
	md "%WILDFLY_HOME%\modules\org"
)
xcopy "%LAU_BUNDLE%\modules\org" "%WILDFLY_HOME%\modules\org" /S /E

rem  deploy commons ejbs
copy "%LAU_BUNDLE%\deployments\Laurentius-dao.jar"  "%WILDFLY_HOME%\standalone\deployments\"
copy "%LAU_BUNDLE%\deployments\Laurentius-basic-tasks.jar"  "%WILDFLY_HOME%\standalone\deployments\"
rem  deploy modules 
copy "%LAU_BUNDLE%\deployments\Laurentius-msh.ear"  "%WILDFLY_HOME%\standalone\deployments\"
copy "%LAU_BUNDLE%\deployments\laurentius-ws.war"  "%WILDFLY_HOME%\standalone\deployments\"
copy "%LAU_BUNDLE%\deployments\laurentius-web.war"  "%WILDFLY_HOME%\standalone\deployments\"
copy "%LAU_BUNDLE%\deployments\plugin-zpp.war"  "%WILDFLY_HOME%\standalone\deployments\"
copy "%LAU_BUNDLE%\deployments\plugin-testcase.war"  "%WILDFLY_HOME%\standalone\deployments\"






if "%INIT%" == "true" (
	rem  set fix for module org.apache.ws.security
	copy "%LAU_BUNDLE%\modules\org.apache.ws.securitymodule.xml" "%WILDFLY_HOME%\modules\system\layers\base\org\apache\ws\security\main\module.xml"
	echo copy configuration to "%WILDFLY_HOME%\standalone\configuration\".
	rem  copy configuration
	copy "%LAU_BUNDLE%\wildfly-10.1\config\laurentius-roles.properties" "%WILDFLY_HOME%\standalone\configuration\"
	copy "%LAU_BUNDLE%\wildfly-10.1\config\laurentius-users.properties" "%WILDFLY_HOME%\standalone\configuration\"
	copy "%LAU_BUNDLE%\wildfly-10.1\config\standalone-laurentius.xml" "%WILDFLY_HOME%\standalone\configuration\"
	copy "%LAU_BUNDLE%\wildfly-10.1\config\test-tls-keystore.jks" "%WILDFLY_HOME%\standalone\configuration\"

	rem  copy start scripts
	echo copy start scripts "%WILDFLY_HOME%\bin\
	copy "%LAU_BUNDLE%\wildfly-10.1\laurentius-demo.bat" "%WILDFLY_HOME%\bin\"
	move "%WILDFLY_HOME%\bin\standalone.conf.bat" "%WILDFLY_HOME%\bin\standalone.conf.bat.bck"
	copy "%LAU_BUNDLE%\wildfly-10.1\config\standalone.conf.bat" "%WILDFLY_HOME%\bin\standalone.conf.bat"

	echo "copy laurentius-home to %LAU_HOME%\laurentius-home".
	rem  create home folder
	md "%LAU_HOME%\laurentius-home"
	xcopy "%LAU_BUNDLE%\laurentius-home" "%LAU_HOME%\laurentius-home" /S /E
)
goto :END


:quit
echo.
echo Usage:
echo deploy-led.bat --init -b [LAU_BUNDLE] -w [WILDFLY_HOME] -l [LAU_HOME]
echo.
echo   --init  initialize laurentius.home and wildfly properties. 
echo   -w   WILDFLY_HOME -  path jboss home: ex.: c:\temp\wildfly-10.1.0.Final\.
echo   -b   LAU_BUNDLE   - path to unziped Laurentius bundle if not given parent script folder is setted.
echo   -l   LAU_HOME     - path tom application home folder  (laurentius.home) if is not given and --init is setted than '[WILDFLY_HOME]\standalone\data\' is setted.	


:END
pause

@echo off
@if not "%ECHO%" == ""  echo %ECHO%

set "SERVER_HOME="
set "LAU_BUNDLE="
set "LAU_HOME="
set "INIT=false"
set "APPL_SERVER=jboss-eap-7.0"

:loop
      if ["%~1"]==[""] (
        echo done.
        goto endParamReading
      )
	  
	  if ["%~1"]==["--init"] (
        set "INIT=true"
      )
	  
	  if ["%~1"]==["-s"] (
	    
		set "SERVER_HOME=%~2"
		echo SERVER_HOME = "%SERVER_HOME%".
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


if "x%SERVER_HOME%" == "x" (
  echo ERROR: SERVER_HOME folder is not setted!
  goto :quit
)

if "x%LAU_BUNDLE%" == "x" (
	set "LAU_BUNDLE=%RESOLVED_LAU_BUNDLE%"
)

if "x%LAU_HOME%" == "x" (
  set  "LAU_HOME=%SERVER_HOME%\standalone\data\"
)


echo *******************************.
echo SERVER_HOME = "%SERVER_HOME%".
echo LAU_BUNDLE = "%LAU_BUNDLE%".
echo LAU_HOME = "%LAU_HOME%".
echo INIT = "%INIT%".


rem -------------------------------------------------------------------------------
rem copy library modules
md "%SERVER_HOME%\modules\si"
xcopy "%LAU_BUNDLE%\modules\si" "%SERVER_HOME%\modules\si" /e




rem -------------------------------------------------------------------------------
rem application modules
copy "%LAU_BUNDLE%\deployments\Laurentius-dao.jar"  "%SERVER_HOME%\standalone\deployments\"
copy "%LAU_BUNDLE%\deployments\Laurentius-msh.ear"  "%SERVER_HOME%\standalone\deployments\"
copy "%LAU_BUNDLE%\deployments\laurentius-ws.war"  "%SERVER_HOME%\standalone\deployments\"
copy "%LAU_BUNDLE%\deployments\laurentius-web.war"  "%SERVER_HOME%\standalone\deployments\"

rem  application plugins
copy "%LAU_BUNDLE%\deployments\plugin-zpp.war"  "%SERVER_HOME%\standalone\deployments\"
copy "%LAU_BUNDLE%\deployments\plugin-basic.war"  "%SERVER_HOME%\standalone\deployments\"
copy "%LAU_BUNDLE%\deployments\plugin-testcase.war"  "%SERVER_HOME%\standalone\deployments\"
#copy "%LAU_BUNDLE%\deployments\plugin-court-filing.jar"  "%SERVER_HOME%\standalone\deployments\"



if "%INIT%" == "true" (

	rem copy module fix
	if exist "%LAU_BUNDLE%\%APPL_SERVER%\modules\org.apache.ws.security.module.xml" (
		copy "%LAU_BUNDLE%\%APPL_SERVER%\modules\org.apache.ws.security.module.xml" "%SERVER_HOME%\modules\system\layers\base\org\apache\ws\security\main\module.xml"
	)


	copy "%LAU_BUNDLE%\%APPL_SERVER%\modules\si.laurentius.module.xml" "%SERVER_HOME%\modules\si\laurentius\main\module.xml"

	rem copy 'org' library modules (primefaces, pdfbox)
	md "%SERVER_HOME%\modules\org"
	xcopy "%LAU_BUNDLE%\modules\org" "%SERVER_HOME%\modules\org" /e
	

	rem copy start scripts
	copy "%LAU_BUNDLE%\%APPL_SERVER%\laurentius-demo.bat" "%SERVER_HOME%\bin\"

	rem  create home folder
	md "%LAU_HOME%\laurentius-home"
	xcopy "%LAU_BUNDLE%\laurentius-home" "%LAU_HOME%\laurentius-home" /S /E

	rem  copy configuration
	copy "%LAU_BUNDLE%\%APPL_SERVER%\config\laurentius-roles.properties" "%SERVER_HOME%\standalone\configuration\"
	copy "%LAU_BUNDLE%\%APPL_SERVER%\config\laurentius-users.properties" "%SERVER_HOME%\standalone\configuration\"
	copy "%LAU_BUNDLE%\%APPL_SERVER%\config\standalone-laurentius.xml" "%SERVER_HOME%\standalone\configuration\"
	copy "%LAU_BUNDLE%\%APPL_SERVER%\config\test-tls-keystore.jks" "%SERVER_HOME%\standalone\configuration\"	
	
)
goto :END


:quit
echo.
echo Usage:
echo deploy-laurentius.bat --init -b [LAU_BUNDLE] -s [SERVER_HOME] -l [LAU_HOME]
echo.
echo   --init  initialize laurentius.home and wildfly properties. 
echo   -s   SERVER_HOME -  path jboss home: ex.: c:\temp\wildfly-10.1.0.Final\.
echo   -b   LAU_BUNDLE   - path to unziped Laurentius bundle if not given parent script folder is setted.
echo   -l   LAU_HOME     - path tom application home folder  (laurentius.home) if is not given and --init is setted than '[SERVER_HOME]\standalone\data\' is setted.	


:END
pause

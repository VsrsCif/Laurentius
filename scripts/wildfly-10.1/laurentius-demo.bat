@echo off

set "WILDFLY_HOME="
set "RESOLVED_WILDFLY_HOME="
set "LAU_HOME="
set "INIT=false"
set "LAU_DOMAIN="
set "LAU_OPTS=-c standalone-laurentius.xml"

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
	    
      if ["%~1"]==["-l"] (
	    shift
		set "LAU_HOME=%~2"
		echo LAU_HOME = "%LAU_HOME%".
      )	  
	   if ["%~1"]==["-d"] (
	    shift
		set "LAU_DOMAIN=%~2"
		echo LAU_DOMAIN = "%LAU_DOMAIN%".
      )	  
      ::--------------------------
      shift
      goto loop
:endParamReading

pushd "%CD%\.."
	set "RESOLVED_WILDFLY_HOME=%CD%"
popd


if "x%WILDFLY_HOME%" == "x" (
		set "WILDFLY_HOME=%RESOLVED_WILDFLY_HOME%"

)

if "x%LAU_HOME%" == "x" (
  set  "LAU_HOME=%WILDFLY_HOME%\standalone\data\laurentius-home"
)

set "LAU_OPTS=%LAU_OPTS% -Dlaurentius.home=%LAU_HOME%"



if "%INIT%" == "true" (
	
	set "LAU_OPTS=%LAU_OPTS% -Dsi.laurentius.msh.hibernate.hbm2ddl.auto=create -Dsi.laurentius.msh.hibernate.dialect=org.hibernate.dialect.H2Dialect -Dsi.laurentius.init.lookups=%LAU_HOME%\init-data.xml -Dsi.laurentius.domain=%LAU_DOMAIN%"
)


echo *********************************************************************************************************************************
echo * WILDFLY_HOME =  "%WILDFLY_HOME%"
echo * LAU_HOME     =  "%LAU_HOME%"
echo * INIT         =  "%INIT%"
echo * DOMAIN       =  "%LAU_DOMAIN%"
echo * LAU_OPTS     =  "%LAU_OPTS%"
echo *********************************************************************************************************************************

%WILDFLY_HOME%\bin\standalone.bat %LAU_OPTS% -b 0.0.0.0


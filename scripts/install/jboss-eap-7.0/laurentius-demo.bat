@echo off



rem Set database dialect
rem choose appropriate from page 
rem https://docs.jboss.org/hibernate/orm/5.2/javadocs/org/hibernate/dialect/package-summary.html
rem examples:
rem set "DB_DIALECT=org.hibernate.dialect.H2Dialect"
rem set "DB_DIALECT=org.hibernate.dialect.Oracle10gDialect"
rem set "DB_DIALECT=org.hibernate.dialect.Oracle12cDialect"
rem set "DB_DIALECT=org.hibernate.dialect.PostgreSQL9Dialect"
rem set "DB_DIALECT=org.hibernate.dialect.PostgreSQL95Dialect"
rem set "DB_DIALECT=org.hibernate.dialect.SQLServer2008Dialect"
set "DB_DIALECT=org.hibernate.dialect.H2Dialect"

rem set db action where init parameter is true.
rem only validate (validate), 'update' or recreate (create) database objects.
rem recreate  - delete all data in a tables.
rem https://docs.jboss.org/hibernate/orm/5.0/manual/en-US/html/ch03.html

rem "DB_INIT_ACTION=validate"
set "DB_INIT_ACTION=create"
rem "DB_INIT_ACTION=update"


rem inet mask for access 0.0.0.0 - all access
set "LISTEN_MASK=0.0.0.0"



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
	
	set "LAU_OPTS=%LAU_OPTS% -Dlaurentius.hibernate.hbm2ddl.auto=%DB_INIT_ACTION% -Dlaurentius.hibernate.dialect=%DB_DIALECT% -Dlaurentius.init=true  -Dlaurentius.domain=%LAU_DOMAIN%"
)


echo *********************************************************************************************************************************
echo * WILDFLY_HOME =  "%WILDFLY_HOME%"
echo * LAU_HOME     =  "%LAU_HOME%"
echo * INIT         =  "%INIT%"
echo * DOMAIN       =  "%LAU_DOMAIN%"
echo * LAU_OPTS     =  "%LAU_OPTS%"
echo *********************************************************************************************************************************

%WILDFLY_HOME%\bin\standalone.bat %LAU_OPTS% -b %LISTEN_MASK%


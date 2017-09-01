@echo off
@if not "%ECHO%" == ""  echo %ECHO%

set ExitCode=0
set "FileName=script.log"
set "LogFile=%~dp0%FileName%"
(

echo -------------------- test start ----------------- " 
echo params: %* 
echo exit code: %ExitCode%
echo -------------------- test end ----------------- 
) > %LogFile%

EXIT /B %ExitCode%
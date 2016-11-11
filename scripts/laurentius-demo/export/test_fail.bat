@echo off
@if not "%ECHO%" == ""  echo %ECHO%

set ExitCode=1
set "FileName=script.log"
set "LogFile=%~dp0%FileName%"
(

echo -------------------- test start Fail script ----------------- " 
echo params: %* 
echo exit code: %ExitCode%
echo -------------------- test start Fail script ----------------- 
) > %LogFile%

EXIT /B %ExitCode%
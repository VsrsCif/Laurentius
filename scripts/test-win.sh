#!/bin/bash
export JAVA_HOME="C:\Program Files (x86)\Java\jdk1.8.0_20"
echo "JAVA_HOME=$JAVA_HOME"
export WINEDEBUG=1
#wine cmd test-deploy.bat
wineconsole test-deploy.bat

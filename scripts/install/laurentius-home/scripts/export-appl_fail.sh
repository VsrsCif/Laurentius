#!/bin/sh

ExitCode=1
LOG_FILE="`dirname $0`/script.log"

echo "-------------------- test start ----------------- " >> $LOG_FILE ;
echo "params: $@" >> $LOG_FILE
echo "exit code: $ExitCode" >> $LOG_FILE
echo "-------------------- test end ----------------- " >> $LOG_FILE ;

exit $ExitCode;


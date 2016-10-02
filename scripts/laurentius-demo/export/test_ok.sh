#!/bin/sh



LOG_FILE="`dirname $0`/script.log"

echo "-------------------- test start ----------------- " >> $LOG_FILE ;
echo "params: $@" >> $LOG_FILE
echo "-------------------- test end ----------------- " >> $LOG_FILE ;

return 0;


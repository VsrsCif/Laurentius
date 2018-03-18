#!/bin/sh

# inet mask for access 0.0.0.0 - all access
LISTEN_MASK=0.0.0.0

quit () {
	echo "\nUsage:\n"
	echo "laurentius-demo.sh -l [LAU_HOME]\n"
	echo "  -s WILDFLY_HOME   - server home: default value: '../?"
	echo "  -l LAU_HOME  - path tom application home folder  (laurentius.home) if is not given and --init is setted than '[WILDFLY_HOME]\standalone\data\' is setted."	
        exit
}


while [ "$#" -gt 0 ]
do
key="$1"

  case $key in
    -s|--server)
      WILDFLY_HOME="$2"
      shift # past argument
    ;;
      -l|--laurentius.home)
      LAU_HOME="$2"
      shift # past argument
    ;;
   
   

    *)
      # unknown option
  echo "unknown option: $key or bad list of arguments"
    ;;
  esac
  shift # past argument or value
done



DIRNAME=`dirname "$0"`
RESOLVED_WILDFLY_HOME=`cd "$DIRNAME/.." >/dev/null; pwd`
if [ "x$WILDFLY_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    WILDFLY_HOME=$RESOLVED_WILDFLY_HOME
else
 SANITIZED_WILDFLY_HOME=`cd "$WILDFLY_HOME"; pwd`
 if [ "$RESOLVED_WILDFLY_HOME" != "$SANITIZED_WILDFLY_HOME" ]; then
   echo ""
   echo "   WARNING:  WILDFLY_HOME may be pointing to a different installation - unpredictable results may occur."
   echo ""
   echo "             WILDFLY_HOME: $WILDFLY_HOME"
   echo ""
   sleep 2s
 fi
fi


if [ "x$WILDFLY_HOME" = "x" ]; then
	echo "WILDFLY_HOME folder not defined! Check parameters!"
	quit;
fi

if [ ! -d "$WILDFLY_HOME" ]; then
	echo "WILDFLY_HOME folder not exist! Check parameters!"
	quit;
fi

if [ "x$LAU_HOME" = "x" ]; then
	LAU_HOME="$WILDFLY_HOME/standalone/data/laurentius-home";
fi

LAU_OPTS=" -c standalone-laurentius.xml -Dlaurentius.home=$LAU_HOME/";


echo "*********************************************************************************************************************************"
echo "* WILDFLY_HOME =  $WILDFLY_HOME"
echo "* LAU_HOME     =  $LAU_HOME"
echo "* LAU_OPTS     =  $LAU_OPTS"
echo "*********************************************************************************************************************************"


$WILDFLY_HOME/bin/standalone.sh $LAU_OPTS -b $LISTEN_MASK





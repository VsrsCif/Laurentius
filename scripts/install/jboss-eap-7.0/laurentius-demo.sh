#!/bin/sh

# Method starts wildfly server


# inet mask for access 0.0.0.0 - all access
LISTEN_MASK=0.0.0.0

quit () {
	echo "\nUsage:\n"
	echo "laurentius-demo.sh -l [LAU_HOME]\n"
	echo "  -s SERVER_HOME   - server home: default value: '../?"
	echo "  -l LAU_HOME  - path tom application home folder  (laurentius.home) if is not given and --init is setted than '[SERVER_HOME]\standalone\data\' is setted."	
        exit
}


while [ "$#" -gt 0 ]
do
key="$1"

  case $key in
    -s|--server.home)
      SERVER_HOME="$2"
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
RESOLVED_SERVER_HOME=`cd "$DIRNAME/.." >/dev/null; pwd`
if [ "x$SERVER_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    SERVER_HOME=$RESOLVED_SERVER_HOME
else
 SANITIZED_SERVER_HOME=`cd "$SERVER_HOME"; pwd`
 if [ "$RESOLVED_SERVER_HOME" != "$SANITIZED_SERVER_HOME" ]; then
   echo ""
   echo "   WARNING:  SERVER_HOME may be pointing to a different installation - unpredictable results may occur."
   echo ""
   echo "             SERVER_HOME: $SERVER_HOME"
   echo ""
   sleep 2s
 fi
fi


if [ "x$SERVER_HOME" = "x" ]; then
	echo "SERVER_HOME folder not defined! Check parameters!"
	quit;
fi

if [ ! -d "$SERVER_HOME" ]; then
	echo "SERVER_HOME folder not exist! Check parameters!"
	quit;
fi

if [ "x$LAU_HOME" = "x" ]; then
	LAU_HOME="$SERVER_HOME/standalone/data/laurentius-home";
fi

LAU_OPTS=" -c standalone-laurentius.xml -Dlaurentius.home=$LAU_HOME/";


echo "*********************************************************************************************************************************"
echo "* SERVER_HOME =  $SERVER_HOME"
echo "* LAU_HOME     =  $LAU_HOME"
echo "* LAU_OPTS     =  $LAU_OPTS"
echo "*********************************************************************************************************************************"

#org.hibernate.dialect.H2Dialect
$SERVER_HOME/bin/standalone.sh $LAU_OPTS -b $LISTEN_MASK





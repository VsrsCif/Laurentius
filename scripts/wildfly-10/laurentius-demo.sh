#!/bin/sh


quit () {
	echo "\nUsage:\n"
	echo "start-sed.bat --init -s [LAU_HOME]\n"
	echo "  --init  initialize laurentius.home and wildfly properties. "
	echo "  -s   LAU_HOME     - path tom application home folder  (laurentius.home) if is not given and --init is setted than '[WILDFLY_HOME]\standalone\data\' is setted.	"
        exit
}


while [ "$#" -gt 0 ]
do
key="$1"

  case $key in
    -w|--wildfly)
      WILDFLY_HOME="$2"
      shift # past argument
    ;;
      -s|--laurentius.home)
      LAU_HOME="$2"
      shift # past argument
    ;;
    --init)
      INIT="TRUE"
    ;;
    *)
      # unknown option
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
	quit;
fi

if [ ! -d "$WILDFLY_HOME" ]; then
	echo "WILDFLY_HOME folder not exists! Check parameters!"
	quit;
fi

if [ "x$LAU_HOME" = "x" ]; then
	LAU_HOME="$WILDFLY_HOME/standalone/data/laurentius.home";
fi

LAU_OPTS=" -c standalone-ebms.xml -Dlaurentius.home=$LAU_HOME/";

if [ "$INIT" = "TRUE" ]; then
	LAU_OPTS="$LAU_OPTS -Dorg.sed.msh.hibernate.hbm2ddl.auto=create -Dorg.sed.msh.hibernate.dialect=org.hibernate.dialect.H2Dialect -Dorg.sed.init.lookups=$LAU_HOME/sed-settings.xml";
fi

echo "*********************************************************************************************************************************"
echo "* WILDFLY_HOME =  $WILDFLY_HOME"
echo "* LAU_HOME     =  $LAU_HOME"
echo "* INIT         =  $INIT"
echo "* LAU_OPTS     =  $LAU_OPTS"
echo "*********************************************************************************************************************************"

#org.hibernate.dialect.H2Dialect
$WILDFLY_HOME/bin/standalone.sh $LAU_OPTS





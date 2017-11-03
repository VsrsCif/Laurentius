#!/bin/sh

# set fixed parameters

#WILDFLY_HOME=
#LAU_BUNDLE=
#LAU_HOME=
#INIT=
APPL_SERVER="wildfly-11.0"

while [ "$#" -gt 0 ]
do
key="$1"

  case $key in
    -w|--wildfly)
      WILDFLY_HOME="$2"
      shift # past argument
    ;;
      -l|--laurentius.home)
      LAU_HOME="$2"
      shift # past argument
    ;;
      -b|--bundle)
      LAU_BUNDLE="$2"
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


if [ "x$WILDFLY_HOME" = "x" ]; then
	WILDFLY_HOME=$1
fi

if [ "x$LAU_BUNDLE" = "x" ]; then
	LAU_BUNDLE=$2
fi


if [ "x$LAU_HOME" = "x" ]; then
	LAU_HOME=$3
fi

quit () {
	echo "\nUsage:\n"
	echo "deploy-led.bat --init -b [LAU_BUNDLE] -w [WILDFLY_HOME] -l [LAU_HOME]\n"
	echo "  --init  initialize laurentius.home and wildfly properties. "
	echo "  -w   WILDFLY_HOME -  path jboss home: ex.: c:\\temp\\$APPL_SERVER.0.Final\\."
	echo "  -b   LAU_BUNDLE   - path to unziped Laurentius bundle if not given parent script folder is setted."
	echo "  -l   LAU_HOME     - path tom application home folder  (laurentius.home) if is not given and --init is setted than '[WILDFLY_HOME]\standalone\data\' is setted.	"
        exit
}

if [ "x$WILDFLY_HOME" = "x" ]; then
	quit;
fi

if [ ! -d "$WILDFLY_HOME" ]; then
	echo "WILDFLY_HOME folder not exists! Check parameters!"
	quit;
fi


if [ "x$LAU_BUNDLE" = "x" ]; then
	DIRNAME=`dirname "$0"`
	LAU_BUNDLE="$DIRNAME/../"
fi

if [ ! -d "$LAU_BUNDLE" ]; then
	echo "LAU_BUNDLE folder not exists! Check parameters!"
	quit;
fi



if [ "x$LAU_HOME" = "x" ]; then
	LAU_HOME="$WILDFLY_HOME/standalone/data/";
fi

#-------------------------------------------------------------------------------
# copy library modules
cp -r "$LAU_BUNDLE/modules/si" "$WILDFLY_HOME/modules/"
cp "$LAU_BUNDLE/$APPL_SERVER/modules/si.laurentius.module.xml" "$WILDFLY_HOME/modules/si/laurentius/main/module.xml"


# copy 'org' library modules (primefaces, pdfbox)
cp -r "$LAU_BUNDLE/modules/org" "$WILDFLY_HOME/modules/"


#-------------------------------------------------------------------------------
# application modules
cp "$LAU_BUNDLE/deployments/Laurentius-dao.jar"  "$WILDFLY_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/Laurentius-msh.ear"  "$WILDFLY_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/laurentius-ws.war"  "$WILDFLY_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/laurentius-web.war"  "$WILDFLY_HOME/standalone/deployments/"

# application plugins
cp "$LAU_BUNDLE/deployments/plugin-zpp.war"  "$WILDFLY_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/plugin-basic.war"  "$WILDFLY_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/plugin-testcase.war"  "$WILDFLY_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/example-web-plugin.war"  "$WILDFLY_HOME/standalone/deployments/"


if [ "$INIT" = "TRUE" ]; then

	# copy module fix
	if [ -e "$LAU_BUNDLE/$APPL_SERVER/modules/org.apache.ws.security.module.xml" ]; then
		cp "$LAU_BUNDLE/$APPL_SERVER/modules/org.apache.ws.security.module.xml" "$WILDFLY_HOME/modules/system/layers/base/org/apache/ws/security/main/module.xml"
	fi

	# copy 'org' library modules (primefaces, pdfbox)
	cp -r "$LAU_BUNDLE/modules/org" "$WILDFLY_HOME/modules/"
	

	# copy start scripts
	cp "$LAU_BUNDLE/$APPL_SERVER/laurentius-demo.sh" "$WILDFLY_HOME/bin/"
	chmod u+x "$WILDFLY_HOME/bin/laurentius-demo.sh"

	# create home folder
	mkdir -p  "$LAU_HOME"
	cp -r "$LAU_BUNDLE/laurentius-home" "$LAU_HOME"
	
	# copy configuration
	cp "$LAU_BUNDLE/$APPL_SERVER/config/laurentius-roles.properties" "$WILDFLY_HOME/standalone/configuration/"
	cp "$LAU_BUNDLE/$APPL_SERVER/config/laurentius-users.properties" "$WILDFLY_HOME/standalone/configuration/"
	cp "$LAU_BUNDLE/$APPL_SERVER/config/standalone-laurentius.xml" "$WILDFLY_HOME/standalone/configuration/"
	cp "$LAU_BUNDLE/$APPL_SERVER/config/test-tls-keystore.jks" "$WILDFLY_HOME/standalone/configuration/"

	

fi


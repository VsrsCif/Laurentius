#!/bin/sh

# set fixed parameters
#WILDFLY_HOME=
#LAU_BUNDLE=
#LAU_HOME=
#INIT=


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
	echo "deploy-sed.bat --init -b [LAU_BUNDLE] -w [WILDFLY_HOME] -s [LAU_HOME]\n"
	echo "  --init  initialize laurentius.home and wildfly properties. "
	echo "  -w   WILDFLY_HOME -  path jboss home: ex.: c:\temp\wildfly-10.0.0.Final\."
	echo "  -b   LAU_BUNDLE   - path to unziped Laurentius bundle if not given parent script folder is setted."
	echo "  -s   LAU_HOME     - path tom application home folder  (laurentius.home) if is not given and --init is setted than '[WILDFLY_HOME]\standalone\data\' is setted.	"
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



# create module folder
mkdir -p  "$WILDFLY_HOME/modules/org/sed/main/"
# copy module libraries
cp "$LAU_BUNDLE/modules/Laurentius-msh-xsd-1.0.jar" "$WILDFLY_HOME/modules/org/sed/main/"
cp "$LAU_BUNDLE/modules/Laurentius-wsdl-1.0.jar" "$WILDFLY_HOME/modules/org/sed/main/"
cp "$LAU_BUNDLE/modules/Laurentius-commons-1.0.jar" "$WILDFLY_HOME/modules/org/sed/main/"
# copy module descriptor
cp "$LAU_BUNDLE/modules/org.sed.module.xml" "$WILDFLY_HOME/modules/org/sed/main/module.xml"



# deploy commons ejbs
cp "$LAU_BUNDLE/deployments/Laurentius-dao.jar"  "$WILDFLY_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/sed-basic-tasks.jar"  "$WILDFLY_HOME/standalone/deployments/"
# deploy modules 
cp "$LAU_BUNDLE/deployments/Laurentius-msh.ear"  "$WILDFLY_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/Laurentius-ws.war"  "$WILDFLY_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/Laurentius-webgui.war"  "$WILDFLY_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/plugin-zpp.war"  "$WILDFLY_HOME/standalone/deployments/"

if [ "$INIT" = "TRUE" ]; then

	# set fix for module org.apache.ws.security
	cp "$LAU_BUNDLE/modules/org.apache.ws.securitymodule.xml" "$WILDFLY_HOME/modules/system/layers/base/org/apache/ws/security/main/module.xml"


	# copy start scripts
	cp "$LAU_BUNDLE/widlfly-10/start-sed.sh" "$WILDFLY_HOME/bin/"
	chmod u+x "$WILDFLY_HOME/bin/start-sed.sh"

	# create home folder
	mkdir -p  "$LAU_HOME"
	cp -r "$LAU_BUNDLE/laurentius.home" "$LAU_HOME"
	
	# copy configuration
	cp "$LAU_BUNDLE/widlfly-10/config/sed-roles.properties" "$WILDFLY_HOME/standalone/configuration/"
	cp "$LAU_BUNDLE/widlfly-10/config/sed-users.properties" "$WILDFLY_HOME/standalone/configuration/"
	cp "$LAU_BUNDLE/widlfly-10/config/standalone-ebms.xml" "$WILDFLY_HOME/standalone/configuration/"

fi


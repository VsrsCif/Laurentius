#!/bin/sh

# set fixed parameters
#JBOSS_HOME=
#LAU_BUNDLE=
#LAU_HOME=
#INIT=


while [ "$#" -gt 0 ]
do
key="$1"

  case $key in
    -j|--jboss)
      JBOSS_HOME="$2"
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


if [ "x$JBOSS_HOME" = "x" ]; then
	JBOSS_HOME=$1
fi

if [ "x$LAU_BUNDLE" = "x" ]; then
	LAU_BUNDLE=$2
fi


if [ "x$LAU_HOME" = "x" ]; then
	LAU_HOME=$3
fi

quit () {
	echo "\nUsage:\n"
	echo "deploy-led.sh --init -b [LAU_BUNDLE] -w [JBOSS_HOME] -l [LAU_HOME]\n"
	echo "  --init  initialize laurentius.home and jboss properties. "
	echo "  -j   JBOSS_HOME -  path jboss home: ex.: c:\temp\jboss-eap-7.0\."
	echo "  -b   LAU_BUNDLE   - path to unziped Laurentius bundle if not given parent script folder is setted."
	echo "  -l   LAU_HOME     - path tom application home folder  (laurentius.home) if is not given and --init is setted than '[JBOSS_HOME]\standalone\data\' is setted.	"
        exit
}

if [ "x$JBOSS_HOME" = "x" ]; then
	quit;
fi

if [ ! -d "$JBOSS_HOME" ]; then
	echo "JBOSS_HOME folder not exists! Check parameters!"
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
	LAU_HOME="$JBOSS_HOME/standalone/data/";
fi

# copy modules
cp -r "$LAU_BUNDLE/modules/org" "$JBOSS_HOME/modules/"
cp -r "$LAU_BUNDLE/modules/si" "$JBOSS_HOME/modules/"
# copy module descriptor
cp "$LAU_BUNDLE/modules/si.laurentius.module.xml" "$JBOSS_HOME/modules/si/laurentius/main/module.xml"




# application modules
cp "$LAU_BUNDLE/deployments/Laurentius-dao.jar"  "$JBOSS_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/Laurentius-msh.ear"  "$JBOSS_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/laurentius-ws.war"  "$JBOSS_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/laurentius-web.war"  "$JBOSS_HOME/standalone/deployments/"

# application plugins
cp "$LAU_BUNDLE/deployments/plugin-zpp.war"  "$JBOSS_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/plugin-basic.war"  "$JBOSS_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/plugin-testcase.war"  "$JBOSS_HOME/standalone/deployments/"
cp "$LAU_BUNDLE/deployments/example-web-plugin.war"  "$JBOSS_HOME/standalone/deployments/"


if [ "$INIT" = "TRUE" ]; then

	# set fix for module org.apache.ws.security
	cp "$LAU_BUNDLE/modules/org.apache.ws.securitymodule_jboss-eap7.0.xml" "$JBOSS_HOME/modules/system/layers/base/org/apache/ws/security/main/module.xml"

	# copy start scripts
	cp "$LAU_BUNDLE/jboss-eap-7.0/laurentius-demo.sh" "$JBOSS_HOME/bin/"
	chmod u+x "$JBOSS_HOME/bin/laurentius-demo.sh"

	# create home folder
	mkdir -p  "$LAU_HOME"
	cp -r "$LAU_BUNDLE/laurentius-home" "$LAU_HOME"
	
	# copy configuration
	cp "$LAU_BUNDLE/jboss-eap-7.0/config/laurentius-roles.properties" "$JBOSS_HOME/standalone/configuration/"
	cp "$LAU_BUNDLE/jboss-eap-7.0/config/laurentius-users.properties" "$JBOSS_HOME/standalone/configuration/"
	cp "$LAU_BUNDLE/jboss-eap-7.0/config/standalone-laurentius.xml" "$JBOSS_HOME/standalone/configuration/"
	cp "$LAU_BUNDLE/jboss-eap-7.0/config/test-tls-keystore.jks" "$JBOSS_HOME/standalone/configuration/"
	mv "$JBOSS_HOME/bin/standalone.conf" "$JBOSS_HOME/bin/standalone.conf.bck"
	cp "$LAU_BUNDLE/jboss-eap-7.0/config/standalone.conf" "$JBOSS_HOME/bin/"
fi



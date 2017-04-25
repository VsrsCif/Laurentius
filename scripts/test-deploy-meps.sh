#/!bin/sh



WILDFLY_HOME="wildfly-10.1.0.Final"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"
MEPS_UNZIP="$FOLDER_DEPLOY/meps"

rm -rf "$FOLDER_DEPLOY/$MEPS_UNZIP"
unzip -q plugin-meps-*.zip -d $MEPS_UNZIP


cp $MEPS_UNZIP/plugin-meps.war $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/deployments/

cp $MEPS_UNZIP/init/pmode-conf.xml $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/data/laurentius-home/conf/
cp -r $MEPS_UNZIP/init/meps $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/data/laurentius-home/plugins/


cd "$FOLDER_DEPLOY/$WILDFLY_HOME/bin"

./laurentius-demo.sh --init -d mb-laurentius.si

#/!bin/sh



WILDFLY_HOME="wildfly-10.1.0.Final"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"
MEPS_UNZIP="$FOLDER_DEPLOY/meps"

#rm -rf "$FOLDER_DEPLOY/$MEPS_UNZIP"
#unzip -q plugin-meps-*.zip -d $MEPS_UNZIP


cp ../../plugins/Laurentius-meps-plugin/target/plugin-meps.war $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/deployments/

cp ../../plugins/Laurentius-meps-plugin/src/main/resources/init/pmode-conf.xml $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/data/laurentius-home/conf/
cp -r ../../plugins/Laurentius-meps-plugin/src/main/resources/init/meps $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/data/laurentius-home/plugins/


cd "$FOLDER_DEPLOY/$WILDFLY_HOME/bin"

./laurentius-demo.sh --init -d mb-laurentius.si

#/!bin/sh



WILDFLY_HOME="wildfly-11.0.0.Final"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"


#rm -rf "$FOLDER_DEPLOY/$MEPS_UNZIP"
#unzip -q plugin-meps-*.zip -d $MEPS_UNZIP


cp ../../plugins/Laurentius-meps-plugin/target/plugin-meps.war $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/deployments/
cp -r ../../plugins/Laurentius-meps-plugin/src/main/resources/init/meps $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/data/laurentius-home/plugins/

cd "$FOLDER_DEPLOY/$WILDFLY_HOME/bin"

./laurentius-demo.sh --init -d mb-laurentius.si

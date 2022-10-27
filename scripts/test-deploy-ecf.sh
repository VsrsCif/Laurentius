#/!bin/sh



WILDFLY_HOME="wildfly-11.0.0.Final"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"


cp ../Laurentius-plugins/Laurentius-ecf-plugin/target/plugin-odlozisce.war $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/deployments/
cp -r ../Laurentius-plugins/Laurentius-ecf-plugin/src/main/resources/init/def-init-data.xml $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/data/laurentius-home/plugins/

cd "$FOLDER_DEPLOY/$WILDFLY_HOME/bin"

./laurentius-init.sh --init -d mb-laurentius.si

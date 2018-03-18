#/!bin/sh

#/!bin/sh


WILDFLY_HOME="jboss-eap-7.0"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"
BOUNDLE_NAME="Laurentius-test"

rm -rf "$FOLDER_DEPLOY/$WILDFLY_HOME"
unzip -q "../../settings/$WILDFLY_HOME.0.zip" -d $FOLDER_DEPLOY


cd "$FOLDER_DEPLOY/$BOUNDLE_NAME/jboss-eap-7.0"

./deploy-laurentius.sh --init -s "../../$WILDFLY_HOME"

cd "../../$WILDFLY_HOME/bin"

./laurentius-init.sh --init -d mb-laurentius.si


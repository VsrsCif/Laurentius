#/!bin/sh


WILDFLY_HOME="wildfly-10.1.0.Final"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"
BOUNDLE_NAME="Laurentius-test"

rm -rf "$BOUNDLE_NAME/$WILDFLY_HOME"
unzip -q "$WILDFLY_HOME.zip" -d $FOLDER_DEPLOY


cd "$FOLDER_DEPLOY/$BOUNDLE_NAME/widlfly-10.1"

./deploy-laurentius.sh --init -w "../../$WILDFLY_HOME"

cd "../../$WILDFLY_HOME/bin"

./laurentius-demo.sh --init -d test-laurentius.org







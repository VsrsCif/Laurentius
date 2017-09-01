#/!bin/sh


JBOSS_HOME="jboss-eap-7.0"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"
BOUNDLE_NAME="Laurentius-test"

rm -rf "$FOLDER_DEPLOY/$JBOSS_HOME"
unzip -q "../../settings/$JBOSS_HOME.0.zip" -d $FOLDER_DEPLOY


cd "$FOLDER_DEPLOY/$BOUNDLE_NAME/jboss-eap-7.0"

./deploy-laurentius.sh --init -j "../../$JBOSS_HOME"

cd "../../$JBOSS_HOME/bin"

#./laurentius-demo.sh --init -d test-12345678901234567890-12345678901234567890-123456789012345678901234567890-1234567890123456789012345678901234567890-laurentius.si
./laurentius-demo.sh --init -d mb-laurentius.si


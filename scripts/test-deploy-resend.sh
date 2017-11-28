#/!bin/sh



WILDFLY_HOME="wildfly-11.0.0.Final"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"


#rm -rf "$FOLDER_DEPLOY/$MEPS_UNZIP"
#unzip -q plugin-meps-*.zip -d $MEPS_UNZIP

cp "/sluzba/code/e-vlozisce/trunk/04 Implementacija/e-vlozisce-code/laurentius-plugin/evip-test-plugin/target/plugin-evip-meps.jar"  ./test-deploy/wildfly-11.0.0.Final/standalone/deployments/

cd "$FOLDER_DEPLOY/$WILDFLY_HOME/bin"

./laurentius-demo.sh --init -d mb-laurentius.si

#/!bin/sh


LAU_PROJECT=$1
if [ "x$LAU_PROJECT" = "x" ]; then
	LAU_PROJECT="../"
fi


ZIP_FILENAME="Laurentius-$(date +%Y%m%d_%H%M)"
ZIP_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"

rm -rf $ZIP_FILENAME
rm -rf $FOLDER_DEPLOY


mkdir $ZIP_FILENAME
mkdir "$ZIP_FILENAME/modules"
mkdir "$ZIP_FILENAME/deployments"
mkdir "$ZIP_FILENAME/widlfly-10.1"
mkdir $FOLDER_DEPLOY

cp "$LAU_PROJECT/Laurentius-libs/Laurentius-msh-xsd/target/Laurentius-msh-xsd-1.0.jar" "$ZIP_FILENAME/modules/" 	
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-wsdl/target/Laurentius-wsdl-1.0.jar" "$ZIP_FILENAME/modules/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-commons/target/Laurentius-commons-1.0.jar" "$ZIP_FILENAME/modules/"
cp "$LAU_PROJECT/scripts/wildfly-10.1/modules/si.laurentius.module.xml" "$ZIP_FILENAME/modules/"
cp "$LAU_PROJECT/scripts/wildfly-10.1/modules/org.apache.ws.securitymodule.xml" "$ZIP_FILENAME/modules/"

# commons ejbs
cp "$LAU_PROJECT/Laurentius-dao/target/Laurentius-dao.jar" "$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-tasks/Laurentius-basic-tasks/target/Laurentius-basic-tasks.jar" "$ZIP_FILENAME/deployments/"
# modules
cp "$LAU_PROJECT/Laurentius-msh/Laurentius-msh-ear/target/Laurentius-msh.ear" "$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-app/Laurentius-ws/target/laurentius-ws.war"  "$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-app/Laurentius-web/target/laurentius-web.war"  "$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-zpp-plugin/target/plugin-zpp.war"  "$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-test-case-plugin/target/plugin-testcase.war"  "$ZIP_FILENAME/deployments/"

# configuration file
cp -r "$LAU_PROJECT/scripts/wildfly-10.1/config"  "$ZIP_FILENAME/widlfly-10.1"
# deploy script
cp "$LAU_PROJECT/scripts/wildfly-10.1/deploy-laurentius.sh" "$ZIP_FILENAME/widlfly-10.1"
cp "$LAU_PROJECT/scripts/wildfly-10.1/laurentius-demo.sh" "$ZIP_FILENAME/widlfly-10.1"
cp "$LAU_PROJECT/scripts/wildfly-10.1/deploy-laurentius.bat" "$ZIP_FILENAME/widlfly-10.1"
cp "$LAU_PROJECT/scripts/wildfly-10.1/laurentius-demo.bat" "$ZIP_FILENAME/widlfly-10.1"

# init data:
cp -r "$LAU_PROJECT/scripts/laurentius-demo" "$ZIP_FILENAME/laurentius-home"

zip -r "$ZIP_FILENAME.zip" $ZIP_FILENAME
# move "boudle folder to test folder fo test deploy"
mv $ZIP_FILENAME $FOLDER_DEPLOY/$ZIP_TEST






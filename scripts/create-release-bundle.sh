#/!bin/sh

LAU_RELEASE_FOLDER="releases"
LAU_PROJECT=$1
if [ "x$LAU_PROJECT" = "x" ]; then
	LAU_PROJECT="../"
fi

if [ ! -d "$LAU_RELEASE_FOLDER" ]; then
	mkdir $LAU_RELEASE_FOLDER
fi


VERSION="2.0-SNAPSHOT"
ZIP_FILENAME="Laurentius-$VERSION-$(date +%Y%m%d_%H%M)"
ZIP_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"


rm -rf $LAU_RELEASE_FOLDER/$ZIP_FILENAME
rm -rf $FOLDER_DEPLOY


mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME"
mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules"
mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments"
mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/wildfly-10.1"
mkdir $FOLDER_DEPLOY
# si.laurentius module libralies
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-msh-xsd/target/Laurentius-msh-xsd-$VERSION.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/" 	
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-wsdl/target/Laurentius-wsdl-$VERSION.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-commons/target/Laurentius-commons-$VERSION.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-lce/target/Laurentius-lce-$VERSION.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-plugin-interfaces/target/Laurentius-plugin-interfaces-$VERSION.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/"

cp "$LAU_PROJECT/scripts/wildfly-10.1/modules/si.laurentius.module.xml" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/"
sed -i -- "s/VERSION/$VERSION/g" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si.laurentius.module.xml"
cp "$LAU_PROJECT/scripts/wildfly-10.1/modules/org.apache.ws.securitymodule.xml" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/"
cp -r "$LAU_PROJECT/scripts/wildfly-10.1/modules/org"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/"

# application modules
cp "$LAU_PROJECT/Laurentius-dao/target/Laurentius-dao.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-msh/Laurentius-msh-ear/target/Laurentius-msh.ear" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-app/Laurentius-ws/target/laurentius-ws.war"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-app/Laurentius-web/target/laurentius-web.war"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
# application plugins
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-zpp-plugin/target/plugin-zpp.war"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-basic-plugin/target/plugin-basic.war"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-test-case-plugin/target/plugin-testcase.war"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-examples/example-web-plugin/target/example-web-plugin.war" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"


# configuration file
cp -r "$LAU_PROJECT/scripts/wildfly-10.1/config"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/wildfly-10.1"
# deploy script
cp "$LAU_PROJECT/scripts/wildfly-10.1/deploy-laurentius.sh" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/wildfly-10.1"
sed -i -- "s/VERSION/$VERSION/g" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/wildfly-10.1/deploy-laurentius.sh"

cp "$LAU_PROJECT/scripts/wildfly-10.1/laurentius-demo.sh" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/wildfly-10.1"

cp "$LAU_PROJECT/scripts/wildfly-10.1/deploy-laurentius.bat" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/wildfly-10.1"
sed -i -- "s/VERSION/$VERSION/g" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/wildfly-10.1/deploy-laurentius.bat"

cp "$LAU_PROJECT/scripts/wildfly-10.1/laurentius-demo.bat" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/wildfly-10.1"

# init data:
cp -r "$LAU_PROJECT/scripts/laurentius-demo" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/laurentius-home"

zip -r "$LAU_RELEASE_FOLDER/$ZIP_FILENAME.zip" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME"
# move "boudle folder to test folder fo test deploy"
mv $LAU_RELEASE_FOLDER/$ZIP_FILENAME $FOLDER_DEPLOY/$ZIP_TEST








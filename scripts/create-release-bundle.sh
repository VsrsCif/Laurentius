#/!bin/sh

LAU_RELEASE_FOLDER="releases"
LAU_PROJECT=$1
if [ "x$LAU_PROJECT" = "x" ]; then
	LAU_PROJECT="../"
fi

if [ ! -d "$LAU_RELEASE_FOLDER" ]; then
	mkdir $LAU_RELEASE_FOLDER
fi


ZIP_FILENAME="Laurentius-$(date +%Y%m%d_%H%M)"
ZIP_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"

rm -rf $LAU_RELEASE_FOLDER/$ZIP_FILENAME
rm -rf $FOLDER_DEPLOY

# create folders
mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME"
mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments"
mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/wildfly-10.1"
mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/jboss-eap-7.0"
mkdir -p "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main"
mkdir $FOLDER_DEPLOY


cp -r "$LAU_PROJECT/scripts/deployments/modules" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/"

# si.laurentius module libralies
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-msh-xsd/target/Laurentius-msh-xsd-1.0.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/" 	
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-wsdl/target/Laurentius-wsdl-1.0.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-commons/target/Laurentius-commons-1.0.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-lce/target/Laurentius-lce-1.0.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-plugin-interfaces/target/Laurentius-plugin-interfaces-1.0.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/"



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

# wildfly configuration
cp -r "$LAU_PROJECT/scripts/deployments/wildfly-10.1" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/"
# jboss eap
cp -r "$LAU_PROJECT/scripts/deployments/jboss-eap-7.0" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/"


# init data:
cp -r "$LAU_PROJECT/scripts/deployments/laurentius-demo" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/laurentius-home"
cd  "$LAU_RELEASE_FOLDER"
zip -r "$ZIP_FILENAME.zip" "$ZIP_FILENAME"
cd ..
# move "boudle folder to test folder fo test deploy"
mv $LAU_RELEASE_FOLDER/$ZIP_FILENAME $FOLDER_DEPLOY/$ZIP_TEST








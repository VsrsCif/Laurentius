#/!bin/sh


LAU_PROJECT=$1
if [ "x$LAU_PROJECT" = "x" ]; then
	LAU_PROJECT="../"
fi


ZIP_FILENAME="Laurentius-$(date +%Y%m%d_%H%M)"
rm -rf $ZIP_FILENAME

mkdir $ZIP_FILENAME
mkdir "$ZIP_FILENAME/modules"
mkdir "$ZIP_FILENAME/deployments"
mkdir "$ZIP_FILENAME/widlfly-10"

cp "$LAU_PROJECT/Laurentius-libs/Laurentius-msh-xsd/target/Laurentius-msh-xsd-1.0.jar" "$ZIP_FILENAME/modules/" 	
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-wsdl/target/Laurentius-wsdl-1.0.jar" "$ZIP_FILENAME/modules/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-commons/target/Laurentius-commons-1.0.jar" "$ZIP_FILENAME/modules/"
cp "$LAU_PROJECT/scripts/wildfly-10/modules/org.sed.module.xml" "$ZIP_FILENAME/modules/"
cp "$LAU_PROJECT/scripts/wildfly-10/modules/org.apache.ws.securitymodule.xml" "$ZIP_FILENAME/modules/"

# commons ejbs
cp "$LAU_PROJECT/Laurentius-dao/target/Laurentius-dao.jar" "$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-tasks/sed-basic-tasks/target/sed-basic-tasks.jar" "$ZIP_FILENAME/deployments/"
# modules
cp "$LAU_PROJECT/Laurentius-msh/Laurentius-msh-ear/target/Laurentius-msh.ear" "$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-app/Laurentius-ws/target/Laurentius-ws.war"  "$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-app/Laurentius-web/target/Laurentius-webgui.war"  "$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-zpp-plugin/target/plugin-zpp.war"  "$ZIP_FILENAME/deployments/"
# configuration file
cp -r "$LAU_PROJECT/scripts/wildfly-10/config"  "$ZIP_FILENAME/widlfly-10"
# deploy script
cp "$LAU_PROJECT/scripts/wildfly-10/deploy-sed.sh" "$ZIP_FILENAME/widlfly-10"
cp "$LAU_PROJECT/scripts/wildfly-10/start-sed.sh" "$ZIP_FILENAME/widlfly-10"
cp "$LAU_PROJECT/scripts/wildfly-10/deploy-sed.bat" "$ZIP_FILENAME/widlfly-10"
cp "$LAU_PROJECT/scripts/wildfly-10/start-sed.bat" "$ZIP_FILENAME/widlfly-10"

# init data:
cp -r "$LAU_PROJECT/scripts/init-sample" "$ZIP_FILENAME/laurentius.home"

zip -r "$ZIP_FILENAME.zip" $ZIP_FILENAME

rm -rf $ZIP_FILENAME






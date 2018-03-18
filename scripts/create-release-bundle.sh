#/!bin/sh

# laurentius version
VERSION="2.0"
# releases folder 
LAU_RELEASE_FOLDER="releases"
# release zip file name
ZIP_FILENAME="Laurentius-$VERSION-$(date +%Y%m%d_%H%M)"

ZIP_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"


#AS_WILDFLY_10_1="wildfly-10.1"
AS_WILDFLY_11_0="wildfly-11.0"
AS_JBOSS_EAP_7_0="jboss-eap-7.0"

AS_ARRAY=($AS_WILDFLY_10_1 $AS_WILDFLY_11_0 $AS_JBOSS_EAP_7_0)


# 
LAU_PROJECT=$1
if [ "x$LAU_PROJECT" = "x" ]; then
	LAU_PROJECT="../"
fi

if [ ! -d "$LAU_RELEASE_FOLDER" ]; then
	mkdir $LAU_RELEASE_FOLDER
fi

# clear target folders 
rm -rf $LAU_RELEASE_FOLDER/$ZIP_FILENAME
rm -rf $FOLDER_DEPLOY


mkdir    "$LAU_RELEASE_FOLDER/$ZIP_FILENAME"
mkdir -p "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/"
mkdir    "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments"
for asItem in ${AS_ARRAY[*]}
do
	mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem"
	mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem/config"
done

# create test deploy folder
mkdir $FOLDER_DEPLOY
#--------------------------------------------------------------------------------------
# COPY LIBRARIES / MODULES
# si.laurentius module libralies
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-msh-xsd/target/Laurentius-msh-xsd-$VERSION.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/" 	
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-wsdl/target/Laurentius-wsdl-$VERSION.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-commons/target/Laurentius-commons-$VERSION.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-lce/target/Laurentius-lce-$VERSION.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/"
cp "$LAU_PROJECT/Laurentius-libs/Laurentius-plugin-interfaces/target/Laurentius-plugin-interfaces-$VERSION.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/si/laurentius/main/"

#create module descriptions for application servers 
for asItem in ${AS_ARRAY[*]}
do
	# copy module descriptors for application servers
	cp -r "$LAU_PROJECT/scripts/install/$asItem/modules" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem/"
	# set library versions for si.laureius module
	sed -i -- "s/VERSION/$VERSION/g" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem/modules/si.laurentius.module.xml"
done

# copy common libraries (pdfbox, primefaces)
cp -r "$LAU_PROJECT/scripts/install/modules/org" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/modules/"


#--------------------------------------------------------------------------------------
# COPY APPLICATION MODULES
# application modules
cp "$LAU_PROJECT/Laurentius-dao/target/Laurentius-dao.jar" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-msh/Laurentius-msh-ear/target/Laurentius-msh.ear" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-app/Laurentius-ws/target/laurentius-ws.war"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-app/Laurentius-web/target/laurentius-web.war"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
#--------------------------------------------------------------------------------------
# application plugins
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-zpp-plugin/target/plugin-zpp.war"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-basic-plugin/target/plugin-basic.war"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-test-case-plugin/target/plugin-testcase.war"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-ecf-plugin/target/plugin-court-filing.jar"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"
#cp "$LAU_PROJECT/Laurentius-examples/example-web-plugin/target/example-web-plugin.war" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/deployments/"

#--------------------------------------------------------------------------------------
# SET APPLICATION SETTINGS AND DEMO DEPLOY/START SCRIPT

for asItem in ${AS_ARRAY[*]}
do
	# copy application server config script
	cp -r "$LAU_PROJECT/scripts/install/$asItem/config"  "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem"
	# copy deploy script
	cp "$LAU_PROJECT/scripts/install/$asItem/deploy-laurentius.sh" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem"
	cp "$LAU_PROJECT/scripts/install/$asItem/deploy-laurentius.bat" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem"
	
	cp "$LAU_PROJECT/scripts/install/$asItem/laurentius-demo.sh" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem"
	cp "$LAU_PROJECT/scripts/install/$asItem/laurentius-demo.bat" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem"
	
	cp "$LAU_PROJECT/scripts/install/$asItem/laurentius-init.sh" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem"
	cp "$LAU_PROJECT/scripts/install/$asItem/laurentius-init.bat" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/$asItem"
done


#--------------------------------------------------------------------------------------
# COPY INIT HOME DATA  
# init data:
cp -r "$LAU_PROJECT/scripts/install/laurentius-home" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/laurentius-home"

#--------------------------------------------------------------------------------------
# CREATE_BOUNDLE   
cd $LAU_RELEASE_FOLDER
zip -r "$ZIP_FILENAME.zip" $ZIP_FILENAME
# CALCULATE CHECKSUM   
sha256sum --tag "$ZIP_FILENAME.zip" >> "$ZIP_FILENAME.sha256"

cd ..

#--------------------------------------------------------------------------------------
# Prepare for test   
# move "boudle folder to test folder fo test deploy"
mv $LAU_RELEASE_FOLDER/$ZIP_FILENAME $FOLDER_DEPLOY/$ZIP_TEST








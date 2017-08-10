#/!bin/sh

LAU_RELEASE_FOLDER="releases"
LAU_PROJECT=$1
if [ "x$LAU_PROJECT" = "x" ]; then
	LAU_PROJECT="../"
fi


if [ "x$LAU_RELEASE_FOLDER" = "x" ]; then
	mkdir $LAU_RELEASE_FOLDER
fi


ZIP_FILENAME="plugin-meps-$(date +%Y%m%d_%H%M)"

# clean 
rm -rf "$LAU_RELEASE_FOLDER/$ZIP_FILENAME"
rm -rf $FOLDER_DEPLOY


mkdir "$LAU_RELEASE_FOLDER/$ZIP_FILENAME"


# si.laurentius module libralies
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-meps-plugin/target/plugin-meps.war" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/" 	
cp -r "$LAU_PROJECT/Laurentius-plugins/Laurentius-meps-plugin/src/main/resources/init" "$LAU_RELEASE_FOLDER/$ZIP_FILENAME/" 

cd 	"$LAU_RELEASE_FOLDER/$ZIP_FILENAME"

zip -r "../$ZIP_FILENAME.zip" *

cd ..
# remove "boudle folder to test folder fo test deploy"
rm -rf $ZIP_FILENAME
cd ..


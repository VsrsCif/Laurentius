#/!bin/sh


LAU_PROJECT=$1
if [ "x$LAU_PROJECT" = "x" ]; then
	LAU_PROJECT="../"
fi


ZIP_FILENAME="plugin-meps-$(date +%Y%m%d_%H%M)"


rm -rf $ZIP_FILENAME
rm -rf $FOLDER_DEPLOY


mkdir $ZIP_FILENAME


# si.laurentius module libralies
cp "$LAU_PROJECT/Laurentius-plugins/Laurentius-meps-plugin/target/plugin-meps.war" "$ZIP_FILENAME/" 	
cp -r "$LAU_PROJECT/Laurentius-plugins/Laurentius-meps-plugin/src/main/resources/init" "$ZIP_FILENAME/" 

cd 	$ZIP_FILENAME

zip -r "../$ZIP_FILENAME.zip" *

cd ..
# move "boudle folder to test folder fo test deploy"
rm -rf $ZIP_FILENAME







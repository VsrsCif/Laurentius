#!/bin/sh

REPO_FOLDER=../../laurentius-maven-repo
GROUP_ID=si.vsrs.cif.sed
VERSION=1.1
COMM_PROP="-DlocalRepositoryPath=$REPO_FOLDER -DcreateChecksum=true -Dpackaging=jar -DgroupId=$GROUP_ID -Dversion=$VERSION"

#install commons
mvn install:install-file $COMM_PROP -DartifactId=Laurentius-commons  -Dfile=../Laurentius-libs/Laurentius-commons/target/Laurentius-commons-$VERSION.jar 
mvn install:install-file $COMM_PROP -DartifactId=Laurentius-plugin-interfaces  -Dfile=../Laurentius-libs/Laurentius-plugin-interfaces/target/Laurentius-plugin-interfaces-$VERSION.jar 
mvn install:install-file $COMM_PROP -DartifactId=Laurentius-msh-xsd  -Dfile=../Laurentius-libs/Laurentius-msh-xsd/target/Laurentius-msh-xsd-$VERSION.jar
mvn install:install-file $COMM_PROP -DartifactId=Laurentius-wsdl -Dfile=../Laurentius-libs/Laurentius-wsdl/target/Laurentius-wsdl-$VERSION.jar
mvn install:install-file $COMM_PROP -DartifactId=Laurentius-lce -Dfile=../Laurentius-libs/Laurentius-lce/target/Laurentius-lce-$VERSION.jar


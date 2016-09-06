rm -rf ../standalone/tmp/*
rm -rf ../standalone/data/*
rm -rf ../standalone/deployments/*.failed
rm -rf ../standalone/deployments/*.deployed

export JAVA_HOME=/opt/java/jdk1.8.0_60/


cp "/Laurentius/Laurentius-libs/Laurentius-msh-xsd/target/Laurentius-msh-xsd-1.0.jar" ../modules/org/sed/main/
cp "/Laurentius/Laurentius-libs/Laurentius-wsdl/target/Laurentius-wsdl-1.0.jar" ../modules/org/sed/main/
cp "Laurentius/Laurentius-libs/Laurentius-commons/target/Laurentius-commons-1.0.jar" ../modules/org/sed/main/



cp "/Laurentius/Laurentius-msh/Laurentius-msh-ear/target/Laurentius-msh.ear" ../standalone/deployments/
cp "/Laurentius/Laurentius-app/Laurentius-ws/target/Laurentius-ws.war" ../standalone/deployments/
cp "/Laurentius/Laurentius-app/Laurentius-web/target/Laurentius-webgui.war" ../standalone/deployments/
cp "/Laurentius/Laurentius-plugins/Laurentius-zpp-plugin/target/plugin-zpp.war"  ../standalone/deployments/

cp "/Laurentius/Laurentius-dao/target/Laurentius-dao.jar" ../standalone/deployments/






#./standalone.sh -c standalone-ebms.xml -Dlaurentius.home=/opt/servers/wildfly-10.0.0.Final/laurentius.home -Dorg.sed.msh.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect


./standalone.sh -c standalone-ebms.xml -Dlaurentius.home=/opt/servers/wildfly-10.0.0.Final/laurentius.home -Dorg.sed.msh.hibernate.hbm2ddl.auto=create -Dorg.sed.msh.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect -Dorg.sed.msh.sender.workers.count=7



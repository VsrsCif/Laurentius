FROM jboss/wildfly

ENV VERSION 2.0-SNAPSHOT
ENV LAU_PROJECT .
ENV WILDFLY_HOME /opt/jboss/wildfly


# si.laurentius module libralies
ADD  $LAU_PROJECT/Laurentius-libs/Laurentius-msh-xsd/target/Laurentius-msh-xsd-$VERSION.jar $WILDFLY_HOME/modules/si/laurentius/main/
ADD  $LAU_PROJECT/Laurentius-libs/Laurentius-wsdl/target/Laurentius-wsdl-$VERSION.jar $WILDFLY_HOME/modules/si/laurentius/main/
ADD  $LAU_PROJECT/Laurentius-libs/Laurentius-commons/target/Laurentius-commons-$VERSION.jar $WILDFLY_HOME/modules/si/laurentius/main/
ADD  $LAU_PROJECT/Laurentius-libs/Laurentius-lce/target/Laurentius-lce-$VERSION.jar $WILDFLY_HOME/modules/si/laurentius/main/
ADD  $LAU_PROJECT/Laurentius-libs/Laurentius-plugin-interfaces/target/Laurentius-plugin-interfaces-$VERSION.jar $WILDFLY_HOME/modules/si/laurentius/main/
# set module
ADD  $LAU_PROJECT/scripts/wildfly-10.1/modules/si.laurentius.module.xml $WILDFLY_HOME/modules/si/laurentius/main/module.xml
# set module fix
ADD  $LAU_PROJECT/scripts/wildfly-10.1/modules/org.apache.ws.securitymodule.xml $WILDFLY_HOME/modules/system/layers/base/org/apache/ws/security/main/module.xml

# copy modules
COPY $LAU_PROJECT/scripts/wildfly-10.1/modules/org/  $WILDFLY_HOME/modules/org/

# copy configuration
ADD $LAU_PROJECT/scripts/wildfly-10.1/config/laurentius-roles.properties $WILDFLY_HOME/standalone/configuration/"
ADD $LAU_PROJECT/scripts/wildfly-10.1/config/laurentius-users.properties $WILDFLY_HOME/standalone/configuration/"
ADD $LAU_PROJECT/scripts/wildfly-10.1/config/standalone-laurentius.xml $WILDFLY_HOME/standalone/configuration/"
ADD $LAU_PROJECT/scripts/wildfly-10.1/config/test-tls-keystore.jks $WILDFLY_HOME/standalone/configuration/"



# application modules
ADD $LAU_PROJECT/Laurentius-dao/target/Laurentius-dao.jar $WILDFLY_HOME/standalone/deployments/
ADD $LAU_PROJECT/Laurentius-msh/Laurentius-msh-ear/target/Laurentius-msh.ear $WILDFLY_HOME/standalone/deployments/
ADD $LAU_PROJECT/Laurentius-app/Laurentius-ws/target/laurentius-ws.war $WILDFLY_HOME/standalone/deployments/
ADD $LAU_PROJECT/Laurentius-app/Laurentius-web/target/laurentius-web.war $WILDFLY_HOME/standalone/deployments/
# application plugins
ADD $LAU_PROJECT/Laurentius-plugins/Laurentius-zpp-plugin/target/plugin-zpp.war $WILDFLY_HOME/standalone/deployments/
ADD $LAU_PROJECT/Laurentius-plugins/Laurentius-basic-plugin/target/plugin-basic.war $WILDFLY_HOME/standalone/deployments/
ADD $LAU_PROJECT/Laurentius-plugins/Laurentius-test-case-plugin/target/plugin-testcase.war $WILDFLY_HOME/standalone/deployments/
ADD $LAU_PROJECT/Laurentius-examples/example-web-plugin/target/example-web-plugin.war $WILDFLY_HOME/standalone/deployments/
# copy laurentius home
COPY $LAU_PROJECT/scripts/laurentius-demo/  $WILDFLY_HOME/standalone/data/laurentius-home/

ADD  $LAU_PROJECT/scripts/wildfly-10.1/laurentius-demo.sh  $WILDFLY_HOME/bin/


user root
RUN  cd $WILDFLY_HOME/modules/si/laurentius/main/ \
    && sed -i -- "s/VERSION/$VERSION/g" "module.xml"   \
    && chown -R jboss:jboss $WILDFLY_HOME/standalone/data    
user jboss


# Set the default command to run on boot
CMD ["/opt/jboss/wildfly/bin/laurentius-demo.sh", "--init", "-d", "test-laurentius.si"]

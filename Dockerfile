FROM maven:3.8.6-openjdk-8 as build

WORKDIR /app

# move the project over
COPY . .

# build
RUN mvn install

FROM jboss/wildfly:11.0.0.Final as package

ENV VERSION 2.0
ENV SERVER_VERSION wildfly-11.0
ENV WILDFLY_HOME /opt/jboss/wildfly


# si.laurentius module libralies
COPY --from=build /app/Laurentius-libs/Laurentius-msh-xsd/target/Laurentius-msh-xsd-$VERSION.jar $WILDFLY_HOME/modules/si/laurentius/main/
COPY --from=build /app/Laurentius-libs/Laurentius-wsdl/target/Laurentius-wsdl-$VERSION.jar $WILDFLY_HOME/modules/si/laurentius/main/
COPY --from=build /app/Laurentius-libs/Laurentius-commons/target/Laurentius-commons-$VERSION.jar $WILDFLY_HOME/modules/si/laurentius/main/
COPY --from=build /app/Laurentius-libs/Laurentius-lce/target/Laurentius-lce-$VERSION.jar $WILDFLY_HOME/modules/si/laurentius/main/
COPY --from=build /app/Laurentius-libs/Laurentius-plugin-interfaces/target/Laurentius-plugin-interfaces-$VERSION.jar $WILDFLY_HOME/modules/si/laurentius/main/
# set module
COPY --from=build /app/scripts/install/$SERVER_VERSION/modules/si.laurentius.module.xml $WILDFLY_HOME/modules/si/laurentius/main/module.xml

# copy modules
COPY --from=build /app/scripts/install/modules/org/  $WILDFLY_HOME/modules/org/

# copy configuration
COPY --from=build /app/scripts/install/$SERVER_VERSION/config/laurentius-roles.properties $WILDFLY_HOME/standalone/configuration/
COPY --from=build /app/scripts/install/$SERVER_VERSION/config/laurentius-users.properties $WILDFLY_HOME/standalone/configuration/
COPY --from=build /app/scripts/install/$SERVER_VERSION/config/standalone-laurentius.xml $WILDFLY_HOME/standalone/configuration/
COPY --from=build /app/scripts/install/$SERVER_VERSION/config/test-tls-keystore.jks $WILDFLY_HOME/standalone/configuration/

# application modules
COPY --from=build /app/Laurentius-dao/target/Laurentius-dao.jar $WILDFLY_HOME/standalone/deployments/
COPY --from=build /app/Laurentius-msh/Laurentius-msh-ear/target/Laurentius-msh.ear $WILDFLY_HOME/standalone/deployments/
COPY --from=build /app/Laurentius-app/Laurentius-ws/target/laurentius-ws.war $WILDFLY_HOME/standalone/deployments/
COPY --from=build /app/Laurentius-app/Laurentius-web/target/laurentius-web.war $WILDFLY_HOME/standalone/deployments/
# application plugins
COPY --from=build /app/Laurentius-plugins/Laurentius-zpp-plugin/target/plugin-zpp.war $WILDFLY_HOME/standalone/deployments/
COPY --from=build /app/Laurentius-plugins/Laurentius-zkp-plugin/target/plugin-zkp.war $WILDFLY_HOME/standalone/deployments/
COPY --from=build /app/Laurentius-plugins/Laurentius-basic-plugin/target/plugin-basic.war $WILDFLY_HOME/standalone/deployments/
COPY --from=build /app/Laurentius-plugins/Laurentius-test-case-plugin/target/plugin-testcase.war $WILDFLY_HOME/standalone/deployments/
COPY --from=build /app/Laurentius-examples/example-web-plugin/target/example-web-plugin.war $WILDFLY_HOME/standalone/deployments/
# copy laurentius home
COPY --from=build /app/scripts/install/laurentius-home/  $WILDFLY_HOME/standalone/data/laurentius-home/

COPY --from=build /app/scripts/install/$SERVER_VERSION/laurentius-demo.sh  $WILDFLY_HOME/bin/
COPY --from=build /app/scripts/install/$SERVER_VERSION/laurentius-init.sh  $WILDFLY_HOME/bin/

RUN echo "test=544e00c06e8229f4face117c31564c8b" >> $WILDFLY_HOME/standalone/configuration/mgmt-users.properties

user root
RUN  cd $WILDFLY_HOME/modules/si/laurentius/main/ && \
     sed -i -- "s/VERSION/$VERSION/g" "module.xml"  && \
     chown -R jboss:jboss $WILDFLY_HOME
user jboss

# Set the default command to run on boot
CMD ["/opt/jboss/wildfly/bin/laurentius-demo.sh"]

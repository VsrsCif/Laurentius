#!/bin/sh

cp ../../settings/standalone-laurentius.xml test-deploy/wildfly-10.1.0.Final/standalone/configuration/
cd test-deploy/wildfly-10.1.0.Final/bin
./laurentius-demo.sh

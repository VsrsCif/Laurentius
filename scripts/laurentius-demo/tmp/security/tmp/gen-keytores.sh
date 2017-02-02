#/bin/sh

mkdir keystore
# generate key 
echo "generate key\n"
echo "    key/cert for test party for TLS" 
$JAVA_HOME/bin/keytool -genkey -alias test-tls -keypass key1234 -keystore keystore/test-tls-keystore.jks -storepass test1234 -dname "cn=test-tls,ou=laurentius,ou=cif,ou=vsrs,ou=si" -keyalg RSA -validity 1095
echo "    test/cert for test party for AS4-message signature"
$JAVA_HOME/bin/keytool -genkey -alias test-laurentius -keypass key1234 -keystore keystore/test-laurentius-keystore.jks -storepass test1234 -dname "cn=test-laurentius,ou=laurentius,ou=cif,ou=vsrs,ou=si" -keyalg RSA -validity 1095
echo "    test/cert for test party for signature of deliveryAdvice" 
$JAVA_HOME/bin/keytool -genkey -alias test-zpp-sign -keypass key1234 -keystore keystore/test-laurentius-keystore.jks -storepass test1234 -dname "cn=test-zpp-sign,ou=laurentius,ou=cif,ou=vsrs,ou=si" -keyalg RSA -validity 1095

echo "    test key for court AS4-message signature"
$JAVA_HOME/bin/keytool -genkey -alias court-laurentius -keypass key1234 -keystore keystore/court-laurentius-keystore.jks -storepass test1234 -dname "cn=court-laurentius,ou=laurentius,ou=cif,ou=vsrs,ou=si" -keyalg RSA -validity 1095
echo "    test key for court - tls client"
$JAVA_HOME/bin/keytool -genkey -alias court-tls -keypass key1234 -keystore keystore/court-laurentius-keystore.jks -storepass test1234 -dname "cn=court-tls,ou=laurentius,ou=cif,ou=vsrs,ou=si" -keyalg RSA -validity 1095




# extract certs
echo "extract certs"
$JAVA_HOME/bin/keytool -exportcert  -keystore keystore/test-laurentius-keystore.jks -storepass test1234 -alias test-laurentius  -file keystore/test-laurentius.crt
$JAVA_HOME/bin/keytool -exportcert  -keystore keystore/test-laurentius-keystore.jks -storepass test1234 -alias test-zpp-sign  -file keystore/test-zpp-sign.crt
$JAVA_HOME/bin/keytool -exportcert  -keystore keystore/court-laurentius-keystore.jks -storepass test1234 -alias court-laurentius  -file keystore/court-laurentius.crt
$JAVA_HOME/bin/keytool -exportcert  -keystore keystore/test-tls-keystore.jks -storepass test1234 -alias test-tls  -file keystore/test-tls-laurentius.crt
$JAVA_HOME/bin/keytool -exportcert  -keystore keystore/court-laurentius-keystore.jks -storepass test1234 -alias court-tls  -file keystore/court-tls-client.crt

# import certs to trustores 
echo "import certs to trustores"
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias test-laurentius -keystore keystore/test-laurentius-truststore.jks -storepass test1234 -file keystore/test-laurentius.crt
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias court-laurentius -keystore keystore/test-laurentius-truststore.jks -storepass test1234 -file keystore/court-laurentius.crt
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias b2g-test.sodisce.si -keystore keystore/test-laurentius-truststore.jks -storepass test1234 -file b2g-test.sodisce.si.crt
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias court-tls-client -keystore keystore/test-laurentius-truststore.jks -storepass test1234 -file keystore/court-tls-client.crt

# import ssl truststore
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias test-laurentius -keystore keystore/court-laurentius-truststore.jks -storepass test1234 -file keystore/test-laurentius.crt
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias court-laurentius -keystore keystore/court-laurentius-truststore.jks -storepass test1234 -file keystore/court-laurentius.crt
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias test-zpp-sign -keystore keystore/court-laurentius-truststore.jks -storepass test1234 -file keystore/test-zpp-sign.crt
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias test-tls -keystore keystore/court-laurentius-truststore.jks -storepass test1234 -file keystore/test-tls-laurentius.crt








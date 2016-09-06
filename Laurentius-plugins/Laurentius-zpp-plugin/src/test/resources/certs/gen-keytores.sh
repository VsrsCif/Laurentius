#/bin/sh

mkdir keystore
# generate key 
echo "generate key"
$JAVA_HOME/bin/keytool -genkey -alias msh.e-box-a.si -keypass key1234 -keystore keystore/msh.e-box-a-keystore.jks -storepass test1234 -dname "cn=msh.e-box-a.si,ou=test,ou=msh,ou=jrc,ou=si" -keyalg RSA
$JAVA_HOME/bin/keytool -genkey -alias msh.e-box-b.si -keypass key1234 -keystore keystore/msh.e-box-b-keystore.jks -storepass test1234 -dname "cn=msh.e-box-b.si,ou=test,ou=msh,ou=jrc,ou=si" -keyalg RSA
$JAVA_HOME/bin/keytool -genkey -alias client-user    -keypass key1234 -keystore keystore/client-user.jks -storepass test1234 -dname "cn=Johan Pohan, ou=ebox,ou=jrc,ou=si" -keyalg RSA


# extract certs
echo "extract certs"
$JAVA_HOME/bin/keytool -exportcert  -keystore keystore/msh.e-box-a-keystore.jks -storepass test1234 -alias msh.e-box-a.si  -file keystore/msh.e-box-a.csr
$JAVA_HOME/bin/keytool -exportcert  -keystore keystore/msh.e-box-b-keystore.jks -storepass test1234 -alias msh.e-box-b.si  -file keystore/msh.e-box-b.csr

# import certs to trustores 
echo "import certs to trustores"
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias msh.e-box-a.si -keystore keystore/msh.e-box-a-truststore.jks -storepass test1234 -file keystore/msh.e-box-a.csr
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias msh.e-box-b.si -keystore keystore/msh.e-box-a-truststore.jks -storepass test1234 -file keystore/msh.e-box-b.csr

$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias msh.e-box-a.si -keystore keystore/msh.e-box-b-truststore.jks -storepass test1234 -file keystore/msh.e-box-a.csr 
$JAVA_HOME/bin/keytool -importcert -trustcacerts -noprompt -alias msh.e-box-b.si -keystore keystore/msh.e-box-b-truststore.jks -storepass test1234 -file keystore/msh.e-box-b.csr

# delete  certs
echo "cleanup"
rm  keystore/msh.e-box-a.csr
rm  keystore/msh.e-box-b.csr


echo "generate property files"
# generate server e-box-a sign properties
echo "org.apache.ws.security.crypto.provider=org.apache.wss4j.common.crypto.Merlin" 		 > msh_e-box-a_sign.properties
echo "org.apache.ws.security.crypto.merlin.keystore.type=jks" 			    		>> msh_e-box-a_sign.properties
echo "org.apache.ws.security.crypto.merlin.keystore.password=test1234" 	    			>> msh_e-box-a_sign.properties
echo "org.apache.ws.security.crypto.merlin.keystore.alias=msh.e-box-a.si"             		>> msh_e-box-a_sign.properties
echo "org.apache.ws.security.crypto.merlin.keystore.file=keystore/msh.e-box-a-keystore.jks" 	>> msh_e-box-a_sign.properties
# generate server e-box-b sign properties
echo "org.apache.ws.security.crypto.provider=org.apache.wss4j.common.crypto.Merlin" 		 > msh_e-box-b_sign.properties
echo "org.apache.ws.security.crypto.merlin.keystore.type=jks" 			    		>> msh_e-box-b_sign.properties
echo "org.apache.ws.security.crypto.merlin.keystore.password=test1234" 	    			>> msh_e-box-b_sign.properties
echo "org.apache.ws.security.crypto.merlin.keystore.alias=msh.e-box-b.si"             		>> msh_e-box-b_sign.properties
echo "org.apache.ws.security.crypto.merlin.keystore.file=keystore/msh.e-box-b-keystore.jks" 	>> msh_e-box-b_sign.properties
# generate server e-box-a trustotre properties
echo "org.apache.ws.security.crypto.provider=org.apache.wss4j.common.crypto.Merlin" 		 > msh_e-box-a_signVer.properties
echo "org.apache.ws.security.crypto.merlin.keystore.type=jks" 			    		>> msh_e-box-a_signVer.properties
echo "org.apache.ws.security.crypto.merlin.keystore.password=test1234" 	    			>> msh_e-box-a_signVer.properties
echo "org.apache.ws.security.crypto.merlin.keystore.alias=msh.e-box-b.si"             		>> msh_e-box-a_signVer.properties
echo "org.apache.ws.security.crypto.merlin.keystore.file=keystore/msh.e-box-a-truststore.jks" 	>> msh_e-box-a_signVer.properties
# generate server e-box-b trustotre properties
echo "org.apache.ws.security.crypto.provider=org.apache.wss4j.common.crypto.Merlin" 		 > msh_e-box-b_signVer.properties
echo "org.apache.ws.security.crypto.merlin.keystore.type=jks" 			    		>> msh_e-box-b_signVer.properties
echo "org.apache.ws.security.crypto.merlin.keystore.password=test1234" 	    			>> msh_e-box-b_signVer.properties
echo "org.apache.ws.security.crypto.merlin.keystore.alias=msh.e-box-a.si"             		>> msh_e-box-b_signVer.properties
echo "org.apache.ws.security.crypto.merlin.keystore.file=keystore/msh.e-box-b-truststore.jks" 	>> msh_e-box-b_signVer.properties
# generate keypasswords
echo "msh.e-box-a.si=key1234"  > msh_key-passwords.properties
echo "msh.e-box-b.si=key1234" >> msh_key-passwords.properties


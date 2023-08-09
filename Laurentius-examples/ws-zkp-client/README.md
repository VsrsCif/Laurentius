# ZKP Test client

This purpose of this client is to test ZKP implementation.

## Prerequisites

- Have a running Laurentius instance on localhost and port 8080. 
- Disable the ZKPSign plugin in the admin interface (https://localhost:8080/laurentius-web). 
The setting is located under *Settings* > *Scheduler*. Select the *ZKPSign* Cron Job and set the *Active* field to unchecked.
- Do not forget to re-enable the ZKPSign Cron Job after the test is complete
- By law ZKP messages should be deleted after 15 days. To test this, you can change the message expiry time in the cron job settings.
  - There is 1 cron job that need to be changed: *ZKPTaskDeleteUndelivered*
    - First, set the `dev.mode` property to `true`
    - Second, set the `zkp.wait.days` property to `0`
    - Third, set the `zkp.wait.minutes` property to `1` (or any suitable value bigger than 0)
  - The above settings will change how expiry time of the ZKP messages is calculated and will bring it down to 1 minute.
  - After the test is complete, revert the changes to the above properties and re-enable the cron job.

## How to run

Best way to run this test is from an IDE (Intellij Idea preferrably). It can be run via terminal as well, but that process is more involved. 
Mind you, because of longer message expiry times this test can take a while.

### Intellij Idea
Simply open the `WSZKPClientExample` and run it.

### Terminal
If you want to run this test directly from the terminal the process is a bit more involved by adding dependencies to the classpath. 
Suggested approach is to use the command the Idea uses and modify that. An example of the command is:

```
/usr/lib/jvm/java-8-openjdk/bin/java -javaagent:/home/user/Apps/idea-IU-231.9161.38/lib/idea_rt.jar=46233:/home/user/Apps/idea-IU-231.9161.38/bin -Dfile.encoding=UTF-8 -classpath /usr/lib/jvm/java-8-openjdk/jre/lib/charsets.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/cldrdata.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/dnsns.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/jaccess.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/localedata.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/nashorn.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/sunec.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/sunjce_provider.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/sunpkcs11.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/zipfs.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/jce.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/jfr.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/jsse.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/management-agent.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/resources.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/rt.jar:/home/user/Projects/digics/laurentius/Laurentius-examples/ws-zkp-client/target/classes:/home/user/Projects/digics/laurentius/Laurentius-libs/Laurentius-wsdl/target/classes:/home/user/Projects/digics/laurentius/Laurentius-libs/Laurentius-commons/target/classes:/home/user/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar:/home/user/.m2/repository/org/apache/pdfbox/pdfbox/2.0.8/pdfbox-2.0.8.jar:/home/user/.m2/repository/org/apache/pdfbox/fontbox/2.0.8/fontbox-2.0.8.jar:/home/user/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:/home/user/.m2/repository/org/bouncycastle/bcprov-jdk15on/1.56/bcprov-jdk15on-1.56.jar:/home/user/.m2/repository/org/bouncycastle/bcpkix-jdk15on/1.56/bcpkix-jdk15on-1.56.jar:/home/user/.m2/repository/org/apache/xmlgraphics/fop/2.2/fop-2.2.jar:/home/user/.m2/repository/org/apache/xmlgraphics/xmlgraphics-commons/2.2/xmlgraphics-commons-2.2.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-svg-dom/1.9/batik-svg-dom-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-css/1.9/batik-css-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-dom/1.9/batik-dom-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-parser/1.9/batik-parser-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-util/1.9/batik-util-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-constants/1.9/batik-constants-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-i18n/1.9/batik-i18n-1.9.jar:/home/user/.m2/repository/xml-apis/xml-apis/1.3.04/xml-apis-1.3.04.jar:/home/user/.m2/repository/xml-apis/xml-apis-ext/1.3.04/xml-apis-ext-1.3.04.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-bridge/1.9/batik-bridge-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-anim/1.9/batik-anim-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-script/1.9/batik-script-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-xml/1.9/batik-xml-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-awt-util/1.9/batik-awt-util-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-gvt/1.9/batik-gvt-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-transcoder/1.9/batik-transcoder-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-svggen/1.9/batik-svggen-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-extension/1.9/batik-extension-1.9.jar:/home/user/.m2/repository/org/apache/xmlgraphics/batik-ext/1.9/batik-ext-1.9.jar:/home/user/.m2/repository/commons-io/commons-io/1.3.1/commons-io-1.3.1.jar:/home/user/.m2/repository/org/apache/avalon/framework/avalon-framework-api/4.3.1/avalon-framework-api-4.3.1.jar:/home/user/.m2/repository/org/apache/avalon/framework/avalon-framework-impl/4.3.1/avalon-framework-impl-4.3.1.jar si.laurentius.test.zkp.WSZKPClientExample
```
This command should be run from the `/Laurentius-examples/ws-zkp-client/` folder.

## Expected results 

After the test is run in the admin GUI there should be:

- 2 instances of successful ZKP-A scenarios
- 2 instances of successful ZKP-B scenarios
- 2 instances of non-delivery scenarios
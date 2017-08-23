/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.ejb.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import static org.junit.Assert.fail;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.ejb.SEDDaoBeanTest;

/**
 *
 * @author Jože Rihtaršič
 */
public class TestUtils {

  /**
   *
   */
  protected static final String PERSISTENCE_UNIT_NAME = "ebMS_PU";

  /**
   *
   */
  protected static final String LAU_HOME = "target/TEST-LAU_HOME";
  public static final String LAU_TEST_DOMAIN = "test.com";
  
  public static final String S_JMS_JNDI_CF ="java:/jboss/ConnectionFactory";
  public static final String S_JMS_QUEUE ="queue/MSHQueue";

  static {
    if (!Paths.get(LAU_HOME).toFile().exists()) {
      try {
        Files.createDirectory(Paths.get(LAU_HOME));
      } catch (IOException ex) {
        java.util.logging.Logger.getLogger(TestUtils.class.getName()).
                log(java.util.logging.Level.SEVERE, null, ex);
      }
      System.setProperty(SEDSystemProperties.SYS_PROP_HOME_DIR, LAU_HOME);
      System.setProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN,
              LAU_TEST_DOMAIN);
    }
  }

  /**
   *
   * @param fileName
   */
  public static void setLogger(String fileName) {
    // set logger
    ConsoleAppender console = new ConsoleAppender(); // create appender
    // configure the appender
    String PATTERN = "%d [%p|%c|%C{1}] %m%n";
    console.setLayout(new PatternLayout(PATTERN));
    console.setThreshold(Level.WARN);
    console.activateOptions();
    // add appender to any Logger (here is root)
    Logger.getRootLogger().addAppender(console);
    FileAppender fa = new FileAppender();
    fa.setName("FileLogger-" + fileName);
    fa.setFile("target" + File.separator + fileName + ".log");
    fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
    fa.setThreshold(Level.DEBUG);
    fa.setAppend(true);
    fa.activateOptions();
    // add appender to any Logger (here is root)
    Logger.getRootLogger().addAppender(fa);
  }

  public static void setUpStorage(String folder)
          throws IOException {
    System.setProperty(SEDSystemProperties.SYS_PROP_HOME_DIR, folder);

    Path directory = StorageUtils.getStorageFolder().toPath();
    if (Files.exists(directory)) {
      Path p = Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
          Files.deleteIfExists(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }

      });
      if (p == null) {
        fail();
      }
    }
  }

  public static void setupJMS(String jndiFac, String prefix, List<String> lstJndiQueue) {
    EmbeddedActiveMQBroker broker = new EmbeddedActiveMQBroker();
    broker.start();
    
    // Add ConnectionFactory to JNDI
    ConnectionFactory connectionFactory = broker.createConnectionFactory();
    InitialContextFactoryForTest.bind(jndiFac,connectionFactory);
    // create destionation 
    try {
      Connection connection = connectionFactory.createConnection();
      connection.start();   
      for (String jndiQueue: lstJndiQueue) {
      Destination dest = connection.createSession(false,
              Session.AUTO_ACKNOWLEDGE).createQueue(jndiQueue);
      InitialContextFactoryForTest.bind(prefix+ jndiQueue,
              dest);
      }
    } catch (JMSException ex) {
      java.util.logging.Logger.getLogger(SEDDaoBeanTest.class.getName()).log(
              java.util.logging.Level.SEVERE, null,
              ex);
    }
  }
}

/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.ejb;

import java.io.File;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author Jože Rihtaršič
 */
public class TestUtils {

  /**
     *
     */
  protected static final String JNDI_CONNECTION_FACTORY = "ConnectionFactory";
  /**
     *
     */
  protected static final String PERSISTENCE_UNIT_NAME = "ebMS_PU";

  /**
     *
     */
  protected static final String LAU_HOME = "target/TEST-LAU_HOME";



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

}

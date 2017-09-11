/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.task;

import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.plugin.def.DefaultInitData;
import si.laurentius.plugin.def.MenuItem;
import si.laurentius.plugin.interfaces.AbstractPluginDescription;
import si.laurentius.plugin.interfaces.PluginDescriptionInterface;
import si.laurentius.plugin.interfaces.exception.PluginException;

/**
 *
 * @author Jože Rihtaršič
 */

@Singleton
@Startup
@Local(PluginDescriptionInterface.class)
public class ExampleTaskPluginDescription extends  AbstractPluginDescription {

  private static final SEDLogger LOG = new SEDLogger(ExampleTaskPluginDescription.class);
  MenuItem miRoot = null;

  
  @Override
  public DefaultInitData getDefaultInitData() {
    return null;
  }
  @Override
  public MenuItem getMenu() {
    return null;
  }

  @Override
  public MenuItem getProcessMenu() {
    return null;
  }
  
  @PostConstruct
  private void postConstruct() {    
    try {      
      registerPluginComponentInterface(ExampleCronTask.class);    
      // register plugin
      registerPlugin();
    } catch (PluginException ex) {
      LOG.logError("Error occured while registering plugin: " + ex.getMessage(), ex);
    }
  }

  /**
   *
   * @return
   */
  @Override
  public String getDesc() {
    return "Example task plugin descriptor";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  /**
   *
   * @return
   */
  @Override
  public String getName() {
    return "Example tasks plugin";
  }

  /**
   *
   * @return
   */
  @Override
  public String getWebUrlContext() {
    return null;
  }
    /**
     *
     * @return
     */
    @Override
    public List<String> getWebPageRoles() {
        return Collections.emptyList();
    }

 

  /**
   *
   * @return
   */
  @Override
  public String getType() {
    return "BasicTaskPlugin";
  }
}

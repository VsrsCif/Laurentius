/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.vsrs.cif.filing;

import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.bind.JAXBException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
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
public class ECFPluginDescription extends  AbstractPluginDescription {

  private static final SEDLogger LOG = new SEDLogger(ECFPluginDescription.class);
  
  
  @Override
  public DefaultInitData getDefaultInitData() {
    try {
      DefaultInitData did = (DefaultInitData) XMLUtils.deserialize(ECFPluginDescription.class.
              getResourceAsStream("/init/def-init-data.xml"),
              DefaultInitData.class);
      return did;
    } catch (JAXBException ex) {
      LOG.logError("Error parsing default init data!", ex);
    }
    return null;
  }
  @PostConstruct
  private void postConstruct() {    
    try {
      // and log further application specific info
      registerPluginComponentInterface(ECFInInterceptor.class);
      registerPlugin();
    } catch (PluginException ex) {
      LOG.logError("Error occurred while registering plugin: " + ex.getMessage(), ex);
    }
  }
  
   @Override
  public MenuItem getMenu() {
    return null;
  }

  @Override
  public MenuItem getProcessMenu() {
    return null;
  }
  /**
   *
   * @return
   */
  @Override
  public String getDesc() {
    return "Dodatek za elektronsko vlaganje";
  }


  @Override
  public String getVersion() {
    return "0.1.0";
  }


  /**
   *
   * @return
   */
  @Override
  public String getName() {
    return "eOdlozisce";
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
    return getClass().getSimpleName();
  }

}

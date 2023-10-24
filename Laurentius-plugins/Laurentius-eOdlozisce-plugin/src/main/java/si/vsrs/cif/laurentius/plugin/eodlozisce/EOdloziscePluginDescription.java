/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.eodlozisce;

import si.laurentius.commons.pmode.PModeUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.plugin.def.DefaultInitData;
import si.laurentius.plugin.def.MenuItem;
import si.laurentius.plugin.interfaces.AbstractPluginDescription;
import si.laurentius.plugin.interfaces.PluginDescriptionInterface;
import si.laurentius.plugin.interfaces.exception.PluginException;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.bind.JAXBException;
import java.util.Collections;
import java.util.List;

@Singleton
@Startup
@Local(PluginDescriptionInterface.class)
public class EOdloziscePluginDescription extends AbstractPluginDescription {

  private static final SEDLogger LOG = new SEDLogger(
          EOdloziscePluginDescription.class);

  PModeUtils mPMDUtils = new PModeUtils();

  @Override
  public DefaultInitData getDefaultInitData() {
    try {
      DefaultInitData did = (DefaultInitData) XMLUtils.deserialize(
              EOdloziscePluginDescription.class.
                      getResourceAsStream("/init/def-init-data.xml"),
              DefaultInitData.class);

      return did;
    } catch (JAXBException ex) {
      LOG.logError("Error parsing default init data!", ex);
    }
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
      registerPluginComponentInterface(EOdlozisceInInterceptor.class);
      registerPluginComponentInterface(EOdlozisceOutInterceptor.class);

      registerPluginComponentInterface(EOdlozisceTask.class);
      registerPluginComponentInterface(ECFEVIPTask.class);

      registerPlugin();
    } catch (PluginException ex) {
      LOG.logError("Error occured while registering plugin: " + ex.
              getMessage(), ex);
    }
  }

  /**
   *
   * @return
   */
  @Override
  public String getDesc() {
    // TODO get proper plugin description
    return "eOdlozisce";
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
    return "eOdlozisce plugin";
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
    return EOdlozisceConstants.EODLOZISCE_PLUGIN_TYPE;
  }

}

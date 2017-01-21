/*
 * Copyright 2016, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.jrc.msh.plugin.example;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import si.laurentius.commons.SEDGUIConstants;
import si.laurentius.commons.utils.SEDLogger;
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
public class ExampleWebPluginDescription extends  AbstractPluginDescription {
  
  private static final SEDLogger LOG = new SEDLogger(ExampleWebPluginDescription.class);
  
  @PostConstruct
  private void postConstruct() {    
    try {
      // and log further application specific info
      registerPluginComponentInterface(ExampleSoapInterceptor.class);
      registerPluginComponentInterface(ExampleWebCronTask.class);
      
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
    return "This is example of plugin with a web administration";
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
    return "Example web plugin";
  }

  /**
   *
   * @return
   */
  @Override
  public String getWebUrlContext() {
    return "/laurentius-web/example-web-plugin";
  }
  
  /**
     *
     * @return
     */
    @Override
    public List<String> getWebPageRoles() {
        return Arrays.asList(SEDGUIConstants.ROLE_USER, SEDGUIConstants.ROLE_ADMIN);
    }

  /**
   *
   * @return
   */
  @Override
  public String getType() {
    return "ExampleWebPlugin";
  }

}

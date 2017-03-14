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
package si.jrc.msh.plugin.tc;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import si.jrc.msh.plugin.tc.web.AppConstant;
import si.laurentius.commons.SEDGUIConstants;
import si.laurentius.commons.utils.SEDLogger;
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
public class TestCasePluginDescription extends AbstractPluginDescription {

  private static final SEDLogger LOG = new SEDLogger(
          TestCasePluginDescription.class);
  MenuItem miRoot = null;

  @PostConstruct
  private void postConstruct() {
    try {
      // and log further application specific info
      registerPluginComponentInterface(TestCaseInInterceptor.class);
      registerPluginComponentInterface(TestCaseOutInterceptor.class);
      // register plugin
      registerPlugin();
    } catch (PluginException ex) {
      LOG.logError("Error occured while registering plugin: " + ex.getMessage(),
              ex);
    }
  }

  /**
   *
   * @return
   */
  @Override
  public String getDesc() {
    return "Test case ";
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
    return "Test case plugin";
  }

  /**
   *
   * @return
   */
  @Override
  public String getWebUrlContext() {
    return "/laurentius-web/testcase-plugin";
  }

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
    return "TestCasePlugin";
  }

  @Override
  public MenuItem getMenu() {
    if (miRoot == null) {
      miRoot = new MenuItem();
      miRoot.setName(getName());

      //
      MenuItem stressTest = new MenuItem();
      stressTest.setName("Stress test");
      stressTest.setPageId(AppConstant.S_PANEL_STRESS_TEST);

      MenuItem fictionTest = new MenuItem();
      fictionTest.setName("Fiction test");
      fictionTest.setPageId(AppConstant.S_PANEL_FICTION_TEST);

      MenuItem customTest = new MenuItem();
      customTest.setName("Custom test");
      customTest.setPageId(AppConstant.S_PANEL_CUSTOM_TEST);

      MenuItem reliabilityTest = new MenuItem();
      reliabilityTest.setName("Reliability test");
      reliabilityTest.setPageId(AppConstant.S_PANEL_RELIABILITY_TEST);

      miRoot.getMenuItems().add(stressTest);
      miRoot.getMenuItems().add(fictionTest);
      miRoot.getMenuItems().add(customTest);
      miRoot.getMenuItems().add(reliabilityTest);
    }

    return miRoot;

  }

  @Override
  public MenuItem getProcessMenu() {
    return null;
  }

}

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

import java.util.Collections;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.commons.interfaces.PluginDescriptionInterface;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(PluginDescriptionInterface.class)
public class TestCasePluginDescription implements PluginDescriptionInterface {

  /**
   *
   * @return
   */
  @Override
  public String getDesc() {
    return "Test case ";
  }

    @Override
    public List<String> getJNDIInEventInterceptors() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getJNDIInFaultInterceptors() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getJNDIInInterceptors() {
       return Collections.emptyList();
    }

    @Override
    public List<String> getJNDIOutEventInterceptors() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getJNDIOutFaultInterceptors() {
      return Collections.emptyList();
    }



    @Override
    public List<String> getJNDIOutInterceptors() {
      return Collections.singletonList("java:global/plugin-zpp/ZPPOutInterceptor!si.laurentius.commons.interfaces.SoapInterceptorInterface");
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
  public String getSettingUrlContext() {
    return "/laurentius-web/testcase-plugin";
  }

  /**
   *
   * @return
   */
  @Override
  public List<String> getTaskJNDIs() {
    return Collections
        .singletonList("java:global/plugin-zpp/ZPPTask!si.laurentius.commons.interfaces.TaskExecutionInterface");
  }

  /**
   *
   * @return
   */
  @Override
  public String getType() {
    return "TestCasePlugin";
  }

}

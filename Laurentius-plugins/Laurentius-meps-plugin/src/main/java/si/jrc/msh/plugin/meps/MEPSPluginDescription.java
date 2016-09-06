/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.meps;

import java.util.Collections;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.commons.interfaces.PluginDescriptionInterface;

/**
 *
 * @author sluzba
 */
@Stateless
@Local(PluginDescriptionInterface.class)
public class MEPSPluginDescription implements PluginDescriptionInterface {

  /**
   *
   * @return
   */
  @Override
  public String getDesc() {
    return "";
  }

  /**
   *
   * @return
   */
  @Override
  public String getJNDIInInterceptor() {
    return "java:global/plugin-meps/MEPSOutInterceptor!si.laurentius.commons.interfaces.SoapInterceptorInterface";
  }

  /**
   *
   * @return
   */
  @Override
  public String getJNDIOutInterceptor() {
    return "java:global/plugin-meps/MEPSOutInterceptor!si.laurentius.commons.interfaces.SoapInterceptorInterface";
  }

  /**
   *
   * @return
   */
  @Override
  public String getName() {
    return "MEPS-plugin";
  }

  /**
   *
   * @return
   */
  @Override
  public String getSettingUrlContext() {
    return "/meps";
  }

  /**
   *
   * @return
   */
  @Override
  public List<String> getTaskJNDIs() {
    return Collections
        .singletonList("java:global/plugin-meps/MEPSTask!si.laurentius.commons.interfaces.TaskExecutionInterface");
  }

  /**
   *
   * @return
   */
  @Override
  public String getType() {
    return "MEPSPlugin";
  }

}

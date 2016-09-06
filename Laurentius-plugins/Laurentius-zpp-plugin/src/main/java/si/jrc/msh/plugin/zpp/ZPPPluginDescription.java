/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.zpp;

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
public class ZPPPluginDescription implements PluginDescriptionInterface {

  /**
   *
   * @return
   */
  @Override
  public String getDesc() {
    return "ZPP - e-delivery: SVEV 2.0 service implementation";
  }

  /**
   *
   * @return
   */
  @Override
  public String getJNDIInInterceptor() {
    return "java:global/plugin-zpp/ZPPOutInterceptor!si.laurentius.commons.interfaces.SoapInterceptorInterface";
  }

  /**
   *
   * @return
   */
  @Override
  public String getJNDIOutInterceptor() {
    return "java:global/plugin-zpp/ZPPOutInterceptor!si.laurentius.commons.interfaces.SoapInterceptorInterface";
  }

  /**
   *
   * @return
   */
  @Override
  public String getName() {
    return "ZPP plugin";
  }

  /**
   *
   * @return
   */
  @Override
  public String getSettingUrlContext() {
    return "/zpp-plugin";
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
    return ZPPConstants.S_ZPP_PLUGIN_TYPE;
  }

}

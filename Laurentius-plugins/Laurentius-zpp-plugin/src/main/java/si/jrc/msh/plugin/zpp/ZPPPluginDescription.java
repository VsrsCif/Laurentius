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
 * @author Jože Rihtaršič
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

    @Override
    public List<String> getJNDIInEventInterceptors() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getJNDIInFaultInterceptors() {
      return Collections.emptyList();
    }



    @Override
    public List<String> getJNDIInInterceptors() {
      return Collections.singletonList("java:global/plugin-zpp/ZPPOutInterceptor!si.laurentius.commons.interfaces.SoapInterceptorInterface");
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
    return "ZPP plugin";
  }

  /**
   *
   * @return
   */
  @Override
  public String getSettingUrlContext() {
    return "/laurentius-web/zpp-plugin";
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

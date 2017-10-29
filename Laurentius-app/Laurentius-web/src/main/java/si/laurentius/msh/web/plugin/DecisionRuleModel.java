/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.plugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sluzba
 */
public class DecisionRuleModel implements Serializable {

  private final List<DecisionRuleModelItem> pluginProperties = new ArrayList<>();

  /**
   *
   */
  public void clear() {
    pluginProperties.clear();
  }

  public void setPluginProperties(Map<String, String> tpv, List lstPropDef) {
    clear();    
    /*
    lstPropDef.forEach((obj) -> {
      DecisionRuleDef stp = (DecisionRuleDef)obj;
      String key = stp.getKey();
      pluginProperties.add(new DecisionRuleModelItem((DecisionRuleDef)stp, tpv.
              getOrDefault(key, stp.getDefValue())));
    });*/
  }

  /**
   *
   * @return
   */
  public List<DecisionRuleModelItem> getPluginProperties() {
    return pluginProperties;
  }
  
   
  
}

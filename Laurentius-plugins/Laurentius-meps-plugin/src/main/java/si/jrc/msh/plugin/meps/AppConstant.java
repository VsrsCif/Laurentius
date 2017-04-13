/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.meps;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author Jože Rihtaršič
 */
@ApplicationScoped
@ManagedBean(name = "AppConstant")
public class AppConstant {

  
  /**
   *
   */
  public static final String S_PANEL_TEST = "S_PANEL_TEST";
  
  public static final String PLUGIN_NAME = "MEPS";
  
  

  public String getS_PANEL_TEST() {
    return S_PANEL_TEST;
  }
}
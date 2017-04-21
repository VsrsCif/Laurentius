/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.mju.plugin;

import si.laurentius.commons.SEDSystemProperties;

/**
 *
 * @author sluzba
 */
public class AppConstants {
  
  public static final String PLUGIN_NAME ="mju-svev1";
  public static final String PLUGIN_FOLDER ="mju";
  public static final String PLUGIN_ROOT_FOLDER = String.format("${%s}/%s/",
          SEDSystemProperties.SYS_PROP_PLUGINS_DIR,
          AppConstants.PLUGIN_FOLDER);
  
}

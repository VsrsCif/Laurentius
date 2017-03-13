/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.plg.web;

import java.io.File;
import si.laurentius.commons.SEDSystemProperties;

/**
 *
 * @author sluzba
 */
public class PlgSystemProperties {

  public static final String SYS_ROOT_FOLDER_NAME = "base-plugin";
  public static final String SYS_XSLT_FOLDER_NAME = "xslt";
  public static final String SYS_SCHEMA_FOLDER_NAME = "schema";

  private static File F_FOLDER_PLUGIN = null;
  private static File F_XSLT_PLUGIN = null;
  private static File F_SCHEMA_PLUGIN = null;

  public static synchronized File getPluginFolder() {
    if (F_FOLDER_PLUGIN == null) {
      F_FOLDER_PLUGIN = new File(SEDSystemProperties.getPluginsFolder(),
              SYS_ROOT_FOLDER_NAME);
      if (!F_FOLDER_PLUGIN.exists()) {
        F_FOLDER_PLUGIN.mkdirs();
      }
    }
    return F_FOLDER_PLUGIN;
  }

  public static synchronized File getXSLTFolder() {
    if (F_XSLT_PLUGIN == null) {
      F_XSLT_PLUGIN = new File(getPluginFolder(),
              SYS_XSLT_FOLDER_NAME);
      if (!F_XSLT_PLUGIN.exists()) {
        F_XSLT_PLUGIN.mkdirs();
      }
      
    }
    return F_XSLT_PLUGIN;
  }
  public static synchronized File getSchemaFolder() {
    if (F_SCHEMA_PLUGIN == null) {
      F_SCHEMA_PLUGIN = new File(getPluginFolder(),
              SYS_SCHEMA_FOLDER_NAME);
      if (!F_SCHEMA_PLUGIN.exists()) {
        F_SCHEMA_PLUGIN.mkdirs();
      }      
    }
    return F_SCHEMA_PLUGIN;
  }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.vsrs.cif.filing;

import si.laurentius.commons.SEDSystemProperties;

import java.io.File;

/**
 *
 * @author sluzba
 */
public class ECFSystemProperties {

  public static final String ECF_FOLDER_NAME_PLUGIN = "ecf-plugin";
  public static final String ECF_FOLDER_NAME_DATA = "data";

  public static final String ECF_FILENAME_COURTS = "sifrant-sodisca.json";
  public static final String ECF_FILENAME_REGISTER = "sifrant-vpisniki.json";
  public static final String ECF_FILENAME_FIEDS_OF_LAW = "sifrant-pravna-podrocja.json";



  private static File F_FOLDER_PLUGIN = null;
    private static File F_SCHEMA_PLUGIN = null;

  public static synchronized File getPluginFolder() {
    if (F_FOLDER_PLUGIN == null) {
      F_FOLDER_PLUGIN = new File(SEDSystemProperties.getPluginsFolder(),
              ECF_FOLDER_NAME_PLUGIN);
      if (!F_FOLDER_PLUGIN.exists()) {
        F_FOLDER_PLUGIN.mkdirs();
      }
    }
    return F_FOLDER_PLUGIN;
  }

  public static synchronized File getDataFolder() {
    if (F_SCHEMA_PLUGIN == null) {
      F_SCHEMA_PLUGIN = new File(getPluginFolder(),
              ECF_FOLDER_NAME_DATA);
      if (!F_SCHEMA_PLUGIN.exists()) {
        F_SCHEMA_PLUGIN.mkdirs();
      }      
    }
    return F_SCHEMA_PLUGIN;
  }

  public static  File getDataFile(String fileName) {
      return new File(getDataFolder(),fileName);
  }

}

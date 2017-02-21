/*
 * Copyright 2015, Supreme Court Republic of Slovenia
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
package si.laurentius.commons.utils.abst;

import java.io.File;
import static java.io.File.separator;
import static java.lang.System.getProperties;
import static java.util.Collections.enumeration;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;
import si.laurentius.commons.utils.SEDLogger;
import static java.lang.System.getProperty;

/**
 * Abstract settings class
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public abstract class ASettings {
  /**
   * Logger
   */
  protected static final SEDLogger LOG = new SEDLogger(ASettings.class);

  /**
   * Retruns Creates sorted properties by keys.
   *
   * @return - Sorted properties by key.
   */
  public static Properties newProperties() {
    return new Properties() {
      @Override
      public synchronized Enumeration<Object> keys() {
        return enumeration(new TreeSet<Object>(super.keySet()));
      }
    };
  }

  /**
   * Last changed properties
   */
  protected long mlLastChagedTime = 0;


  /**
   * SED Properties
   */
  final protected Properties mprpProperties = newProperties();

  /**
   *
   */
  public ASettings() {

  }

  /**
   * Method returs data for key. Searches for the property value in system properties and this
   * property list. If the key is not found in system property list, the this property list is
   * checked. The method returns null if the property is not found.
   *
   * @param strKey - propert key
   * @return data -value for property
   */
  public String getData(String strKey) {
    return getData(strKey, null);
  }

  /**
   * Method returs data for key. Searches for the property value in system properties and this
   * property list. If the key is not found in system property list, the this property list is
   * checked. The method returns def value if the property is not found.
   *
   * @param strKey - propert key
   * @param defVal - if no property found this value is returned.
   * @return data -value for property
   */
  public String getData(String strKey, String defVal) {
    // check if system property exists
    if (getProperties().containsKey(strKey)) {
      return getProperty(strKey);
    }
    init();
    return mprpProperties.getProperty(strKey, defVal);
  }

  /**
   * Returns File object in folder give in valus for key strPropName. File is [folder:
   * ${strPropName}]/strFileName. If folder not exists, folder is created
   *
   * @param strPropName
   * @param strDefProfValue
   * @param strFileName
   * @return
   */
  public File getFile(String strPropName, String strDefProfValue, String strFileName) {
    long l = LOG.getTime();
    File f = getFolder(strPropName, strDefProfValue);
    if (f.exists()){
      LOG.logError(l, "Error creating folders: '" + f.getAbsolutePath() + "' for file: '"+strFileName+"'", null);
    }
    return new File(f.getAbsolutePath() + separator + strFileName);
  }

  /**
   *
   * @param prop
   * @param defVal
   * @return
   */
  public File getFolder(String prop, String defVal) {
    long l = LOG.getTime();
    File f = new File(getProperty(prop, getData(prop, defVal)));
     if (!f.exists() && !f.mkdirs()) {
      LOG.logError(l, "Error creating folders: '" + f.getAbsolutePath() + "'", null);
    }
    return f;
  }

  /**
   * Abstract method for initialization and refresh property data. 
   */
  protected abstract void init();

  /**
   * Sets init data for key
   * @param key - property key 
   * @param value - property ataa
   */
  public void initData(String key, String value) {
    if (!mprpProperties.containsKey(key)) {
      mprpProperties.setProperty(key, value);
    }
  }

  /**
   * initialize folders and lookups
   */
  public abstract void initialize();

  /**
   * Remove property in serialized form (DB or file)
   * @param key property key
   */
  protected abstract void removeProperty(String key);

  /**
   * Replace property in serialized form (DB or file)
   * @param key
   * @param value
   * @param group
   */
  protected abstract void replaceProperty(String key, String value, String group);

  /**
   * Set data 
   * @param key 
   * @param value
   */
  public void setData(String key, String value) {
    setData(key, value, null);
  }

  /**
   * Set data for group
   * @param key
   * @param value
   * @param group
   */
  public void setData(String key, String value, String group) {
    if (key == null || key.trim().isEmpty()) {
      return;
    }
    String strKey = key.trim();
    String strValue = value != null ? value.trim() : null;

    init();

    if (mprpProperties.containsKey(strKey)) {
      if (strValue == null) {
        mprpProperties.remove(strKey);
        removeProperty(strKey);
      } else if (mprpProperties.get(strKey) != null && !mprpProperties.get(strKey).equals(strValue)) {
        mprpProperties.setProperty(strKey, strValue);
        replaceProperty(strKey, strValue, group);
      }
    } else if (strValue != null) {
      mprpProperties.setProperty(strKey, strValue);
      storeProperty(strKey, strValue, group);
    }
  }

  /**
   * Strore new property to DB, file etc.
   * @param key
   * @param value
   * @param group
   */
  protected abstract void storeProperty(String key, String value, String group);


}

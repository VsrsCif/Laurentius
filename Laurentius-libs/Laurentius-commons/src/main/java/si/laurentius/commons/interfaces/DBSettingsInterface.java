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
package si.laurentius.commons.interfaces;

import java.util.List;
import java.util.Properties;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import si.laurentius.property.SEDProperty;

/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface DBSettingsInterface {
  
  public static final String SYSTEM_SETTINGS = "SYSTEM";
  public static final String LAU_SETTINGS = "SED";

  /**
   *
   * @param prps
   */
  @Lock(value = LockType.READ)
  void setSEDProperties(List<SEDProperty> prps);

    /**
   *
   * @param key
   * @param value
   * @param group
   */
  @Lock(value = LockType.WRITE)
  void setSEDProperty(String key, String value, String group);
  
     /**
   *
   * @param key
   * @param value
   * @param group
   */
  @Lock(value = LockType.WRITE)
  void removeSEDProperty(String key, String group);
  /**
   * 
   * 
   *
   * @return
   */
  List<SEDProperty> getSEDProperties();
    @Lock(value = LockType.WRITE)
  SEDProperty getSEDProperty(String key, String group);



  boolean isNetworkConnected();

}

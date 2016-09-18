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

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import javax.ejb.Local;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTaskType;
import si.laurentius.ebox.SEDBox;
import si.laurentius.plugin.SEDPlugin;
import si.laurentius.user.SEDUser;

/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface SEDLookupsInterface {

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDBox(SEDBox sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDCertStore(SEDCertStore sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDCronJob(SEDCronJob sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDPlugin(SEDPlugin sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDTaskType(SEDTaskType sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDUser(SEDUser sb);

  /**
   *
   * @param f
   * @param saveCertPasswords
   */
  void exportLookups(File f, boolean saveCertPasswords);

  /**
   *
   * @param strname
   * @return
   */
  SEDBox getSEDBoxByAddressName(String strname);

   /**
   *
   * @param strname
   * @return
   */
  SEDBox getSEDBoxByLocalName(String strname);
  /**
   *
   * @param id
   * @return
   */
  SEDCronJob getSEDCronJobById(BigInteger id);

  /**
   *
   * @param type
   * @return
   */
  SEDTaskType getSEDTaskTypeByType(String type);

  /**
   *
   * @param userId
   * @return
   */
  SEDUser getSEDUserByUserId(String userId);

  /**
   *
   * @param type
   * @return
   */
  SEDPlugin getSEDPluginByType(String type);

  /**
   *
   * @return
   */
  List<SEDBox> getSEDBoxes();

  /**
   * Return list of registred key/trust stores
   * @return List of SEDCertStores
   */
  List<SEDCertStore> getSEDCertStore();
  
  /**
   * Method return SEDCertStore by sed id.
   * @param id Certificate store name
   * @throw IllegalArgumentException if storname is null or empty
   * @return SEDCertStore if not found return null
   */
  public SEDCertStore getSEDCertStoreById(BigInteger id);
  
  /**
   * Method return SEDCertStore by name.
   * @param storeName Certificate store name
   * @throw IllegalArgumentException if storname is null or empty
   * @return SEDCertStore if not found return null
   */
  public SEDCertStore getSEDCertStoreByName(String storeName);

  

  /**
   * MEthod resturs SEDCertificat object for alias
   *
   * @param alias - alias of certificate
   * @param cs - key/trustostre
   * @param isKey - returned SEDCertificate must be a key
   * @throw IllegalArgumentException if alias or  keystore are null
   * @return SEDCertificate or null in SEDCertificate is found givem store.
   */
   SEDCertificate getSEDCertificatForAlias(String alias,
      SEDCertStore cs, boolean isKey);
   
   /**
   * MEthod resturs SEDCertificat object for alias
   *
   * @param alias - alias of certificate
   * @param storeName - trustore or keystore name
   * @param isKey - returned SEDCertificate must be a key
   * @throw IllegalArgumentException if alias or  keystore are null
   * @return SEDCertificate or null in SEDCertificate is found givem store.
   */
   SEDCertificate getSEDCertificatForAlias(String alias,
      String storeName, boolean isKey);
   
   
  /**
   *
   * @return
   */
  List<SEDCronJob> getSEDCronJobs();

  /**
   *
   * @return
   */
  List<SEDPlugin> getSEDPlugin();

  /**
   *
   * @return
   */
  List<SEDTaskType> getSEDTaskTypes();

  /**
   *
   * @return
   */
  List<SEDUser> getSEDUsers();

  /**
   *
   * @param sb
   * @return
   */
  boolean removeSEDBox(SEDBox sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean removeEDCertStore(SEDCertStore sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean removeSEDCronJob(SEDCronJob sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean removeSEDPlugin(SEDPlugin sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean removeSEDTaskType(SEDTaskType sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean removeSEDUser(SEDUser sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDBox(SEDBox sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDCertStore(SEDCertStore sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDCronJob(SEDCronJob sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDPlugin(SEDPlugin sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDTaskType(SEDTaskType sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDUser(SEDUser sb);
}

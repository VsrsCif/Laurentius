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

import java.math.BigInteger;
import java.util.List;
import javax.ejb.Local;
import si.laurentius.application.SEDApplication;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.ebox.SEDBox;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessor;
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
  boolean addSEDCronJob(SEDCronJob sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDUser(SEDUser sb);
  
  boolean addSEDApplication(SEDApplication sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDProcessor(SEDProcessor sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDInterceptor(SEDInterceptor sb);

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
   * @param id
   * @return
   */
  SEDProcessor getSEDProcessor(BigInteger id);

  SEDInterceptor getSEDInterceptorById(BigInteger id);

  /**
   *
   * @param name
   * @return
   */
  SEDCronJob getSEDCronJobByName(String name);

  
  /**
   *
   * @param name
   * @return
   */
  SEDInterceptor getSEDInterceptorByName(String name);
  SEDProcessor getSEDProcessorByName(String name);
  
  /**
   *
   * @param userId
   * @return
   */
  SEDUser getSEDUserByUserId(String userId);
  
  SEDApplication getSEDApplicationById(String id);

  /**
   *
   * @return
   */
  List<SEDBox> getSEDBoxes();


  List<SEDProcessor> getSEDProcessors();
  

  List<SEDInterceptor> getSEDInterceptors();

  /**
   *
   * @return
   */
  List<SEDCronJob> getSEDCronJobs();

  /**
   *
   * @return
   */
  List<SEDUser> getSEDUsers();
  
  List<SEDApplication> getSEDApplications();
  


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
  boolean removeSEDCronJob(SEDCronJob sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean removeSEDUser(SEDUser sb);
  
  boolean removeSEDApplication(SEDApplication sb);
  
  



  /**
   *
   * @param sb
   * @return
   */
  boolean removeSEDProcessor(SEDProcessor sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean removeSEDInterceptor(SEDInterceptor sb);

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
  boolean updateSEDCronJob(SEDCronJob sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDUser(SEDUser sb);
  
  boolean updateSEDApplication(SEDApplication sb);
  
  

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDProcessor(SEDProcessor sb);
  boolean updateSEDProcessorInstance(SEDProcessorInstance sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDInterceptor(SEDInterceptor sb);
  
  void clearAllCache();
  void clearCache(Class cls);
  

}

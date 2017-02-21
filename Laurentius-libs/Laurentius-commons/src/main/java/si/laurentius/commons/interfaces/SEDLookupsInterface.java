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
import si.laurentius.cron.SEDCronJob;
import si.laurentius.ebox.SEDBox;
import si.laurentius.process.SEDProcessorRule;
import si.laurentius.process.SEDProcessorSet;
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

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDProcessorSet(SEDProcessorSet sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean addSEDProcessorRule(SEDProcessorRule sb);



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
  SEDProcessorSet getSEDProcessorSet(String code);

  /**
   *
   * @param id
   * @return
   */
  SEDProcessorRule getSEDProcessorRule(BigInteger id);

  /**
   *
   * @param name
   * @return
   */
  SEDCronJob getSEDCronJobByName(String name);

  /**
   *
   * @param userId
   * @return
   */
  SEDUser getSEDUserByUserId(String userId);

  /**
   *
   * @return
   */
  List<SEDBox> getSEDBoxes();

  List<SEDProcessorSet> getSEDProcessorSets();

  List<SEDProcessorRule> getSEDProcessorRules();

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

  /**
   *
   * @param sb
   * @return
   */
  boolean removeSEDProcessorSet(SEDProcessorSet sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean removeSEDProcessorRule(SEDProcessorRule sb);

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

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDProcessorSet(SEDProcessorSet sb);

  /**
   *
   * @param sb
   * @return
   */
  boolean updateSEDProcessorRule(SEDProcessorRule sb);
}

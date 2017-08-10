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
package si.laurentius.ejb;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import si.laurentius.report.SEDReportBoxStatus;
import si.laurentius.report.Status;
import si.laurentius.commons.interfaces.SEDReportInterface;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(SEDReportInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SEDReportBean implements SEDReportInterface {

  /**
   *
   */
  @PersistenceContext(unitName = "ebMS_LAU_PU", name = "ebMS_LAU_PU")
  public EntityManager memEManager;
  /**
   *
   */
  @Resource
  public UserTransaction mutUTransaction;

  /**
   *
   * @param strSedBox
   * @param fromDateChanged
   * @param toDateChanged
   * @param inStatuses
   * @param services
   * @param outStatuses
   * @return
   */
  @Override
  public SEDReportBoxStatus getReportForStatusChangeInterval(List<String> lstSedBoxes,
          Date fromDateChanged, Date toDateChanged, List<String> inStatuses,
          List<String> outStatuses, List<String> services) {
    SEDReportBoxStatus rbs = new SEDReportBoxStatus();
    
    rbs.setReportDate(Calendar.getInstance().getTime());
    rbs.setOutMail(new SEDReportBoxStatus.OutMail());
    rbs.setInMail(new SEDReportBoxStatus.InMail());
    
    if (lstSedBoxes == null || lstSedBoxes.isEmpty()){
      return rbs;
    }

    if (inStatuses != null && !inStatuses.isEmpty()) {

      TypedQuery<Status> tqIn
              = memEManager.createNamedQuery(
                      "si.laurentius.report.getInMailStatusesByBox",
                      Status.class);

      tqIn.setParameter("sedBoxes", lstSedBoxes);
      tqIn.setParameter("services", services);
      tqIn.setParameter("fromDateChanged", fromDateChanged);
      tqIn.setParameter("toDateChanged", toDateChanged);
      tqIn.setParameter("statuses", inStatuses);
      rbs.getInMail().getStatuses().addAll(tqIn.getResultList());

    }

    if (outStatuses != null && !outStatuses.isEmpty()) {

      TypedQuery<Status> tqOut
              = memEManager.createNamedQuery(
                      "si.laurentius.report.getOutMailStatusesByBox",
                      Status.class);

      tqOut.setParameter("sedBoxes", lstSedBoxes);
      tqOut.setParameter("services", services);
      tqOut.setParameter("fromDateChanged", fromDateChanged);
      tqOut.setParameter("toDateChanged", toDateChanged);
      tqOut.setParameter("statuses", outStatuses);
      rbs.getOutMail().getStatuses().addAll(tqOut.getResultList());
    }
    return rbs;
  }

   @Override
  public SEDReportBoxStatus getReportForAddMailnterval(List<String> lstSedBoxes,
          Date fromAddMailDate, Date toAddMailDate, List<String> inStatuses,
          List<String> outStatuses, List<String> services) {
    SEDReportBoxStatus rbs = new SEDReportBoxStatus();
    
    rbs.setReportDate(Calendar.getInstance().getTime());
    rbs.setOutMail(new SEDReportBoxStatus.OutMail());
    rbs.setInMail(new SEDReportBoxStatus.InMail());
    
    if (lstSedBoxes == null || lstSedBoxes.isEmpty()){
      return rbs;
    }

    if (inStatuses != null && !inStatuses.isEmpty()) {

      TypedQuery<Status> tqIn
              = memEManager.createNamedQuery(
                      "si.laurentius.report.getInMailStatusesByBoxAndReceivedDate",
                      Status.class);

      tqIn.setParameter("sedBoxes", lstSedBoxes);
      tqIn.setParameter("services", services);
      tqIn.setParameter("fromReceivedDate", fromAddMailDate);
      tqIn.setParameter("toReceivedDate", toAddMailDate);
      tqIn.setParameter("statuses", inStatuses);
      rbs.getInMail().getStatuses().addAll(tqIn.getResultList());

    }

    if (outStatuses != null && !outStatuses.isEmpty()) {

      TypedQuery<Status> tqOut
              = memEManager.createNamedQuery(
                      "si.laurentius.report.getInMailStatusesByBoxAndSubmittedDate",
                      Status.class);

      tqOut.setParameter("sedBoxes", lstSedBoxes);
      tqOut.setParameter("services", services);
      tqOut.setParameter("fromSubmittedDate", fromAddMailDate);
      tqOut.setParameter("toSubmittedDate", toAddMailDate);
      tqOut.setParameter("statuses", outStatuses);
      rbs.getOutMail().getStatuses().addAll(tqOut.getResultList());
    }
    return rbs;
  }

   
}

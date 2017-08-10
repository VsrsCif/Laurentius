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

import java.util.Date;
import java.util.List;
import javax.ejb.Local;
import si.laurentius.report.SEDReportBoxStatus;

/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface SEDReportInterface {

  /**
   *
   * @param strSedBox
   * @param fromDateChanged
   * @param toDateChanged
   * @param inStatuses
   * @param outStatuses
   * @param services
   * @return
   */
    SEDReportBoxStatus getReportForStatusChangeInterval(List<String> strSedBox, Date fromDateChanged, Date toDateChanged, List<String> inStatuses,  
          List<String> outStatuses, List<String> services);
    
     SEDReportBoxStatus getReportForAddMailnterval(List<String> strSedBox, Date fromAddMailDate, Date toAddMailDate, List<String> inStatuses,  
          List<String> outStatuses, List<String> services);
    
    
}

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
package si.jrc.msh.db.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import si.laurentius.commons.ebms.EBMSError;

/**
 *
 * @author Jože Rihtaršič
 */
public class EBMSSignal {

  boolean AS4ResponseValid;

  List<EBMSError> errors = new ArrayList<>();
  String refToMessageId;
  Date signalDate;
  Date signalReceivedDate;

  /**
   *
   * @return
   */
  public List<EBMSError> getErrors() {
    return errors;
  }

  /**
   *
   * @return
   */
  public String getRefToMessageId() {
    return refToMessageId;
  }

  /**
   *
   * @return
   */
  public Date getSignalDate() {
    return signalDate;
  }

  /**
   *
   * @return
   */
  public Date getSignalReceivedDate() {
    return signalReceivedDate;
  }

  /**
   *
   * @return
   */
  public boolean isAS4ResponseValid() {
    return AS4ResponseValid;
  }

  /**
   *
   * @param isAS4ResponseValid
   */
  public void setAS4ResponseValid(boolean isAS4ResponseValid) {
    this.AS4ResponseValid = isAS4ResponseValid;
  }

  /**
   *
   * @param refToMessageId
   */
  public void setRefToMessageId(String refToMessageId) {
    this.refToMessageId = refToMessageId;
  }

  /**
   *
   * @param signalDate
   */
  public void setSignalDate(Date signalDate) {
    this.signalDate = signalDate;
  }

  /**
   *
   * @param signalReceivedDate
   */
  public void setSignalReceivedDate(Date signalReceivedDate) {
    this.signalReceivedDate = signalReceivedDate;
  }

  enum SignalType {
    SvevKeySignal, ErrorSignal, AS4ResponseSignal
  }

}

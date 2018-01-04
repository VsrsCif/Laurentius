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
package si.jrc.msh.interceptor;

import java.util.ArrayList;
import java.util.List;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.Phase;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import si.jrc.msh.utils.EBMSValidation;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.lce.KeystoreUtils;

/**
 *
 * @author Jože Rihtaršič
 */
public class EBMSInFaultInterceptor extends AbstractEBMSInterceptor {

  static final SEDLogger LOG = new SEDLogger(EBMSInFaultInterceptor.class);

  /**
   * Keystore tools
   */
  private final KeystoreUtils mKSUtis = new KeystoreUtils();
  final EBMSValidation mebmsValidation = new EBMSValidation();

  /**
   *
   */
  public EBMSInFaultInterceptor() {
    super(Phase.PRE_PROTOCOL); // user preprotocol for generating receipt
    getAfter().add(EBMSInInterceptor.class.getName());
  }

  /**
   *
   *
   * /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg) {
    long l = LOG.logStart();
    LOG.
            log("SoapMessage: ********************************************************");
    msg.entrySet().stream().forEach((entry) -> {
      LOG.formatedlog("Key: %s, val: %s", entry.getKey(), entry.getValue());
    });
    LOG.
            log("Exchange: ********************************************************");
    Exchange map = msg.getExchange();
    map.entrySet().stream().forEach((entry) -> {
      LOG.formatedlog("Key: %s, val: %s", entry.getKey(), entry.getValue());
    });

    // validate soap request and retrieve messaging
    Messaging msgHeader = mebmsValidation.vaildateHeader_Messaging(msg,
            SoapFault.FAULT_CODE_CLIENT);

    if (msgHeader != null
            && msgHeader.getSignalMessages() != null) {
      List<org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Error> errLst = new ArrayList<>();

      for (SignalMessage sm : msgHeader.getSignalMessages()) {
        if (sm.getErrors() != null && !sm.getErrors().isEmpty()) {
          errLst.addAll(sm.getErrors());
        }
      }
      SoapUtils.setInErrors(errLst, msg);

    }

    LOG.logEnd(l);
  }

}

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

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.Phase;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.sec.KeystoreUtils;

/**
 *
 * @author sluzba
 */
public class EBMSInFaultInterceptor extends AbstractEBMSInterceptor {

  
  static final SEDLogger LOG = new SEDLogger(EBMSInFaultInterceptor.class);

 
  /**
   * Keystore tools
   */
  private final KeystoreUtils mKSUtis = new KeystoreUtils();

  /**
   *
   */
  public EBMSInFaultInterceptor() {
    super(Phase.PRE_PROTOCOL); // user preprotocol for generating receipt
    getAfter().add(EBMSInInterceptor.class.getName());
  }

  /**


  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage message) {
    long l = LOG.logStart();
    LOG.log("SoapMessage: ********************************************************");
     message.entrySet().stream().forEach((entry) -> {
      LOG.formatedlog("Key: %s, val: %s", entry.getKey(), entry.getValue());
    }); 
        LOG.log("Exchange: ********************************************************");
    Exchange map = message.getExchange();
     map.entrySet().stream().forEach((entry) -> {
      LOG.formatedlog("Key: %s, val: %s", entry.getKey(), entry.getValue());
    }); 
     
     
     
    LOG.logEnd(l);
  }


}

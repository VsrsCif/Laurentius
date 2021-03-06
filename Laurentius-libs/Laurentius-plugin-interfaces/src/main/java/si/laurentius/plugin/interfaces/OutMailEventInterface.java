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
package si.laurentius.plugin.interfaces;

import javax.ejb.Local;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.msh.outbox.mail.MSHOutMail;


/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface OutMailEventInterface extends PluginComponentInterface {

  public enum  PluginOutEvent {
    SEND,
    RESEND,
    ERROR,
    FAILED;    
  }

  /**
   *
   * @param mi
   * @param ctx
   * @param evnt
   */
  public void outEvent(MSHOutMail mi, EBMSMessageContext ctx, PluginOutEvent evnt);
  
  
}

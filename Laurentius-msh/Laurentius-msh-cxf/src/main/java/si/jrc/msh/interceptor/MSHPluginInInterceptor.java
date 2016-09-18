/*
 * Copyright 2015, Supreme Court Republic of Slovenia
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

import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import si.jrc.msh.exception.EBMSError;
import si.jrc.msh.exception.EBMSErrorCode;
import static si.jrc.msh.interceptor.MSHPluginInFaultInterceptor.LOG;
import si.laurentius.msh.pmode.PluginType;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.interfaces.SoapInterceptorInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.mail.MSHInMail;

/**
 *
 * @author Jože Rihtaršič
 */
public class MSHPluginInInterceptor extends AbstractSoapInterceptor {

  /**
   *
   */
  protected static final SEDLogger LOG = new SEDLogger(MSHPluginInInterceptor.class);

  /**
   *
   */
  public MSHPluginInInterceptor() {
    super(Phase.PRE_INVOKE);
  }

  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg)
      throws Fault {
    long l = LOG.logStart();
    EBMSMessageContext ectx = SoapUtils.getEBMSMessageInContext(msg);
    MSHInMail inMail = SoapUtils.getMSHInMail(msg);
    
    
    if (ectx == null) {
      LOG.formatedWarning("No EBMSMessageContext context for in mail: '%d'.", inMail == null?-1:inMail.getId());
    } else if (ectx.getPMode()!= null 
              && ectx.getPMode().getPlugins()!= null 
              && ectx.getPMode().getPlugins().getInPlugins()!= null 
              && !ectx.getPMode().getPlugins().getInPlugins().getPlugins().isEmpty())  {

      List<PluginType> lst = ectx.getPMode().getPlugins().getInPlugins().getPlugins();
      lst.stream().map((pt) -> pt.getValue()).filter((str) ->
          (!Utils.isEmptyString(str))).forEach((str) -> {
            try {
              SoapInterceptorInterface example = InitialContext.doLookup(str);
              example.handleMessage(msg);
            } catch (NamingException ex) {
              LOG.logError(l, String.format("SoapInterceptorInterface '%s' not found!", str),  ex);
            } catch (Throwable ex) {
              String errmsg = String.format("SoapInterceptorInterface '%s' throws an error with message: %s!", str, ex.getMessage());
              LOG.logError(l, errmsg,  ex);
               throw new EBMSError(EBMSErrorCode.Other, inMail!=null?inMail.getMessageId():"",
              errmsg,ex, SoapFault.FAULT_CODE_CLIENT);
            }
      }); 
    }
    
    
    LOG.logEnd(l);
  }

}

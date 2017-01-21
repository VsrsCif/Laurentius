/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
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
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.PluginType;
import si.laurentius.commons.cxf.SoapUtils;

import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author Jože Rihtaršič
 */
public class MSHPluginOutInterceptor extends AbstractSoapInterceptor {

  /**
   *
   */
  protected final static SEDLogger LOG = new SEDLogger(MSHPluginOutInterceptor.class);

  /**
   *
   */
  public MSHPluginOutInterceptor() {
    super(Phase.USER_LOGICAL);
  }

  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg)
      throws Fault {
    long l = LOG.logStart();
    EBMSMessageContext ectx = SoapUtils.getEBMSMessageOutContext(msg);
    MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);
   String strInMsgId = outMail!=null? outMail.getMessageId(): "No-msg-id";
     
   if (ectx == null) {
      LOG.formatedWarning("No EBMSMessageContext context for out mail: '%s'.", strInMsgId);
    } else if (ectx.getPMode() != null &&
         ectx.getPMode().getPlugins() != null &&
         ectx.getPMode().getPlugins().getOutPlugins() != null &&
         !ectx.getPMode().getPlugins().getOutPlugins().getPlugins().isEmpty()) {

      List<PluginType> lst = ectx.getPMode().getPlugins().getOutPlugins().getPlugins();
      for (PluginType pt: lst) {
        String jndiName = pt.getValue();
        try {
          SoapInterceptorInterface example = InitialContext.doLookup(jndiName);
          if (!example.handleMessage(msg)){
              LOG.formatedWarning("Plugin: %s returned false  for out mail : '%s'- stop executing.", jndiName ,strInMsgId);
              break;
          };
        } catch (NamingException ex) {
          String errmsg = String.format(
              "(OutMsgID: '%s' ) Plugin '%s' not registred! Check deployment folder!",strInMsgId, jndiName,
              ex.getMessage());
          LOG.logError(l, errmsg, ex);
          throw new EBMSError(EBMSErrorCode.PModeConfigurationError,outMail!=null? outMail.getMessageId(): null,
              errmsg, ex, SoapFault.FAULT_CODE_SERVER);
        } catch (Throwable ex) {
          String errmsg = String.format(
              "(OutMsgID: '%s' ) SoapInterceptorInterface '%s' throws an error with message: %s!",strInMsgId, jndiName,
              ex.getMessage());
          LOG.logError(l, errmsg, ex);
          throw new EBMSError(EBMSErrorCode.Other,outMail!=null? outMail.getMessageId(): null,
              errmsg, ex, SoapFault.FAULT_CODE_CLIENT);
        }
      }    
    }
    LOG.logEnd(l);
  }

}

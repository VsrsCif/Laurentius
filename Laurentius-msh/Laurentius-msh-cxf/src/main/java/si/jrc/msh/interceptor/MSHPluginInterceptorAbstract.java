/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.interceptor;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import si.laurentius.commons.ebms.EBMSError;
import si.jrc.msh.exception.EBMSErrorCode;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.SEDInterceptorEvent;
import si.laurentius.commons.enums.SEDInterceptorRole;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.rule.DecisionRuleAssertion;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.interceptor.SEDInterceptorInstance;
import si.laurentius.interceptor.SEDInterceptorProperty;
import si.laurentius.interceptor.SEDInterceptorRule;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author sluzba
 */
public abstract class MSHPluginInterceptorAbstract extends AbstractSoapInterceptor {

  /**
   *
   */
  protected static final SEDLogger LOG_PRIVATE = new SEDLogger(
          AbstractSoapInterceptor.class);
  DecisionRuleAssertion dra = new DecisionRuleAssertion();
  protected SEDPluginManagerInterface mPluginManager;
  /**
   *
   */
  protected SEDLookupsInterface mSedLookups;

  public MSHPluginInterceptorAbstract(String p) {
    super(p);
  }

  /**
   * Methods lookups SEDLookupsInterface.
   *
   * @return SEDLookupsInterface or null if bad application configuration.
   */
  public SEDLookupsInterface getLookups() {
    long l = AbstractEBMSInterceptor.A_LOG.logStart();
    if (mSedLookups == null) {
      try {
        mSedLookups = InitialContext.doLookup(SEDJNDI.JNDI_SEDLOOKUPS);
        AbstractEBMSInterceptor.A_LOG.logEnd(l);
      } catch (NamingException ex) {
        AbstractEBMSInterceptor.A_LOG.logError(l, ex);
      }
    }
    return mSedLookups;
  }

  public SEDPluginManagerInterface getPluginManager() {
    long l = AbstractEBMSInterceptor.A_LOG.logStart();
    if (mPluginManager == null) {
      try {
        mPluginManager = InitialContext.doLookup(SEDJNDI.JNDI_PLUGIN);
        AbstractEBMSInterceptor.A_LOG.logEnd(l);
      } catch (NamingException ex) {
        AbstractEBMSInterceptor.A_LOG.logError(l, ex);
      }
    }
    return mPluginManager;
  }

  protected void handleInterception(SEDInterceptorEvent se,
          SoapMessage msg) {

    MSHInMail inMail = SoapUtils.getMSHInMail(msg);
    MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);
    String strMsg = String.format(
            "InterceptEvent '%s', isInboundMessage:'%s', isRequestMessage '%s',"
            + " inMailId '%d', outMailId %s ",
            se.getValue(), SoapUtils.isInboudMessage(msg) ? "true" : "false",
            SoapUtils.isRequestMessage(msg) ? "true" : "false",
            inMail != null ? inMail.getId() : null,
            outMail != null ? outMail.getId() : null
    );

    long l = LOG_PRIVATE.logStart();
    if (inMail != null || outMail != null) {
      List<SEDInterceptor> lst = getLookups().getSEDInterceptors();
      for (SEDInterceptor intc : lst) {
        if (intc.isActive() && Objects.equals(intc.getInterceptEvent(),
                se.getValue())) {

          boolean intercept = interceptMail(outMail, inMail, intc);

          LOG_PRIVATE.formatedWarning("Intercept %s message %s", intercept? "true" : "false",strMsg);
          if (intercept) {
            
            // set ready context properties 
            SEDInterceptorInstance inst = intc.getSEDInterceptorInstance();
            Properties contextProperties = new Properties();
            for (SEDInterceptorProperty sip: inst.getSEDInterceptorProperties()) {
              contextProperties.setProperty(sip.getKey(),sip.getValue());
            }
            
            MailInterceptorDef mid = getPluginManager().
                    getMailInterceptoDef(intc.getSEDInterceptorInstance().
                            getPlugin(), intc.getSEDInterceptorInstance().
                                    getType());
            String jndiName = mid.getJndi();
            LOG_PRIVATE.formatedlog("Execute plugin: %s", jndiName);
            if (!Utils.isEmptyString(jndiName)) {
              try {
                SoapInterceptorInterface interceptor = InitialContext.doLookup(
                        jndiName);
                if (!interceptor.handleMessage(msg, contextProperties)) {
                  LOG_PRIVATE.formatedWarning(
                          "Plugin: %s returned false - stop executing for: '%s'.",
                          jndiName, strMsg);
                  break;
                }
                ;
              } catch (NamingException ex) {
                String errmsg = String.format(
                        "('%s') Plugin '%s' not registred! Check deployment folder!",
                        strMsg, jndiName, ex.getMessage());
                LOG_PRIVATE.logError(l, errmsg, ex);
                throw new EBMSError(EBMSErrorCode.PModeConfigurationError,
                        strMsg, errmsg, ex, SoapFault.FAULT_CODE_SERVER);
              } catch (Throwable ex) {
                String errmsg = String.format(
                        "('%s') SoapInterceptorInterface '%s' throws an error with message: %s!",
                        strMsg, jndiName, ex.getMessage());
                LOG_PRIVATE.logError(l, errmsg, ex);
                throw new EBMSError(EBMSErrorCode.Other, strMsg, errmsg, ex,
                        SoapFault.FAULT_CODE_CLIENT);
              }
            }
          }
        }
      }
    }
    LOG_PRIVATE.logEnd(l);
  }

  private boolean interceptMail(MSHOutMail outMail, MSHInMail inMail,
          SEDInterceptor intc) {
    boolean intercept = false;

    /*
          requestr  in-messageEvent    - outMessage
          requestr  out-messageEvent   - outMessage
          responder  in-messageEvent   - inMessage
          responder  out-messageEvent  - inMessage
     */
    // test condition for interception
    if (outMail != null && (intc.getInterceptRole() == null
            || Objects.equals(intc.getInterceptRole(),
                    SEDInterceptorRole.ALL.getValue())
            || Objects.equals(intc.getInterceptRole(),
                    SEDInterceptorRole.REQUESTOR.getValue()))) {

      intercept = true;
      for (SEDInterceptorRule ir : intc.getSEDInterceptorRules()) {
        if (!dra.assertRule(outMail, ir)) {
          intercept = false;
          break;
        }
      }

    }

    if (!intercept && inMail != null && (intc.getInterceptRole() == null
            || Objects.equals(intc.getInterceptRole(),
                    SEDInterceptorRole.ALL.getValue())
            || Objects.equals(intc.getInterceptRole(),
                    SEDInterceptorRole.RESPONDER.getValue()))) {

      intercept = true;
      for (SEDInterceptorRule ir : intc.getSEDInterceptorRules()) {
        if (!dra.assertRule(inMail, ir)) {
          intercept = false;
          break;
        }
      }

    }
    return intercept;
  }

}

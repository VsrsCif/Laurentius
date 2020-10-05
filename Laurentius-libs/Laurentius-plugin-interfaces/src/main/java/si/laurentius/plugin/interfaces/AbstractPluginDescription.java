/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.plugin.interfaces;

import java.io.File;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.plugin.component.ComponentBase;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.eventlistener.OutMailEventListenerDef;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.exception.PluginException;
import si.laurentius.plugin.processor.InMailProcessorDef;

/**
 *
 * @author Jože Rihtaršič
 */
public abstract class AbstractPluginDescription implements
        PluginDescriptionInterface {

  private static final String JNDI_APPLICATION_NAME = "java:app/AppName";

  private static final String JNDI_INTERFACE_TEMPLATE = "java:global/%s/%s!%s";

  static final SEDLogger LOG = new SEDLogger(AbstractPluginDescription.class);

  private String applicationName;
  private Plugin mPlgDef = null;

  @Override
  public Plugin getPluginDescription() {

    if (mPlgDef == null) {
      mPlgDef = new Plugin();
      mPlgDef.setName(getName());
      mPlgDef.setType(getType());
      mPlgDef.setVersion(getVersion());
      mPlgDef.setDescription(getDesc());
      mPlgDef.setWebContext(getWebUrlContext());
      mPlgDef.setMainMenu(getMenu());
      mPlgDef.setProcessMenu(getProcessMenu());
      mPlgDef.setDefaultInitData(getDefaultInitData());
      
      if (getWebPageRoles() != null && !getWebPageRoles().isEmpty()) {
        mPlgDef.getWebRoles().addAll(getWebPageRoles());
      }
      mPlgDef.setJndi(String.format(JNDI_INTERFACE_TEMPLATE,
              getApplicationName(),
              getClass().getSimpleName(), PluginDescriptionInterface.class.
              getName()));
    }

    return mPlgDef;
  }


  public void registerPluginComponentInterface(Class plg)
          throws PluginException {
    if (plg == null) {
      throw new PluginException(
              PluginException.PluginExceptionCode.NullPluginType,
              String.format("Plugin is null! Invalid argument"));
    }

    ComponentBase cb = null;
    if (TaskExecutionInterface.class.isAssignableFrom(plg)) {
      cb = getComponentDescription(plg, TaskExecutionInterface.class);
      if (cb instanceof CronTaskDef) {
        getPluginDescription().getCronTaskDeves().add((CronTaskDef) cb);
      } else {
        throw new PluginException(
                PluginException.PluginExceptionCode.InitPluginException,
                String.format(
                        "Plugin '%s' component '%s'! TaskExecutionInterface Component description must be  CronTaskDef but is '%s'!",
                        getApplicationName(), plg.getName(), cb.getClass().
                        getName()));
      }
    } else if (InMailProcessorInterface.class.isAssignableFrom(plg)) {
      cb = getComponentDescription(plg, InMailProcessorInterface.class);
      if (cb instanceof InMailProcessorDef) {
        getPluginDescription().getInMailProcessorDeves().add(
                (InMailProcessorDef) cb);
      } else {
        throw new PluginException(
                PluginException.PluginExceptionCode.InitPluginException,
                String.format(
                        "Plugin '%s' component '%s'! InMailProcessorInterface Component description must be  InMailProcessorDef but is '%s'!",
                        getApplicationName(), plg.getName(), cb.getClass().
                        getName()));
      }
    } else if (OutMailEventInterface.class.isAssignableFrom(plg)) {
      cb = getComponentDescription(plg, OutMailEventInterface.class);
      if (cb instanceof OutMailEventListenerDef) {
        getPluginDescription().getOutMailEventListenerDeves().add(
                (OutMailEventListenerDef) cb);
      } else {
        throw new PluginException(
                PluginException.PluginExceptionCode.InitPluginException,
                String.format(
                        "Plugin '%s' component '%s'! OutMailEventInterface Component description must be  OutMailEventListenerDef but is '%s'!",
                        getApplicationName(), plg.getName(), cb.getClass().
                        getName()));
      }

    } else if (SoapInterceptorInterface.class.isAssignableFrom(plg)) {
      cb = getComponentDescription(plg, SoapInterceptorInterface.class);
      if (cb instanceof MailInterceptorDef) {
        getPluginDescription().getMailInterceptorDeves().add(
                (MailInterceptorDef) cb);
      } else {
        throw new PluginException(
                PluginException.PluginExceptionCode.InitPluginException,
                String.format(
                        "Plugin '%s' component '%s'! SoapInterceptorInterface Component description must be MailInterceptorDef but is '%s'!",
                        getApplicationName(), plg.getName(), cb.getClass().
                        getName()));
      }

    } else {
      throw new PluginException(
              PluginException.PluginExceptionCode.UnknownPluginType,
              String.format(
                      "Class %s can not be casted to known plugin interface!",
                      plg.getName()));
    }

  }

  private ComponentBase getComponentDescription(Class plg, Class intrfc)
          throws PluginException {
    String JNDI
            = String.format(JNDI_INTERFACE_TEMPLATE, getApplicationName(), plg.
                    getSimpleName(),
                    intrfc.getName());

    ComponentBase cb;
    PluginComponentInterface plgInt;
    try {
      plgInt = InitialContext.doLookup(JNDI);
      cb = plgInt.getDefinition();
      if (cb == null) {
        throw new PluginException(
                PluginException.PluginExceptionCode.InitPluginException,
                String.format(
                        "Could not instantiate EJB for class '%s' and JNDI '%s'! Missing ComponentBase!",
                        plg.getName(), JNDI));
      }
      cb.setJndi(JNDI);
    } catch (NamingException ex) {
      throw new PluginException(
              PluginException.PluginExceptionCode.InitPluginException,
              String.format(
                      "Could not instantiate EJB for class '%s' and JNDI '%s'!",
                      plg.getName(),
                      JNDI), ex);
    }

    return cb;
  }

  protected void registerPlugin() throws PluginException {

    try {
      SEDPluginManagerInterface pmi = InitialContext.doLookup(
              SEDJNDI.JNDI_PLUGIN);
      pmi.registerPlugin(getPluginDescription());

    } catch (NamingException ex) {
      throw new PluginException(
              PluginException.PluginExceptionCode.InitPluginException,
              "Could not register plugin!", ex);
    }
  }

  private String getApplicationName() {
    if (Utils.isEmptyString(applicationName)) {
      try {
        applicationName = InitialContext.doLookup(JNDI_APPLICATION_NAME);
      } catch (NamingException ex) {
        LOG.logError(
                "Could not retrieve application name for plugin descriptor!", ex);
      }
    }
    return applicationName;
  }


  /**
   *
   * @param initFolder
   * @param savePasswds
   */
  @Override
  public void exportData(File initFolder, boolean  savePasswds) {
  }

}

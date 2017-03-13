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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.ejb.AccessTimeout;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.eventlistener.OutMailEventListenerDef;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.InMailProcessorInterface;
import si.laurentius.plugin.processor.InMailProcessorDef;

/**
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@AccessTimeout(value = 30000)
@Local(SEDPluginManagerInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SEDPluginManager implements SEDPluginManagerInterface {

  /**
   *
   */
  protected static final SEDLogger LOG = new SEDLogger(SEDPluginManager.class);

  List<Plugin> mlstRegistredPlugins = new ArrayList<>();

  @Override
  public CronTaskDef getCronTaskDef(String plugin, String task) {
    Plugin plg = getPluginByType(plugin);
    if (plg != null && !plg.getCronTaskDeves().isEmpty()) {
      for (CronTaskDef ctd : plg.getCronTaskDeves()) {
        if (Objects.equals(ctd.getType(), task)) {
          return ctd;
        }
      }
    }
    return null;

  }

  @Override
  public List<CronTaskDef> getCronTasksForPlugin(String plugin) {

    for (Plugin plg : mlstRegistredPlugins) {
      if (Objects.equals(plg.getType(), plugin)) {
        return plg.getCronTaskDeves();
      }
    }
    return Collections.emptyList();
  }

  @Override
  public InMailProcessorDef getInMailProcessor(String plugin, String task) {
    Plugin plg = getPluginByType(plugin);
    if (plg != null && !plg.getInMailProcessorDeves().isEmpty()) {
      for (InMailProcessorDef ctd : plg.getInMailProcessorDeves()) {
        if (Objects.equals(ctd.getType(), task)) {
          return ctd;
        }
      }
    }
    return null;

  }

  @Override
  public MailInterceptorDef getMailInterceptoDef(String plugin, String task) {
    Plugin plg = getPluginByType(plugin);
    if (plg != null && !plg.getMailInterceptorDeves().isEmpty()) {
      for (MailInterceptorDef ctd : plg.getMailInterceptorDeves()) {
        if (Objects.equals(ctd.getType(), task)) {
          return ctd;
        }
      }
    }
    return null;
  }

  @Override
  public List<InMailProcessorDef> getInMailProcessorForPlugin(String plugin) {
    for (Plugin plg : mlstRegistredPlugins) {
      if (Objects.equals(plg.getType(), plugin)) {
        return plg.getInMailProcessorDeves();
      }
    }
    return Collections.emptyList();

  }

  @Override
  public List<String> getInMailProcessorInstances(String plugin, String type) {
    InMailProcessorDef imp = getInMailProcessor(plugin, type);
    if (imp == null) {
      LOG.formatedWarning("InMailProcessor %s for plugin %s not exists!",
              plugin, type);

    } else {
      try {
        InMailProcessorInterface pmi = InitialContext.doLookup(imp.getJndi());
        return pmi.getInstanceIds();
      } catch (NamingException ex) {
        LOG.logError(String.format(
                "Error occured while lookup InMailProcessor %s for plugin %s!, Error %s",
                plugin, type, ex.getMessage()), ex);
      }
    }
    return Collections.emptyList();
  }

  @Override
  public List<MailInterceptorDef> getMailInterceptorForPlugin(String plugin) {
    for (Plugin plg : mlstRegistredPlugins) {
      if (Objects.equals(plg.getType(), plugin)) {
        return plg.getMailInterceptorDeves();
      }
    }
    return Collections.emptyList();

  }

  @Override
  public List<OutMailEventListenerDef> getOutMailEventListenerForPlugin(
          String plugin) {
    for (Plugin plg : mlstRegistredPlugins) {
      if (Objects.equals(plg.getType(), plugin)) {
        return plg.getOutMailEventListenerDeves();
      }
    }
    return Collections.emptyList();
  }

  @Override
  public Plugin getPluginByType(String type) {
    for (Plugin plg : mlstRegistredPlugins) {
      if (Objects.equals(plg.getType(), type)) {
        return plg;
      }
    }
    return null;
  }

  @Override
  public List<Plugin> getRegistredPlugins() {
    return mlstRegistredPlugins;
  }

  @Override
  @Lock(LockType.WRITE)
  public void registerPlugin(Plugin pdi) {
    mlstRegistredPlugins.add(pdi);
  }

}

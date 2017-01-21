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

import java.util.List;
import javax.ejb.Local;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.eventlistener.OutMailEventListenerDef;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.processor.InMailProcessorDef;

/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface SEDPluginManagerInterface {

  
  
  void  registerPlugin(Plugin pdi);
  Plugin getPluginByType(String type);  
  List<Plugin> getRegistredPlugins();
  
  
  
  
  CronTaskDef getCronTaskDef(String plugin, String task);
  InMailProcessorDef getInMailProcessor(String plugin, String task);

  
  List<CronTaskDef> getCronTasksForPlugin(String plugin);
  List<MailInterceptorDef> getMailInterceptorForPlugin(String plugin);
  List<InMailProcessorDef> getInMailProcessorForPlugin(String plugin);
  List<OutMailEventListenerDef> getOutMailEventListenerForPlugin(String plugin);
  // return  processor instances for type
  List<String> getInMailProcessorInstances(String plugin, String type);

}

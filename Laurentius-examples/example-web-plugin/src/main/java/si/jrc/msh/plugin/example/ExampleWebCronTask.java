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
package si.jrc.msh.plugin.example;

import java.io.StringWriter;
import java.util.Properties;
import javax.ejb.Local;
import javax.ejb.Stateless;

import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;

/**
 * This is samople of cron task plugin component.
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class ExampleWebCronTask implements TaskExecutionInterface {

  public static final String KEY_FOLDER = "example.task.folder";
  public static final String KEY_PARAM_1 = "example.webtask.parameter.001";
  public static final String KEY_PARAM_2 = "example.webtask.parameter.002";
  public static final String KEY_PARAM_3 = "example.webtask.parameter.003";
  public static final String KEY_PARAM_4 = "example.webtask.parameter.004";
  private static final SEDLogger LOG = new SEDLogger(ExampleWebCronTask.class);

  /**
   * execute metod
   *
   * @param p - parameters defined at configuration of task instance
   * @return result description
   */
  @Override
  public String executeTask(Properties p)
      throws TaskException {
    long l = LOG.logStart();

    StringWriter sw = new StringWriter();
    sw.append("Start example task: ");
    sw.append("\n");

    for (String pKey : p.stringPropertyNames()) {
      sw.append(String.format("Property key: '%s' value %s\n", pKey, p.getProperty(pKey)));
    }
    sw.append("Example task ends in : " + (l - LOG.getTime()) + " ms\n");
    LOG.logEnd(l, sw.toString());
    return sw.toString();
  }
  
  /**
   * Retrun cron task definition: name, unique type, description, parameters.. 
   * @return Cron task definition
   */

  @Override
  public CronTaskDef getDefinition() {
    CronTaskDef tt = new CronTaskDef();
    tt.setType("example-web-task");
    tt.setName("Example web task");
    tt.setDescription("This is simple example of cron task in web plugin example");
    tt.getCronTaskPropertyDeves().add(
        createTTProperty(KEY_FOLDER, "Example folder"));
    
    tt.getCronTaskPropertyDeves().add(
        createTTProperty(KEY_PARAM_1, "First parameter", true, PropertyType.String.getType(), null, null));
    
    tt.getCronTaskPropertyDeves().add(
        createTTProperty(KEY_PARAM_2, "Boolean (true/false)", true,
            
            PropertyType.Boolean.getType(), null, null));
    tt.getCronTaskPropertyDeves().add(
        createTTProperty(KEY_PARAM_3, "Integer parameter", false,
            PropertyType.Integer.getType(), null, null));
    
    tt.getCronTaskPropertyDeves().add(
        createTTProperty(KEY_PARAM_4, "List parameter", false,
            PropertyType.List.getType(), null, "Value1,value2,value3"));
    return tt;
  }

  private CronTaskPropertyDef createTTProperty(String key, String desc, boolean mandatory,
      String type, String valFormat, String valList) {
    CronTaskPropertyDef ttp = new CronTaskPropertyDef();
    ttp.setKey(key);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
    return ttp;
  }

  private CronTaskPropertyDef createTTProperty(String key, String desc) {
    return createTTProperty(key, desc, true, "string", null, null);
  }

}

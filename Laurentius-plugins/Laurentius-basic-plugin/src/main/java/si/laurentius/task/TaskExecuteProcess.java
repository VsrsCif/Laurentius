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
package si.laurentius.task;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.plg.db.IMPDBInterface;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class TaskExecuteProcess implements TaskExecutionInterface {

  private static final SEDLogger LOG = new SEDLogger(TaskExecuteProcess.class);
  public static final String KEY_EXECUTE_COMMAND = "imp.execute.command";
  public static final String KEY_EXECUTE_PARAMETERS = "imp.execute.parameters";

  StringFormater msfFormat = new StringFormater();

  @EJB
  private IMPDBInterface mDB;

  @Override
  public CronTaskDef getDefinition() {
    CronTaskDef impd = new CronTaskDef();
    impd.setType("taskexecute");
    impd.setName("Execute extermal process");
    impd.setDescription("Execute external process");

    impd.getCronTaskPropertyDeves().add(createTTProperty(KEY_EXECUTE_COMMAND,
            "Execution command/file", true,
            PropertyType.String.getType(), null, null,
            "${laurentius.home}/scripts/export-appl.sh"));

    impd.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_EXECUTE_PARAMETERS,
                    "Execution parameters", false,
                    PropertyType.String.getType(), null, null,
                    "fixParam1 fixParam2 fixParam3"));

    return impd;

  }

  @Override
  public String executeTask(Properties p) throws TaskException {
    long l = LOG.logStart();
    
    

    String res = executeProcessForTask(p);

    
    LOG.logEnd(l);
    return res;
  }

  private CronTaskPropertyDef createTTProperty(String key, String desc,
          boolean mandatory,
          String type, String valFormat, String valList, String defValue) {
    CronTaskPropertyDef ttp = new CronTaskPropertyDef();
    ttp.setKey(key);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
    ttp.setDefValue(defValue);
    return ttp;
  }

  public String executeProcessForTask(Properties prop)
          throws TaskException {
   
    String strCmd = prop.getProperty(KEY_EXECUTE_COMMAND);
    String strPrms = prop.getProperty(KEY_EXECUTE_PARAMETERS);
   
    if (Utils.isEmptyString(strCmd)) {
      String errMsg = "Execution failed! No command defined!";
      throw new TaskException(
              TaskException.TaskExceptionCode.InitException,
              errMsg);
    }

    String command = StringFormater.replaceProperties(strCmd);
    String params = strPrms;
          

    return executeCommand( command, params);

  }

  public String executeCommand(String cmd, String param)
          throws TaskException {
    long t = LOG.logStart();
      StringWriter sw = new StringWriter();
    sw.append("Start task: ");
    sw.append(cmd);
    sw.append("\n");
    
    long procRes = -1;
    try {
      String command = StringFormater.replaceProperties(cmd);
      ProcessBuilder builder = new ProcessBuilder(command, param);
      LOG.formatedlog(
              "Start execution of task: interpreter %s  command  '%s', params '%s'",
              command, param);
      long lSt = LOG.getTime();
      Process process = builder.start();
      procRes = process.waitFor();
      String msg = String.format(
              "END execution of command '%s', params '%s' in %d ms. Return value %d",
              command, param, (LOG.getTime() - lSt), procRes);
      sw.append(msg);
      
      LOG.log(msg);

      if (procRes != 0) {
        String errMsg = String.format(
                "Execution process %s return value '%d'. Normal termination is '0'!",
                command.length() > 20 ? "..." + command.substring(command.
                length() - 20) : command,
                procRes);

        throw new TaskException(
                TaskException.TaskExceptionCode.ProcessException,
                errMsg);

      }

    } catch (InterruptedException | IOException ex) {
      String errMsg = String.format(
              "Execution process failed %s!",
              ex.getMessage());
      throw new TaskException(
              TaskException.TaskExceptionCode.ProcessException,
              errMsg, ex, false, true);

    }
    LOG.logEnd(t);
    return sw.toString();
  }

}

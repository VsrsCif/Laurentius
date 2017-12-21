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
package si.laurentius.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.plg.db.IMPDBInterface;
import si.laurentius.plugin.interfaces.InMailProcessorInterface;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.exception.InMailProcessException;
import si.laurentius.plugin.processor.InMailProcessorDef;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(InMailProcessorInterface.class)
public class ProcessExecute extends AbstractMailProcessor {

  private static final SEDLogger LOG = new SEDLogger(ProcessExecute.class);
  public static final String KEY_EXECUTE_FOLDER = "imp.execute.work.folder";
  public static final String KEY_EXECUTE_INTERPRETER = "imp.execute.interpreter";
  
  
  public static final String KEY_EXECUTE_COMMAND = "imp.execute.command";
  public static final String KEY_EXECUTE_PARAMETERS = "imp.execute.parameters";

  StringFormater msfFormat = new StringFormater();

  @EJB
  private IMPDBInterface mDB;

  @Override
  public InMailProcessorDef getDefinition() {
    InMailProcessorDef impd = new InMailProcessorDef();
    impd.setType("execute");
    impd.setName("Execute processor");
    impd.setDescription("Execute processor");

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_EXECUTE_COMMAND, "${laurentius.home}/scripts/export-appl.sh",
            "Execution command.", true,
            PropertyType.String.getType(), null, null));
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_EXECUTE_INTERPRETER, null,
            "Interpreter: aka /bin/bash", false,
            PropertyType.String.getType(), null, null));
    
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_EXECUTE_FOLDER, "${laurentius.home}/scripts/",
            "Execution work folder", false,
            PropertyType.String.getType(), null, null));
    
    
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_EXECUTE_PARAMETERS, "${Id} ${SenderEBox} ${Service}",
            "Execution parameters.", true,
            PropertyType.String.getType(), null, null));

    return impd;

  }

  @Override
  public List<String> getInstanceIds() {

    return Collections.emptyList();
  }

  @Override
  public boolean proccess(MSHInMail mi, Map<String, Object> map) throws InMailProcessException {
    long l = LOG.logStart(mi.getId());
    boolean suc = false;

    List<String> lst = (List<String>) map.
            get(ProcessConstants.MP_EXPORT_FILES);

    executeProcessForMail(map, mi, lst);

    suc = true;

    LOG.logEnd(l, mi.getId());
    return suc;
  }

  public long executeProcessForMail(Map<String, Object> map, MSHInMail mail,
          List<String> lstExportFiles)
          throws InMailProcessException {

    String strCmd = (String) map.get(KEY_EXECUTE_COMMAND);
    String strPrms = (String) map.get(KEY_EXECUTE_PARAMETERS);
    String strInterpreter = (String) map.get(KEY_EXECUTE_INTERPRETER);
    String strWorkFolder = (String) map.get(KEY_EXECUTE_FOLDER);

    if (Utils.isEmptyString(strCmd)) {
      String errMsg = "Execution failed! No command defined!";
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              errMsg);
    }

    String command = StringFormater.replaceProperties(strCmd);
    
    List<String> params = new ArrayList<>();
    // add exported files
    if (lstExportFiles!=null) {
      params.add(String.join(File.pathSeparator,
                            lstExportFiles));
    }
    // add parameters
    params.addAll(translateCommandline(StringFormater.format(strPrms, mail)) );
    
    // execute folder
    return executeCommand(mail, command, params, strWorkFolder, strInterpreter);

  }

  public long executeCommand(MSHInMail mail, String command, List<String> params, String workFolder, String interpreter)
          throws InMailProcessException {
    long t = LOG.logStart();
    long procRes = -1;
    
    try {

      // get execute folder
      String folder = workFolder;
      if (Utils.isEmptyString(folder)  && command.contains(File.separator)) {
        folder = command.substring(0, command.lastIndexOf(File.separator));
      }
      List<String> lstArray = new ArrayList<>();
      if (!Utils.isEmptyString(interpreter)){
        lstArray.add(interpreter);
      }
      
      lstArray.add(command);
      lstArray.addAll(params);

      ProcessBuilder builder = new ProcessBuilder(lstArray);

      if (folder != null) {
        File f = new File(folder);
        if (f.exists() && f.isDirectory()) {
          builder.directory(new File(folder));
        } else {
        LOG.formatedWarning("Folder %s not exists or is not a folder (Process folder is not setted).", folder);
        }
      }
      String execArrayStr = String.join(" ",lstArray);
      LOG.formatedlog(
              "Start execution for mail %d, execution  array: '%s'.",
              mail.getId(), execArrayStr
              );
      long lSt = LOG.getTime();
      Process process = builder.start();
      procRes = process.waitFor();
      LOG.formatedlog(
              "END execution for mail %d in %d ms. Return value %d",
               mail.getId(), (LOG.getTime() - lSt), procRes);

      if (procRes != 0) {
        String errMsg = String.format(
                "Execution for mail %d, execution  array: '%s' returned value '%d'. Normal termination is '0'!",
                mail.getId(), 
                execArrayStr.length() > 20 ? "..." + execArrayStr.substring(execArrayStr.
                length() - 20) : execArrayStr,
                procRes);

        throw new InMailProcessException(
                InMailProcessException.ProcessExceptionCode.ProcessException,
                errMsg);

      }

    } catch (InterruptedException | IOException ex) {
      String errMsg = String.format(
              "Execution process failed %s!",
              ex.getMessage());
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.ProcessException,
              errMsg, ex, false, true);

    }
    LOG.logEnd(t);
    return procRes;
  }

  /**
   * [code borrowed from
   * org/apache/tools/ant/types/Commandline.java#Commandline.translateCommandline(java.lang.String)]
   *
   * @param toProcess the command line to process.
   * @return the command line broken into strings. An empty or null toProcess
   * parameter results in a zero sized array.
   */
  public static List<String> translateCommandline(String toProcess) throws InMailProcessException {
    if (toProcess == null || toProcess.length() == 0) {
      //no command? no string
      return Collections.emptyList();
    }
    // parse with a simple finite state machine

    final int normal = 0;
    final int inQuote = 1;
    final int inDoubleQuote = 2;
    int state = normal;
    final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
    final ArrayList<String> result = new ArrayList<>();
    final StringBuilder current = new StringBuilder();
    boolean lastTokenHasBeenQuoted = false;

    while (tok.hasMoreTokens()) {
      String nextTok = tok.nextToken();
      switch (state) {
        case inQuote:
          if ("\'".equals(nextTok)) {
            lastTokenHasBeenQuoted = true;
            state = normal;
          } else {
            current.append(nextTok);
          }
          break;
        case inDoubleQuote:
          if ("\"".equals(nextTok)) {
            lastTokenHasBeenQuoted = true;
            state = normal;
          } else {
            current.append(nextTok);
          }
          break;
        default:
          if ("\'".equals(nextTok)) {
            state = inQuote;
          } else if ("\"".equals(nextTok)) {
            state = inDoubleQuote;
          } else if (" ".equals(nextTok)) {
            if (lastTokenHasBeenQuoted || current.length() != 0) {
              result.add(current.toString());
              current.setLength(0);
            }
          } else {
            current.append(nextTok);
          }
          lastTokenHasBeenQuoted = false;
          break;
      }
    }
    if (lastTokenHasBeenQuoted || current.length() != 0) {
      result.add(current.toString());
    }
    if (state == inQuote || state == inDoubleQuote) {
      String strMsg = "unbalanced quotes in " + toProcess;
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.ProcessException,
              strMsg);
    }
    return result;
  }

}

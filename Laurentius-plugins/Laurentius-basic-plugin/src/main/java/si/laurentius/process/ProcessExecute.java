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
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.commons.SEDInboxMailStatus;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.plg.db.IMPDBInterface;
import si.laurentius.plugin.imp.IMPExecute;
import si.laurentius.plugin.imp.IMPExport;
import si.laurentius.plugin.interfaces.InMailProcessorInterface;
import si.laurentius.plugin.interfaces.exception.InMailProcessException;
import si.laurentius.plugin.processor.InMailProcessorDef;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(InMailProcessorInterface.class)
public class ProcessExecute implements InMailProcessorInterface {
  
  private static final SEDLogger LOG = new SEDLogger(ProcessExecute.class);

   StringFormater msfFormat = new StringFormater();
   
   @EJB
  private IMPDBInterface mDB;
   @Override
   public InMailProcessorDef getDefinition() {
     InMailProcessorDef impd = new InMailProcessorDef();
     impd.setType("execute");
     impd.setName("Execute processor");
     impd.setDescription("Execute processor");
     
     return impd;
     
   }

  @Override
  public List<String> getInstanceIds() {
    List<String> lst = new ArrayList<>();
    for (IMPExport im: mDB.getExports()){
      lst.add(im.getInstance());
    }
    return lst;
  }
  @Override
  public boolean proccess(String instance, MSHInMail mi,
          Map<String, Object> map) {
    long l = LOG.logStart(instance, mi.getId());
    boolean suc = false;
    IMPExecute imex = mDB.getExecute(instance);
    try {
      List<String> lst =  (List<String>)map.get(ProcessConstants.MP_EXPORT_FILES);
      long lres = executeProcessForMail(imex, mi, lst);

      suc = true;
    } catch (InMailProcessException ex) {
      LOG.logError(instance, ex);
    }
    LOG.logEnd(l, instance, mi.getId());
    return suc;
  }
  
  public long executeProcessForMail(IMPExecute e, MSHInMail mail, List<String> lstExportFiles)
      throws InMailProcessException {
    if (!Utils.isEmptyString(e.getCommand())) {
      String errMsg = "Execution external procces failed! No command defimed!";
      throw new InMailProcessException(InMailProcessException.ProcessExceptionCode.InitException,
          errMsg);
    }

    String command = StringFormater.replaceProperties(e.getCommand());
    String params =
        (lstExportFiles != null ? String.join(File.pathSeparator, lstExportFiles) + " " : "") +
        msfFormat.format(
            e.getParameters(), mail);
    return executeCommand(mail, command, params);

  }
  
  public long executeCommand(MSHInMail mail, String cmd, String param)
      throws InMailProcessException {
    long t = LOG.logStart();
    long procRes = -1;
    try {
      String command = StringFormater.replaceProperties(cmd);
      ProcessBuilder builder = new ProcessBuilder(command, param);
      LOG.formatedlog("Start execution of command '%s', params '%s' for mail %d", command, param,
          mail.getId());
      long lSt = LOG.getTime();
      Process process = builder.start();
      procRes = process.waitFor();
      LOG.formatedlog(
          "END execution of command '%s', params '%s' for mail %d in %d ms. Return value %d",
          command, param, mail.getId(), (LOG.getTime() - lSt), procRes);

      if (procRes != 0) {
        String errMsg = String.format(
            "Execution process %s return value '%d'. Normal termination is '0'!",
            command.length() > 20 ? "..." + command.substring(command.length() - 20) : command,
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


}

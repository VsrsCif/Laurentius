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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import si.laurentius.commons.MimeValues;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.plg.db.IMPDBInterface;
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
public class ProcessExport implements InMailProcessorInterface {

  private static final SEDLogger LOG = new SEDLogger(ProcessExport.class);

  StringFormater msfFormat = new StringFormater();

  StorageUtils msStorageUtils = new StorageUtils();

  @EJB
  private IMPDBInterface mDB;

  @Override
  public InMailProcessorDef getDefinition() {
    InMailProcessorDef impd = new InMailProcessorDef();
    impd.setType("export");
    impd.setName("Export processor");
    impd.setDescription("Export processor");

    return impd;

  }

  @Override
  public List<String> getInstanceIds() {
    List<String> lst = new ArrayList<>();
    for (IMPExport im : mDB.getExports()) {
      lst.add(im.getInstance());
    }
    return lst;
  }

  @Override
  public boolean proccess(String instance, MSHInMail mi,
          Map<String, Object> map) {
    long l = LOG.logStart(instance, mi.getId());
    boolean suc = false;
    IMPExport im = mDB.getExport(instance);
    try {
      List<String> lst = exportMail(im, mi);
      map.put("imp.export.files", lst);
      suc = true;
    } catch (InMailProcessException ex) {
      LOG.logError(instance, ex);
    }
    LOG.logEnd(l, instance, mi.getId());
    return suc;
  }

  public List<String> exportMail(IMPExport e, MSHInMail mail)
          throws InMailProcessException {

    String exportFolderName;
    File exportFolder;
    // export metadata
    List<String> listFiles = new ArrayList<>();

    exportFolderName = msfFormat.format(e.getFileMask(), mail);
    String folder = StringFormater.replaceProperties(e.getFolder());
    exportFolder = new File(folder + File.separator + exportFolderName);
    if (!exportFolder.exists() && !exportFolder.mkdirs()) {
      String errMsg = String.format(
              "Export failed! Could not create export folder '%s'!",
              folder);
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              errMsg);
    }

    boolean overwriteFiles = e.getOverwrite() != null && e.getOverwrite();
    if (e.getExportMetaData() != null && e.getExportMetaData()) {
      File fn = new File(
              exportFolder.getAbsolutePath() + File.separator + String.format(
              "metadata_%06d.", mail.getId())
              + MimeValues.MIME_XML.getSuffix());
      if (fn.exists() && overwriteFiles) {
        fn.delete();
      }

      try {

        XMLUtils.serialize(mail, fn);
        listFiles.add(fn.getAbsolutePath());

      } catch (JAXBException | FileNotFoundException ex) {
        String errMsg = String.format("Failed to serialize metadata: %s!",
                ex.getMessage());
        throw new InMailProcessException(
                InMailProcessException.ProcessExceptionCode.ProcessException,
                errMsg);

      }

      for (MSHInPart mip : mail.getMSHInPayload().getMSHInParts()) {
        File f;
        try {
          f = msStorageUtils.copyFileToFolder(mip.getFilepath(), exportFolder,
                  e.getOverwrite() != null && e.getOverwrite());
          listFiles.add(f.getAbsolutePath());
        } catch (StorageException ex) {
          String errMsg = String.format("Failed to export file %s! Error %s",
                  mip.getFilepath(), ex.getMessage());
          throw new InMailProcessException(
                  InMailProcessException.ProcessExceptionCode.ProcessException,
                  errMsg);
        }
      }
    }
    return listFiles;
  }

}

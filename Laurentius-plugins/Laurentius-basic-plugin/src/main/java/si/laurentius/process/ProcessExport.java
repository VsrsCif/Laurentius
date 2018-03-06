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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
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
public class ProcessExport extends AbstractMailProcessor {

  private static final SEDLogger LOG = new SEDLogger(ProcessExport.class);
  private static final String FORMAT_PART_NS = "PART-";

  public static final String KEY_EXPORT_METADATA = "imp.export.metadata";
  public static final String KEY_EXPORT_OVERWRITE = "imp.export.overwrite";
  public static final String KEY_EXPORT_METADATA_FILENAME = "imp.export.metadata.filename";
  public static final String KEY_EXPORT_FILEMASK = "imp.export.filemask";

  public static final String KEY_EXPORT_FOLDER = "imp.export.folder";
  public static final String KEY_EXPORT_SOURCE = "imp.export.source";
  public static final String KEY_PDF_JOIN = "imp.pdf.join.all";
  public static final String KEY_PDF_JOIN_FILENAMEMASK = "imp.pdf.join.filename.mask";

  StorageUtils msStorageUtils = new StorageUtils();

  @EJB
  private IMPDBInterface mDB;

  @Override
  public InMailProcessorDef getDefinition() {
    InMailProcessorDef impd = new InMailProcessorDef();
    impd.setType("export");
    impd.setName("Export processor");
    impd.setDescription("Export processor");
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_EXPORT_METADATA, "true", "Export metadata", true,
            PropertyType.Boolean.getType(), null, null));
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_EXPORT_OVERWRITE, "true", "Overwrite files  if exits.", true,
            PropertyType.Boolean.getType(), null, null));

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_EXPORT_METADATA_FILENAME, "metadata_${Id}.xml",
            "Metadata filename.",
            true,
            PropertyType.String.getType(), null, null));
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_EXPORT_FILEMASK,
            "${Id}_${" + FORMAT_PART_NS + "Id}_${" + FORMAT_PART_NS + "Filename}",
            "Filename mask.", true,
            PropertyType.String.getType(), null, null));
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_EXPORT_FOLDER,
            "${laurentius.home}/test-export/${SenderEBox}_${Service}_${Id}",
            "Export folder.", true,
            PropertyType.String.getType(), null, null));
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_EXPORT_SOURCE,
            null,
            "Export sources separated by comma  (mail,soap,xlst,...). If no comma is given all sources are exported",
            false,
            PropertyType.String.getType(), null, null));

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_PDF_JOIN,
            "false",
            "Join all pdfs into a signle pdf. If joinning not succeed - than all pdfs are exported seperately ",
            false,
            PropertyType.Boolean.getType(), null, null));
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_PDF_JOIN_FILENAMEMASK,
            "joined_${Id}.pdf",
            "Join filename mask if '" + KEY_PDF_JOIN + "' is true.", false,
            PropertyType.Boolean.getType(), null, null));

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

    List<String> lst = exportMail(map, mi);
    map.put(ProcessConstants.MP_EXPORT_FILES, lst);
    suc = true;

    LOG.logEnd(l, mi.getId());
    return suc;
  }

  public List<String> exportMail(Map<String, Object> map, MSHInMail inMail)
          throws InMailProcessException {

    String fileMask = (String) map.
            getOrDefault(KEY_EXPORT_FILEMASK,
                    "${" + FORMAT_PART_NS + "Id}_${" + FORMAT_PART_NS + "Filename}");

    String expFolderPath = (String) map.
            getOrDefault(KEY_EXPORT_FOLDER, "./");
    String metaDataFileName = (String) map.
            getOrDefault(KEY_EXPORT_METADATA_FILENAME, "metadata_${Id}.xml");

    String joinPDFFileNameMask = (String) map.
            getOrDefault(KEY_EXPORT_METADATA_FILENAME, "joined_${Id}.pdf");

    String sources = (String) map.get(KEY_EXPORT_SOURCE);
    List<String> lstSources = null;
    if (!Utils.isEmptyString(sources)) {
      String[] srcTbl = sources.split(",");
      lstSources = new ArrayList<>();
      for (String vl : srcTbl) {
        lstSources.add(vl.trim());
      }
    }

    boolean overwriteFiles = ((String) map.getOrDefault(KEY_EXPORT_OVERWRITE,
            "false")).equalsIgnoreCase("true");
    boolean exportMetaData = ((String) map.getOrDefault(KEY_EXPORT_METADATA,
            "false")).equalsIgnoreCase("true");

    boolean joinPDF = ((String) map.getOrDefault(KEY_PDF_JOIN,
            "false")).equalsIgnoreCase("true");

    expFolderPath = StringFormater.format(expFolderPath, inMail);
    File exportFolder = new File(expFolderPath);

    List<String> listFiles = new ArrayList<>();

    if (!exportFolder.exists() && !exportFolder.mkdirs()) {
      String errMsg = String.format(
              "Export failed! Could not create export folder '%s'!",
              expFolderPath);
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              errMsg);
    }

    MSHInMail cpMail = XMLUtils.deepCopyJAXB(inMail);
    /*
    if (joinPDF) {
      MSHInPart mip = joinPDFS()
    }*/

    for (MSHInPart mip : cpMail.getMSHInPayload().getMSHInParts()) {

      if (lstSources != null
              && mip.getSource() != null
              && !lstSources.contains(mip.getSource())) {
        // do not export source
        continue;
      }

      File f;
      String fileName = null;
      if (Utils.isEmptyString(fileMask)) {
        fileName = null;
      } else {
        fileName = StringFormater.format(fileMask, cpMail, FORMAT_PART_NS, mip);
      }

      try {
        f = msStorageUtils.copyFileToFolder(mip.getFilepath(), exportFolder,
                overwriteFiles, fileName);
        mip.setFilepath(f.getAbsolutePath());

        listFiles.add(f.getAbsolutePath());
      } catch (StorageException ex) {
        String errMsg = String.format("Failed to export file %s! Error %s",
                mip.getFilepath(), ex.getMessage());
        throw new InMailProcessException(
                InMailProcessException.ProcessExceptionCode.ProcessException,
                errMsg);
      }
    }

    if (exportMetaData) {
      String mdfFilename = StringFormater.format(metaDataFileName, cpMail);
      File fn = new File(exportFolder, mdfFilename);
      if (fn.exists() && overwriteFiles) {
        fn.delete();
      }

      try {

        XMLUtils.serialize(cpMail, fn);
        listFiles.add(fn.getAbsolutePath());

      } catch (JAXBException | FileNotFoundException ex) {
        String errMsg = String.format("Failed to serialize metadata: %s!",
                ex.getMessage());
        throw new InMailProcessException(
                InMailProcessException.ProcessExceptionCode.ProcessException,
                errMsg);

      }
    }

    return listFiles;
  }

}

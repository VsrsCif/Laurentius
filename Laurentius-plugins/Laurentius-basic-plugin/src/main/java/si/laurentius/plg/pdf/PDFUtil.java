/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.plg.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author logos
 */
public class PDFUtil {

  private static final SEDLogger LOG = new SEDLogger(PDFUtil.class);

  public synchronized PDFContentData concatenatePdfFiles(List<File> strFiles) throws PDFException {

    File outFile;
    try {
      outFile = File.createTempFile("concFile", ".pdf");
    } catch (IOException ex) {
      String strMessage = "Error occured while creating temp PDF file; Exception: " + ex.
              getMessage();
      LOG.logError(strMessage, ex);
      throw new PDFException(strMessage, ex);
    }

    PDFMergerUtility pdfUtility = new PDFMergerUtility();
    MemoryUsageSetting memUsageSettings = MemoryUsageSetting.setupMixed(1000000); // 1MB max memory usage
    memUsageSettings.setTempDir(new java.io.File(System.getProperty(
            "java.io.tmpdir"))); // To OS temp

    try (FileOutputStream fis = new FileOutputStream(outFile)) {
      for (File fIn : strFiles) {
        pdfUtility.addSource(fIn);
      }

      pdfUtility.mergeDocuments(memUsageSettings);

    } catch (IOException ex) {
      String strMessage = "Error occured while creating concatenated PDF file; Exception: " + ex.
              getMessage();
      LOG.logError(strMessage, ex);
      throw new PDFException(strMessage, ex);
    }

    PDFContentData mcd = new PDFContentData();
    mcd.setDocumentCount(strFiles.size());
    mcd.setPageCount(getFilePageCount(outFile));
    mcd.setTempFileName(outFile.getAbsolutePath());

    return mcd;
  }

  public int getFilePageCount(File f) throws PDFException {
    int iPageCnt = -1;
    try (FileInputStream fis = new FileInputStream(f)) {
      PDDocument doc = PDDocument.load(fis);
      iPageCnt = doc.getNumberOfPages();
    } catch (IOException ex) {
      String strMessage = "Error occured while creating concatenated PDF file; Exception: " + ex.
              getMessage();
      LOG.logError(strMessage, ex);
      throw new PDFException(strMessage, ex);
    }

    return iPageCnt;

  }

 
}

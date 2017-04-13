/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.meps.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;


/**
 *
 * @author logos
 */
public class PDFUtil {

    private final Logger mlgLogger = Logger.getLogger(PDFUtil.class.getName());

    public synchronized PDFContentData concatenatePdfFiles(List<File> strFiles) throws PDFException {
        PDFContentData mcd = new PDFContentData();
        if (strFiles == null || strFiles.isEmpty()) {
            mlgLogger.warn("PDFUtil:concatenatePdfFiles: no files to concenate!");
            mcd.setDocumentCount(0);
            mcd.setPageCount(0);
            return mcd;
        }
        File outFile = null;
        if (strFiles.size() == 1 && false) {
            outFile = strFiles.get(0);
            mcd.setDocumentCount(1);
            mcd.setTempFileName(outFile.getAbsolutePath());
            PdfReader reader = null;
            try {
                reader = new PdfReader(outFile.getAbsolutePath());
                // iText or adober reader  bug all names are  Signature1. AReader does not show 
                // all signatures ok
                changeAcroFieldNames(reader);

                mcd.setPageCount(reader.getNumberOfPages());
            } catch (IOException ex) {
                String strMessage = "Error reading file '" + outFile + "'!";
                mlgLogger.error("PDFUtil:concatenatePdfFiles: " + strMessage);
                throw new PDFException(strMessage, ex);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        } else {
            try {
                File fTmp = File.createTempFile("concFile", ".pdf");
                outFile = fTmp;
                //    try {
                int pageOffset = 0;
                ArrayList master = new ArrayList();
                int f = 0;
                Document document = null;
                PdfCopy writer = null;
                int iPageCount = 0;
                while (f < strFiles.size()) {
                    // we create a reader for a certain document
                    //System.out.print("strFiles: " + strFiles.get(f));
                    PdfReader reader = new PdfReader(strFiles.get(f).getAbsolutePath());
                    // iText or adober reader  bug all names are  Signature1. AReader does not show 
                    // all signatures ok
                    changeAcroFieldNames(reader);
                    reader.consolidateNamedDestinations();
                    // we retrieve the total number of pages
                    int n = reader.getNumberOfPages();
                    List bookmarks = SimpleBookmark.getBookmark(reader);
                    if (bookmarks != null) {
                        if (pageOffset != 0) {
                            SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
                        }
                        master.addAll(bookmarks);
                    }
                    pageOffset += n;

                    if (f == 0) {
                        // step 1: creation of a document-object
                        document = new Document(reader.getPageSizeWithRotation(1));
                        // step 2: we create a writer that listens to the document
                        writer = new PdfCopy(document, new FileOutputStream(outFile));
                        // step 3: we open the document
                        document.open();
                    }
                    // step 4: we add content
                    PdfImportedPage page;
                    for (int i = 0; i < n;) {
                        ++i;
                        page = writer.getImportedPage(reader, i);
                        writer.addPage(page);
                        iPageCount++;
                    }
                    PRAcroForm form = reader.getAcroForm();
                    if (form != null) {
                        writer.copyAcroForm(reader);
                    }
                    f++;
                    // add blank page!
                    if (iPageCount % 2 == 1) {
                        PdfReader pr = getEmptyPDFPage();
                        page = writer.getImportedPage(pr, 1);
                        writer.addPage(page);
                        iPageCount++;
                        pageOffset++;
                    }
                }
                if (!master.isEmpty()) {
                    writer.setOutlines(master);
                    // step 5: we close the document
                }
                document.close();
                mcd.setDocumentCount(strFiles.size());
                mcd.setPageCount(iPageCount);
                mcd.setTempFileName(outFile.getAbsolutePath());

            } catch (DocumentException ex) {
                String strMessage = "Error concenate PDFs, Bad pdf; Exception: " + ex.getMessage();
                mlgLogger.error("PDFUtil:concatenatePdfFiles: " + strMessage);
                throw new PDFException(strMessage, ex);
            } catch (IOException ex) {
                String strMessage = "Error concenate PDFs, Error readin file! Exception: " + ex.getMessage();
                mlgLogger.error("PDFUtil:concatenatePdfFiles: " + strMessage);
                throw new PDFException(strMessage, ex);
            }
        }
        return mcd;
    }
    
    private void changeAcroFieldNames(PdfReader reader){
         HashMap fields = reader.getAcroFields().getFields();       
            for (Iterator it = fields.keySet().iterator(); it.hasNext();) {
                Object fieldName = it.next();                
                if(fieldName!=null){
                   String sigName = fieldName.toString();
                   reader.getAcroFields().renameField(sigName, "sign-" +  UUID.randomUUID().toString());
                }
             
            }
    }

    public int getFilePageCount(File f) throws IOException {
        int iPageCnt = -1;
        try (FileInputStream fis = new FileInputStream(f)){
            PdfReader pdfReader = new PdfReader(fis);
            iPageCnt = pdfReader.getNumberOfPages();
            pdfReader.close();
        }

        return iPageCnt;

    }

   public void addBarCodeToFile(InputStream isPDF, String value, OutputStream os) throws IOException, PDFException {
        try {
            PdfReader pdfReader = new PdfReader(isPDF);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, os);
            PdfContentByte content = pdfStamper.getOverContent(1);
            Barcode128 code128 = new Barcode128();
            code128.setCode(value);
            Image bc = code128.createImageWithBarcode(content, null, null);
            bc.setRotationDegrees(90f);
            float fOffset = Utilities.millimetersToPoints(5f); // 
            Rectangle rc = pdfReader.getPageSizeWithRotation(1);
            bc.setAbsolutePosition(fOffset, rc.getHeight() - fOffset - bc.getWidth());
            content.addImage(bc);
            pdfStamper.close();
            pdfReader.close();
        } catch (DocumentException ex) {
            String strMessage = "Error adding barcode; Exception: " + ex.getMessage();
            throw new PDFException(strMessage, ex);
        }
    }

    private static PdfReader emptyPdfPage;

    private static synchronized PdfReader getEmptyPDFPage() throws IOException {
        if (emptyPdfPage == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            try {
                Document emptyDoc = new Document(PageSize.A4);
                PdfWriter w = PdfWriter.getInstance(emptyDoc,
                        baos);
                emptyDoc.open();
                emptyDoc.newPage();
                emptyDoc.add(Chunk.NEXTPAGE);

                emptyDoc.close();
            } catch (DocumentException ex) {
                ex.printStackTrace();
                throw new RuntimeException();
            }
            emptyPdfPage = new PdfReader(baos.toByteArray());
        }
        return new PdfReader(emptyPdfPage);
    }

   
    public void concenateTest(String outFile) {
        //PdfTextExtractor  pe = new

        PdfReader reader = null;
        try {
            reader = new PdfReader(outFile);
            PdfTextExtractor pe = new PdfTextExtractor(reader);
            int iVal = reader.getNumberOfPages();
            for (int i = 0; i < iVal; i++) {
                String strVal = pe.getTextFromPage(i + 1);
                System.out.println("PAGE " + i + "\n" + strVal);
                System.out.println("----------------------------------------------------------");
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception exc) {
                }
            }
        }

    }
}

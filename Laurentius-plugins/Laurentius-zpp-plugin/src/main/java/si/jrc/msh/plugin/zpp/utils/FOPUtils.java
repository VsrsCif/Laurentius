package si.jrc.msh.plugin.zpp.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.xml.sax.SAXException;
import si.laurentius.commons.exception.FOPException;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class FOPUtils {

  String mTransformationFolder;
  FopFactory mfopFactorory = null;
  File msfConfigFile;

  /**
   *
   * @param configfile
   * @param xsltFolder
   */
  public FOPUtils(File configfile, String xsltFolder) {
    msfConfigFile = configfile;
    mTransformationFolder = xsltFolder;

  }


  /*
   * public static void main(String... args) { try { File cfile = new
   * File("/sluzba/code/SVEV2.0/laurentius.home/svev/fop.xconf"); File fIn = new File(
   * "/sluzba/mag-naloga/izmenjava-podatkov/code/SVEVDemo/msh-AS4/msh/inbox/msh-as4/msh--653980083958689530-Request.xml"
   * ); File fOut = new File(
   * "/sluzba/mag-naloga/izmenjava-podatkov/code/SVEVDemo/msh-AS4/msh/inbox/msh-as4/msh--653980083958689530-Request.pdf"
   * ); String folder =
   * "/sluzba/mag-naloga/izmenjava-podatkov/code/SVEVDemo/msh-AS4/msh/config/xslt/";
   * 
   * FOPUtils fu = new FOPUtils(cfile, folder); / fu.generateVisualization(fIn, fOut, "",
   * FopTransformations.DeliveryNotification); } catch (FOPException ex) { ex.printStackTrace(); }
   */
  /**
   *
   * @param outMail
   * @param f
   * @param xslt
   * @param mime
   * @throws FOPException
   */
  public void generateVisualization(Object outMail, File f, FopTransformations xslt, String mime)
      throws FOPException {

    File fxslt = getTransformatinoFile(xslt);
    try (FileOutputStream fos = new FileOutputStream(f)) {
      StreamSource ssXslt = new StreamSource(fxslt);
      JAXBSource source = new JAXBSource(JAXBContext.newInstance(outMail.getClass()), outMail);

      generateVisualization(source, fos, ssXslt, mime);

    } catch (IOException | JAXBException ex) {
      String msg = "Error generating visualization:" + ex.getMessage();
      throw new FOPException(msg, ex);
    }
  }

  /**
   *
   * @param src
   * @param out
   * @param xslt
   * @param mime
   * @throws FOPException
   */
  public void generateVisualization(Source src, OutputStream out, Source xslt, String mime)
      throws FOPException {

    try {
      Fop fop = getFopFactory().newFop(mime, out);

      // Fop fop = getFopFactory().newFop(MimeConstants.MIME_PDF, out);
      TransformerFactory factory = TransformerFactory.newInstance();
      Templates template = factory.newTemplates(xslt);
      Transformer transformer = template.newTransformer();
      Result res = new SAXResult(fop.getDefaultHandler());
      transformer.transform(src, res);
    } catch (IOException | SAXException | TransformerException ex) {
      String msg = "Error generating visualization" + ex.getMessage();
      throw new FOPException(msg, ex);
    } finally {
      try {
        out.close();
      } catch (IOException ignore) {

      }
    }
  }

  /**
   *
   * @param src
   * @param out
   * @param xslt
   * @throws FOPException
   */
  public void generateVisualizationToHtml(Source src, OutputStream out, Source xslt)
      throws FOPException {

    try {

      TransformerFactory factory = TransformerFactory.newInstance();
      Templates template = factory.newTemplates(xslt);
      Transformer transformer = template.newTransformer();
      Result result = new StreamResult(out);
      transformer.transform(src, result);
    } catch (TransformerException ex) {
      String msg = "Error generating visualization" + ex.getMessage();
      throw new FOPException(msg, ex);
    } finally {
      try {
        out.close();
      } catch (IOException ignore) {

      }
    }
  }

  private FopFactory getFopFactory() throws SAXException, IOException {
    if (mfopFactorory == null) {
      mfopFactorory = FopFactory.newInstance(msfConfigFile);
    }
    return mfopFactorory;

  }

  /*
   * 
   * public byte[] generateVisualization(File inputRequest, String service, FopTransformations xslt)
   * throws FOPException { // get service + "get vssl" ByteArrayOutputStream bos = new
   * ByteArrayOutputStream(); Source src = new StreamSource(inputRequest);
   * generateVisualization(src, bos, new StreamSource(getTransformatinoFile(xslt, service))); return
   * bos.toByteArray(); }
   * 
   * 
   * 
   * public void generateVisualization(File fIn, File fOut, String service, FopTransformations xslt)
   * throws FOPException {
   * 
   * File tmplxslt = getTransformatinoFile(xslt, service); try (OutputStream out = new
   * FileOutputStream(fOut)) { generateVisualization(new StreamSource(fIn), out, new
   * StreamSource(tmplxslt)); } catch (IOException ex) { String msg =
   * "Error generating visualization: Error writing to file :'" + fOut + "'!"; throw new
   * FOPException(msg, ex); } }
   */
  private File getTransformatinoFile(FopTransformations xslt) {
    if (mTransformationFolder == null) {
      return new File(xslt.getFileName());
    }
    return new File(mTransformationFolder, xslt.getFileName());

  }

  /**
     *
     */
  public enum FopTransformations {

    /**
         *
         */
    DeliveryNotification("LegalDelivery_ZPP-DeliveryNotification.fo"),

    /**
         *
         */
    AdviceOfDelivery("LegalDelivery_ZPP-AdviceOfDelivery.fo"),

    /**
         *
         */
    AdviceOfDeliveryFiction("LegalDelivery_ZPP-AdviceOfDeliveryFiction.fo");

    private final String mstrfileName;

    FopTransformations(String filename) {
      mstrfileName = filename;
    }

    /**
     *
     * @return
     */
    public String getFileName() {
      return mstrfileName;
    }
  }

}

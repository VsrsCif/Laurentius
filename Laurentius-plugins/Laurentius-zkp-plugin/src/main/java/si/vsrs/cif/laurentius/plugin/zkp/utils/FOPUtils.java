package si.vsrs.cif.laurentius.plugin.zkp.utils;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.xml.sax.SAXException;
import si.vsrs.cif.laurentius.plugin.zkp.enums.FopTransformation;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.utils.SEDLogger;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class FOPUtils {

    private static SEDLogger LOG = new SEDLogger(FOPUtils.class);

    String mTransformationFolder;
    FopFactory mfopFactory = null;
    File msfConfigFile;

    /**
     *
     * @param configfile
     * @param xsltFolder
     */
    public FOPUtils(File configfile, String xsltFolder) {
        msfConfigFile = configfile;
        mTransformationFolder = xsltFolder;
        try {
            initFopFactory();
        } catch (SAXException | IOException ex) {
            LOG.logError("Error occured while initialize fop factory! Msg; "
                    + ex.getMessage(), ex);
        }

    }

    /**
     *
     * @param outMail
     * @param f
     * @param xslt
     * @param mime
     * @throws FOPException
     */
    public void generateVisualization(Object outMail, File f, FopTransformation xslt, String mime)
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

    private FopFactory getFopFactory()
            throws SAXException, IOException {
        if (mfopFactory == null) {
            initFopFactory();
        }
        return mfopFactory;
    }

    private void initFopFactory() throws SAXException, IOException {
        if (mfopFactory == null) {
            mfopFactory = FopFactory.newInstance(msfConfigFile);

            File fout = File.createTempFile("foptest", ".pdf");
            // init error happend first time
            /**
             * Caused by:null
             * java.awt.color.ICC_Profile.intFromBigEndian(ICC_Profile.java:1782)
             * java.awt.color.ICC_Profile.getNumComponents(ICC_Profile.java:1474)
             * org.apache.fop.pdf.PDFICCBasedColorSpace.<init>(PDFICCBasedColorSpace.java:49)
             * org.apache.fop.pdf.PDFFactory.makeICCBasedColorSpace(PDFFactory.java:1453)
             * org.apache.fop.pdf.PDFICCBasedColorSpace.setupsRGBAsDefaultRGBColorSpace(PDFICCBasedColorSpace.java:116)
             * org.apache.fop.render.pdf.PDFRenderingUtil.addsRGBColorSpace(PDFRenderingUtil.java:187)
             * org.apache.fop.render.pdf.PDFRenderingUtil.setupPDFDocument(PDFRenderingUtil.java:604)
             * org.apache.fop.render.pdf.PDFDocumentHandler.startDocument(PDFDocumentHandler.java:159)
             * org.apache.fop.render.intermediate.util.IFDocumentHandlerProxy.startDocument(IFDocumentHandlerProxy.java:105)
             * org.apache.fop.render.intermediate.IFRenderer.startRenderer(IFRenderer.java:263)
             * org.apache.fop.area.RenderPagesModel.<init>(RenderPagesModel.java:81)
             * org.apache.fop.area.AreaTreeHandler.setupModel(AreaTreeHandler.java:135)
             * org.apache.fop.area.AreaTreeHandler.<init>(AreaTreeHandler.java:105)
             * org.apache.fop.render.RendererFactory.createFOEventHandler(RendererFactory.java:363)
             * org.apache.fop.fo.FOTreeBuilder.<init>(FOTreeBuilder.java:107)
             * org.apache.fop.apps.Fop.createDefaultHandler(Fop.java:104)
             * org.apache.fop.apps.Fop.<init>(Fop.java:78)
             * org.apache.fop.apps.FOUserAgent.newFop(FOUserAgent.java:182)
             * org.apache.fop.apps.FopFactory.newFop(FopFactory.java:220)
             */
            try (FileOutputStream fos = new FileOutputStream(fout)) {
                Fop fop = mfopFactory.newFop(MimeValue.MIME_PDF.getMimeType(), fos);

                fout.delete();
            } catch (org.apache.fop.apps.FOPException | IOException ex) {
                LOG.formatedWarning("Error occured while initializing  FOP:  %s", ex.getMessage());
            }

        }
    }

    private File getTransformatinoFile(FopTransformation xslt) {
        if (mTransformationFolder == null) {
            return new File(xslt.getFileName());
        }
        return new File(mTransformationFolder, xslt.getFileName());

    }

}

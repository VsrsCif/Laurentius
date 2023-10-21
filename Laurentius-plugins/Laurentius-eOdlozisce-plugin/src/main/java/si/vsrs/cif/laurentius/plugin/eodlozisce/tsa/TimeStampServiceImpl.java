package si.vsrs.cif.laurentius.plugin.eodlozisce.tsa;


import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import si.src.setcce.sign.wsclient.AddXAdESTResult;
import si.src.setcce.sign.wsclient.SetcceSignServer;
import si.src.setcce.sign.wsclient.SetcceSignServerServiceLocator;
import si.src.setcce.sign.wsclient.VerifyResult;
import si.src.setccesign.SetcceConfig;

import javax.xml.rpc.ServiceException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
@Primary
//@ConditionalOnProperty(value="mock.timestamp", havingValue = "false", matchIfMissing = true)
public class TimeStampServiceImpl implements TimeStampService {

    private static final Logger logger = LoggerFactory.getLogger(TimeStampServiceImpl.class);
    public static final String SPLOSNA_VLOGA_ID = "splosnaVlogaId";
    public static final String VLOGA_ID_PREFIX = "_";

    private String type = "prod";

    private final String serverUrl;
    private final String signatureNamespace;
    private final Integer timeout;
//    private final String splosnaVlogaNamespace;
//    private final String vlogaMainElement;

    public TimeStampServiceImpl(final String serverUrl, final String signatureNamespace, final Integer timeout) {
        this.serverUrl = serverUrl;
        this.signatureNamespace = signatureNamespace;
        this.timeout = timeout;
//        this.splosnaVlogaNamespace = splosnaVlogaNamespace;
//        this.vlogaMainElement = vlogaMainElement;
    }

    @Override
    public String timeStampXml(String xml) throws TimestampException {
        logger.debug("TimeStampServiceImpl.timeStampXml");
        SetcceConfig conf = new SetcceConfig();
        conf.setTimeout(timeout);
        try {
            conf.setServerUrl(new URL(serverUrl));
            conf.setType(type);
            SetcceSignServer sign = new SetcceSignServerServiceLocator().getsetcceSignServerPort(conf.getServerUrl());
            byte[] xmlByte = getStringXMLWithoutSignature(xml);
            logger.debug("XML Without signature " + new String(xmlByte));
            byte[] signature = getSignature(xml);
            logger.debug("Only signature " + new String(signature));
            AddXAdESTResult xadesT = sign.addXAdEST(xmlByte, signature, SPLOSNA_VLOGA_ID);

            if (xadesT.getResultCode() != 0) {
                logger.error("Timestamp service returned error: code: {}; message: {}",
                        xadesT.getResultCode(), xadesT.getErrMsg());
                throw new TimestampException("Timestamp service returned error");
            }

            String retVal = new String(xadesT.getXML(), StandardCharsets.UTF_8);
            logger.debug("XML with timestamp " + retVal);
            validateIfXml(retVal);
            return retVal;
        } catch (Exception | TimestampException ex) {
            String message = "TimeStampServiceImpl.timeStampXml - (MalformedURLException)exception occurred " + ex.toString();
            logger.error(message, ex);
            // TODO new exception type again
            throw new TimestampException(message, ex);
        }
    }

    public VerifyResult verifyTimestampSignature(String xml) {
        SetcceConfig conf = new SetcceConfig();
        conf.setTimeout(timeout);
        try {
            conf.setServerUrl(new URL(serverUrl));
            conf.setType(type);

            SetcceSignServer sign = new SetcceSignServerServiceLocator().getsetcceSignServerPort(conf.getServerUrl());
            // TODO should signature be in, or not?
            final byte[] xmlBytesNoSignature = getStringXMLWithoutSignature(xml);
            final VerifyResult verify = sign.verify(xmlBytesNoSignature);
            return verify;
        } catch (ServiceException | TimestampException | IOException | TransformerException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateIfXml(String xml) throws IOException, SAXException {
        logger.debug("TimeStampServiceImpl.validateIfXml");
        DOMParser domParser = new DOMParser();
        StringReader sr = new StringReader(xml);
        InputSource is = new InputSource(sr);

        domParser.parse(is);
    }

    private String getDocumentPrefix(String xml) throws SAXException, IOException {
        logger.debug("TimeStampServiceImpl.getDocumentPrefix");
        DOMParser domParser = new DOMParser();
        StringReader sr = new StringReader(xml);
        InputSource is = new InputSource(sr);
        domParser.parse(is);
        return domParser.getDocument().getDocumentElement().getPrefix();
    }


    private byte[] getSignature(String xmlString) throws SAXException, IOException, TransformerException, TimestampException {
        logger.debug("TimeStampServiceImpl.getSignature");
        DOMParser domParser = new DOMParser();
        StringReader sr = new StringReader(xmlString);
        InputSource is = new InputSource(sr);
        domParser.parse(is);
        Document doc = domParser.getDocument();
        // timestamp does not exist at this moment, and I think that method removeTimestamp() is not working properly.
        // removeTimestamp(doc);

        Node signature = getLastSignatureNode(doc);
        if (signature == null) {
            String message = "Signature tag was not found in xml" + xmlString;
            logger.error(message);
            // TODO new exception type, again
            throw new TimestampException(message);
        }
        String xmlRetString = xmlToString(signature);
        // TODO UTF8Util
        return getUTF8Bytes(xmlRetString);
    }

    private byte[] getUTF8Bytes(final String xmlRetString) {
        return xmlRetString.getBytes(StandardCharsets.UTF_8);
    }


    public String xmlToString(Node node) throws TransformerException {
        logger.debug("TimeStampServiceImpl.xmlToString " + node.toString());
        Source source;
        if (node instanceof Document) {
            Document doc1 = (Document) node;
            source = new DOMSource(doc1.getDocumentElement());
        } else {
            source = new DOMSource(node);
        }
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.transform(source, result);
        return stringWriter.getBuffer().toString();
    }


    private byte[] getStringXMLWithoutSignature(String xmlString) throws SAXException, IOException, TransformerException, TimestampException {
        logger.debug("TimeStampServiceImpl.getStringXMLWithoutSignature ");
        DOMParser domParser = new DOMParser();
        StringReader sr = new StringReader(xmlString);
        InputSource is = new InputSource(sr);
        is.setEncoding("UTF-8");
        domParser.parse(is);
        Document doc = domParser.getDocument();

        Node signature = getLastSignatureNode(doc);
        if (signature == null) {
            String message = "Signature tag was not found in xml:" + xmlString;
            logger.error(message);
            // TODO new exception type
            throw new TimestampException(message);
        }
        Node parent = signature.getParentNode();
        parent.removeChild(signature);

        String xmlRetString = xmlToString(doc);
        return getUTF8Bytes(xmlRetString);
    }

    private void removeTimestamp(Document doc) throws DOMException {
        logger.debug("TimeStampServiceImpl.removeTimestamp");
        NodeList childrens = doc.getElementsByTagNameNS(signatureNamespace, "Signature");
        if (childrens != null && childrens.getLength() > 0) {
            //get the last signature
            Node signature = null;
            for (int is = 0; is < childrens.getLength(); is++) {
                signature = childrens.item(is);
                Node timestampNode = null;
                if (signature.getChildNodes() != null) {
                    for (int i = 0; i < signature.getChildNodes().getLength(); i++) {
                        if (signature.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                            if (signature.getChildNodes().item(i).getLocalName().equals("Object")) {
                                timestampNode = signature.getChildNodes().item(i);
                                break;
                            }
                        }
                    }
                }
                if (timestampNode != null) {
                    signature.removeChild(timestampNode);
                }
            }
        }
    }

    private Node getLastSignatureNode(Document doc) {
        NodeList childrens = doc.getElementsByTagNameNS(signatureNamespace, "Signature");

        if (childrens != null && childrens.getLength() > 0) {
            return childrens.item(childrens.getLength() - 1);
        } else {
            return null;
        }
    }
}

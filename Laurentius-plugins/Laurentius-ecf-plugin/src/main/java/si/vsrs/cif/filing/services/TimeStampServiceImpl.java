package si.vsrs.cif.filing.services;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.sodisce.splosnavloga.v2.SplosnaVloga;
import si.src.setcce.sign.wsclient.AddXAdESTResult;
import si.src.setcce.sign.wsclient.SetcceSignServer;
import si.src.setcce.sign.wsclient.SetcceSignServerServiceLocator;
import si.src.setccesign.SetcceConfig;
import si.vsrs.cif.filing.enums.EFCError;

import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;

import static si.vsrs.cif.filing.utils.ExceptionUtils.throwFault;

/**
 * Service is based on "current" sodisce portal class - eVlaganja izvrsba: TimeStampServiceImpl
 */
@Stateless
public class TimeStampServiceImpl {

    private static final SEDLogger LOG = new SEDLogger(TimeStampServiceImpl.class);
    public static final String SPLOSNA_VLOGA_ID = "splosnaVlogaId";

    public static final String SIGNATURE_NAMESPACE = "http://www.w3.org/2000/09/xmldsig#";
    public static final String SIGNATURE_ELEMENT = "Signature";

    public static final String TIMESTAMP_NAMESPACE = "hhttp://uri.etsi.org/01903/v1.1.1#";
    public static final String TIMESTAMP_ELEMENT = "SignatureTimeStamp";

    private String type = "prod";

    public byte[] timeStampXmlFile(File fileXML, String serverUrl, Integer timeout, String conversationId, String messageId, String senderId) {
        Document document = null;
        try {
            document = parseXMLFile(fileXML);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOG.logError(e.getMessage(), e);
            throwFault(messageId, EFCError.TIMESTAMP_ERROR, conversationId, senderId, ExceptionUtils.getRootCauseMessage(e));
        }

        return timeStampXmlDocument(document, serverUrl, timeout, conversationId, messageId, senderId);
    }

    public byte[] timeStampXmlDocument(Document document, String serverUrl, Integer timeout, String conversationId, String messageId, String senderId) {
        LOG.log("TimeStampServiceImpl.timeStampXml");

        SetcceConfig conf = new SetcceConfig();
        conf.setTimeout(timeout);
        try {
            conf.setServerUrl(new URL(serverUrl));
            conf.setType(type);
            SetcceSignServer sign = new SetcceSignServerServiceLocator().getsetcceSignServerPort(conf.getServerUrl());
            SignatureContainer docContainer = detachSignature(document, conversationId, messageId, senderId);
            LOG.log("XML Without signature " + new String(docContainer.getDocWithoutSignature()));
            LOG.log("Only signature " + new String(docContainer.getSignature()));
            AddXAdESTResult xadesT = sign.addXAdEST(docContainer.getDocWithoutSignature(), docContainer.getSignature(), SPLOSNA_VLOGA_ID);
            if (xadesT.getResultCode() != 0) {
                String message = "Timestamp service returned error: code: [" + xadesT.getResultCode() + "]; message:[" + xadesT.getErrMsg() + "]";
                throwFault(messageId, EFCError.TIMESTAMP_ERROR, conversationId, senderId, message);
            }
            LOG.log("XML with timestamp " + new String(xadesT.getXML()));
            validateIfXml(xadesT.getXML());
            return xadesT.getXML();
        } catch (Exception ex) {
            String message = "TimeStampServiceImpl.timeStampXml - (MalformedURLException)exception occurred " + ex.toString();
            throwFault(messageId, EFCError.TIMESTAMP_ERROR, conversationId, senderId, message);
        }
        return null;
    }

    public void validateIfXml(byte[] xml) throws JAXBException {
        LOG.log("TimeStampServiceImpl.validateIfXml");
        XMLUtils.deserialize(new ByteArrayInputStream(xml), SplosnaVloga.class);
    }

    public Document parseXMLFile(File fileXML) throws ParserConfigurationException, SAXException, IOException {
        LOG.log("TimeStampServiceImpl.getSignature");
        return  XMLUtils.deserializeToDom(fileXML);

    }

    /**
     * Method detaches the "last" signature element from the Document
     *
     * @param document
     * @param conversationId
     * @param messageId
     * @param senderId
     * @return SignatureContainer document without "last" signature and the signature as bytearrayws for the  setcce Timestamp service
     */
    private SignatureContainer detachSignature(final Document document, String conversationId, String messageId, String senderId) {
        // do not change original document
        Document clone = (Document) document.cloneNode(true);
        Node signature = getLastSignatureNode(clone);
        if (signature == null) {
            String message = "Signature tag was not found!";
            LOG.logError(message, null);
            throwFault(messageId, EFCError.TIMESTAMP_ERROR, conversationId, senderId, message);
        }
        Node parent = signature.getParentNode();
        parent.removeChild(signature);
        return new SignatureContainer(xmlToByteArray(signature, conversationId, messageId, senderId), xmlToByteArray(clone, conversationId, messageId, senderId));
    }

    public byte[] xmlToByteArray(Node node, String conversationId, String messageId, String senderId) {
        LOG.log("TimeStampServiceImpl.xmlToString " + node.toString());
        Source source = null;
        if (node instanceof Document) {
            Document doc1 = (Document) node;
            source = new DOMSource(doc1.getDocumentElement());
        } else {
            source = new DOMSource(node);
        }
        ByteArrayOutputStream xmlWriter = new ByteArrayOutputStream();
        Result result = new StreamResult(xmlWriter);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = factory.newTransformer();
            transformer.transform(source, result);
        } catch (TransformerException e) {
            LOG.logError(e.getMessage(), e);
            throwFault(messageId, EFCError.TIMESTAMP_ERROR, conversationId, senderId, ExceptionUtils.getRootCauseMessage(e));
        }
        return xmlWriter.toByteArray();
    }

    public Node getLastSignatureNode(Document doc) {
        NodeList children = doc.getElementsByTagNameNS(SIGNATURE_NAMESPACE, SIGNATURE_ELEMENT);

        if (children != null && children.getLength() > 0) {
            return children.item(children.getLength() - 1);
        } else {
            return null;
        }
    }

    public boolean hasTimestampNode(Document doc) {
        NodeList children = doc.getElementsByTagNameNS(TIMESTAMP_NAMESPACE, TIMESTAMP_ELEMENT);
        return children != null && children.getLength() > 0;
    }

    private static class SignatureContainer {
        byte[] signature;
        byte[] docWithoutSignature;

        public SignatureContainer(byte[] signature, byte[] docWithoutSignature) {
            this.signature = signature;
            this.docWithoutSignature = docWithoutSignature;
        }

        public byte[] getSignature() {
            return signature;
        }

        public byte[] getDocWithoutSignature() {
            return docWithoutSignature;
        }
    }
}

package si.sodisce.splosnaVloga.service;


import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import si.sodisce.splosnaVloga.exception.IntegrationException;
import si.sodisce.splosnaVloga.service.contract.TimeStampService;
import si.src.setcce.sign.wsclient.AddXAdESTResult;
import si.src.setcce.sign.wsclient.SetcceSignServer;
import si.src.setcce.sign.wsclient.SetcceSignServerServiceLocator;
import si.src.setccesign.SetcceConfig;
import si.src.vpisnik.middlelayer.core.vpisnik.common.exception.VpisnikException;
import si.src.vpisnik.middlelayer.core.vpisnik.common.util.ReflectionUtil;
import si.src.vpisnik.middlelayer.core.vpisnik.common.util.UTF8Util;

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
@ConditionalOnProperty(value="mock.timestamp", havingValue = "false", matchIfMissing = true)
public class TimeStampServiceImpl implements TimeStampService {

    private static final Logger logger = LoggerFactory.getLogger(TimeStampServiceImpl.class);
    public static final String SPLOSNA_VLOGA_ID = "splosnaVlogaId";
    public static final String VLOGA_ID_PREFIX = "_";

    private String type = "prod";

    @Value("${service.timestamp.url}")
    private String serverUrl;

    @Value("${service.timestamp.signature-namespace}")
    private String signatureNamespace;

    @Value("${service.timestamp.timeout}")
    private Integer timeout;

    @Value("${service.timestamp.vloga-namespace}")
    private String splosnaVlogaNamespace;

    @Value("${service.timestamp.main-element}")
    private String vlogaMainElement;


    @Override
    public String timeStampXml(String xml) throws IntegrationException {
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
                throw new IntegrationException(IntegrationException.ERRMSG_TIMESTAMP);
            }

            logger.debug("Return value " + ReflectionUtil.writeObjectProperties(xadesT));
            String retVal = new String(xadesT.getXML(), StandardCharsets.UTF_8);
            logger.debug("XML with timestamp " + retVal);
            validateIfXml(retVal);
            return retVal;
        } catch (Exception ex) {
            String message = "TimeStampServiceImpl.timeStampXml - (MalformedURLException)exception occurred " + ex.toString();
            logger.error(message, ex);
            throw new IntegrationException(IntegrationException.ERRMSG_TIMESTAMP, ex);
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


    private byte[] getSignature(String xmlString) throws SAXException, IOException, TransformerException, VpisnikException {
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
            throw new VpisnikException(VpisnikException.ERRCODE_TIMESTAMP, message);
        }
        String xmlRetString = xmlToString(signature);
        return UTF8Util.getUTF8Bytes(xmlRetString);
    }


    public String xmlToString(Node node) throws TransformerException {
        logger.debug("TimeStampServiceImpl.xmlToString " + node.toString());
        Source source = null;
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


    private byte[] getStringXMLWithoutSignature(String xmlString) throws SAXException, IOException, TransformerException, VpisnikException {
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
            throw new VpisnikException(VpisnikException.ERRCODE_TIMESTAMP, message);
        }
        Node parent = signature.getParentNode();
        parent.removeChild(signature);

        String xmlRetString = xmlToString(doc);
        return UTF8Util.getUTF8Bytes(xmlRetString);
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

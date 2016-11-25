/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.lce.sign.xml;

import si.laurentius.lce.sign.xml.XMLSignatureUtils;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import si.laurentius.commons.utils.Utils;
import static si.laurentius.lce.sign.xml.XMLSignatureUtils.XML_SIGNATURE_PROVIDER_PROP;
import static si.laurentius.lce.sign.xml.XMLSignatureUtils.XML_SIGNATURE_PROVIDER_VALUE_1;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.w3c.dom.NodeList;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.utils.xml.SchemaErrorHandler;
import si.laurentius.lce.DigestMethodCode;
import si.laurentius.lce.utils.TestUtils;

/**
 *
 * @author Jože Rihtaršič
 */
public class XMLSignatureUtilsTest {

  TestUtils mtuUtils = new TestUtils();

  public XMLSignatureUtilsTest() {
  }

  /**
   * Test of getXMLSignatureFactory method, of class XMLSignatureUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGetXMLSignatureFactory()
      throws Exception {

    XMLSignatureUtils instance = new XMLSignatureUtils();
    XMLSignatureFactory result = instance.getXMLSignatureFactory();

    System.setProperty(XML_SIGNATURE_PROVIDER_PROP,
        XML_SIGNATURE_PROVIDER_VALUE_1);

    assertNotNull(result);

    System.setProperty(XML_SIGNATURE_PROVIDER_PROP,
        XMLSignatureUtils.XML_SIGNATURE_PROVIDER_VALUE_2);

    XMLSignatureFactory result2 = instance.getXMLSignatureFactory();
    assertNotNull(result2);

  }

  /**
   * Test of createXAdESEnvelopedSignature method, of class XMLSignatureUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testCreateXAdESEnvelopedSignature()
      throws Exception {
    System.out.println("createXAdESEnvelopedSignature");
    // create XML
    // root elements
    Document doc = createSignedDocument();
    NodeList seLst = doc.getElementsByTagName("Signatures");
    assertTrue(seLst.getLength() == 1);
    Element sigEl = (Element) seLst.item(0);

    NodeList nlst = doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
    // test if signature is on defined parent element
    assertTrue(sigEl.getChildNodes().getLength() == 1);
    assertTrue(doc.getElementsByTagName("Signature").getLength() != 0);
    assertEquals(nlst.item(0), sigEl.getChildNodes().item(0));

    Element sig = (Element) nlst.item(0);
    // validate signature by schema
    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = sf.newSchema(getClass().getResource("/xsd/xmldsig-core-schema.xsd"));
    SchemaErrorHandler vec = new SchemaErrorHandler();

    Validator validator = schema.newValidator();
    validator.setErrorHandler(vec);
    DOMSource source = new DOMSource(sig);
    validator.validate(source);

    assertTrue(vec.getFatalErrors().isEmpty());
    assertTrue(vec.getErrors().isEmpty());
    // check if signature has XAdES QualifyingProperties
    NodeList nlstQP = doc.getElementsByTagNameNS("http://uri.etsi.org/01903/v1.1.1#",
        "QualifyingProperties");
    assertTrue(nlstQP.getLength() == 1);

    // validate   XAdES QualifyingProperties by schema
    Schema schemaXAdES = sf.newSchema(getClass().getResource("/xsd/xmldsig-core-schema.xsd"));
    SchemaErrorHandler vecXAdES = new SchemaErrorHandler();
    Validator validatorXAdES = schemaXAdES.newValidator();
    validatorXAdES.setErrorHandler(vecXAdES);
    DOMSource sourceXAdES = new DOMSource(nlstQP.item(0));
    validator.validate(sourceXAdES);

    assertTrue(vecXAdES.getFatalErrors().isEmpty());
    assertTrue(vecXAdES.getErrors().isEmpty());

  }

  /**
   * Test of createXAdESEnvelopedSignature method, of class XMLSignatureUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testValidateXAdESEnvelopedSignature()
      throws Exception {
/* TODO !!
    Document doc = createSignedDocument();
    NodeList nlst = doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
    assertTrue(nlst.getLength() == 1);
    Element sigEl = (Element) nlst.item(0);

    XMLSignatureUtils instance = new XMLSignatureUtils();
    Exception ex = null;
    try {
      instance.validateXAdESEnvelopedSignature(sigEl);
    } catch (MarshalException | XMLSignatureException | SEDSecurityException exth) {
      ex = exth;
    }
    assertNull(ex);
    
    // invalidate message     
    NodeList dataLst = doc.getElementsByTagName("Data");
    assertTrue(dataLst.getLength() == 1);
    Element dataEl = (Element) dataLst.item(0);
    dataEl.appendChild(doc.createTextNode("Test signed data"));
    
    
    Exception expX = null;
    try {
      instance.validateXAdESEnvelopedSignature(sigEl);
    } catch(XMLSignatureException xexcp){
      expX = xexcp;
    }
    catch (MarshalException |  SEDSecurityException exth) {
      fail("Signature testing faile due to error: " + exth);
    }
    assertNotNull(expX);
    */

  }

  private Document createSignedDocument()
      throws SEDSecurityException, KeyStoreException, NoSuchAlgorithmException,
      UnrecoverableKeyException, ParserConfigurationException {
    List<String> strIds = new ArrayList<>();

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    // root elements
    Document doc = docBuilder.newDocument();
    Element rootElement = doc.createElement("SignatureTest");
    doc.appendChild(rootElement);
    // create Data elemenet 
    Element data = doc.createElement("Data");
    // set id

    String dataId = Utils.getUUID("SigId");
    strIds.add(dataId);
    data.setAttribute("Id", dataId);
    // set data to sign
    data.appendChild(doc.createTextNode("Test signed data"));
    rootElement.appendChild(data);
    // create signature holder
    Element sigEl = doc.createElement("Signatures");
    rootElement.appendChild(sigEl);

    KeyStore.PrivateKeyEntry certPrivateKey = mtuUtils.getTestPrivateKey();

    DigestMethodCode digestMethodCode = DigestMethodCode.SHA1;
    String sigMethod = SignatureMethod.RSA_SHA1;
    XMLSignatureUtils instance = new XMLSignatureUtils();
    instance.createXAdESEnvelopedSignature(certPrivateKey, sigEl, strIds,
        digestMethodCode, sigMethod, "Test reason");

    return doc;
  }

}

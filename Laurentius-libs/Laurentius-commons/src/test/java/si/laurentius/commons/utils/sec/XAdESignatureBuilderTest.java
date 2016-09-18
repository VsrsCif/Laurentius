/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.utils.sec;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.etsi.uri._01903.v1_1.QualifyingProperties;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import si.laurentius.commons.utils.TestUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.SchemaErrorHandler;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jože Rihtaršič
 */
public class XAdESignatureBuilderTest {
  TestUtils mtuUtils = new TestUtils();

  public XAdESignatureBuilderTest() {
  }

  /**
   * Test of createXAdESQualifyingProperties method, of class XAdESignatureBuilder.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testCreateXAdESQualifyingProperties()
      throws Exception {
    // sign key cert
    X509Certificate cert = mtuUtils.getTestCertificate();
    assertNotNull("Initialize error: Test cert not found!", cert);

    //  test for all digest codes
    for (DigestMethodCode dmc : DigestMethodCode.values()) {

      String sigId = Utils.getUUID("SigId");
      String strSigValId = Utils.getUUID("SigValId");
      String strSigPropId = Utils.getUUID("SigPropId");

      String signaturePolicy = "test signature";
      String sigCity = "Maribor";
      String sigCountryName = "Slovenia";
      XAdESignatureBuilder instance = new XAdESignatureBuilder();

      QualifyingProperties result =
          instance.createXAdESQualifyingProperties(sigId, strSigValId, strSigPropId, cert, dmc,
              signaturePolicy, sigCity, sigCountryName);

      JAXBContext jc = JAXBContext.newInstance(QualifyingProperties.class);
      JAXBSource source = new JAXBSource(jc, result);

      SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = sf.newSchema(getClass().getResource("/xsd/XAdES-1.1.1.xsd"));
      SchemaErrorHandler vec = new SchemaErrorHandler();

      Validator validator = schema.newValidator();
      validator.setErrorHandler(vec);
      validator.validate(source);

      assertTrue(vec.getFatalErrors().isEmpty());
      assertTrue(vec.getErrors().isEmpty());
    }

  }
  


}

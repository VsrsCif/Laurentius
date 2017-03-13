/*
 * Copyright 2016, Supreme Court Republic of Slovenia
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
package si.laurentius.process.xslt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Jože Rihtaršič
 */
public class XSLTUtils {
  
  public static synchronized void transform(Document source, InputStream xsltSource, File fileResult)
      throws TransformerConfigurationException, JAXBException, TransformerException,
      ParserConfigurationException, SAXException, IOException {

    TransformerFactory factory = TransformerFactory.newInstance();
    //TransformerFactoryImpl factory = new TransformerFactoryImpl();
    Transformer transformer = factory.newTransformer(new StreamSource(xsltSource));
    transformer.transform(new DOMSource(source), new StreamResult(fileResult));

  }
  
  public static synchronized void transform(Document source, File xsltFile, File fileResult)
      throws TransformerConfigurationException, JAXBException, TransformerException,
      ParserConfigurationException, SAXException, IOException {

    
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer(new StreamSource(xsltFile));
    transformer.setURIResolver(new XsltURIResolver(xsltFile.getParentFile()));
    transformer.transform(new DOMSource(source), new StreamResult(fileResult));

  }
 
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.process.xslt;

import java.util.Objects;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author sluzba
 */
public class XPathUtils {
  
  private static final SEDLogger LOG = new SEDLogger(XPathUtils.class);

  public XPath createXPathFromNSContext(XSLTNamespaceContext ctx) {
    XPathFactory factory = XPathFactory.newInstance();
    XPath path = factory.newXPath();
    path.setNamespaceContext(ctx);
    return path;
  }

  public boolean doesRuleApply(Document doc, XSLTNamespaceContext ctx, String expression,
      String value)
      throws XPathExpressionException {
    XPathFactory factory = XPathFactory.newInstance();
    XPath path = factory.newXPath();
    path.setNamespaceContext(ctx);
    return doesRuleApply(doc, path, expression, value);
  }

  public boolean doesRuleApply(Document doc, XPath path, String expression, String value)
      throws XPathExpressionException {
    LOG.formatedlog("doesRuleApply for expression %s, value %s ", expression, value);    
    Node node = (Node) path.evaluate(expression, doc, XPathConstants.NODE);
    if (node != null) {
      LOG.formatedlog("Node value: '%s' value '%s'", node.getNodeValue(), value);
    } else {
      LOG.log("NODE IS NULL");
    }
    
    return node != null && Objects.equals(node.getNodeValue(), value);
  }
/*
  public void evaluateXPath() {
    try {
      File fXmlFile = new File("test.xml");

      String xpath = "//izv:OpisPosiljke/@schemaVersion";
      Document doc = XMLUtils.deserializeToDom(fXmlFile);
      XPathFactory factory = XPathFactory.newInstance();
      XPath path = factory.newXPath();
//      path.setNamespaceContext(ctx);

      Node node = (Node) path.evaluate(xpath, doc, XPathConstants.NODE);
      System.out.println("Node: " + node);

      if (node == null) {

        System.out.println("NOT EXISTS");
      } else {
        System.out.println("EXISTS: " + node.getNodeValue());
      }
      // I exist!
    } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException ex) {
      ex.printStackTrace();
    }

  }
*/
}

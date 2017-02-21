/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.process.xslt;

import java.io.File;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author sluzba
 */
class XsltURIResolver implements URIResolver {

  File rootFolder;

  public XsltURIResolver(File folder) {
    rootFolder = folder;
  }

  @Override
  public Source resolve(String href, String base)
      throws TransformerException {
    File fxlst = new File(rootFolder, href);
    if (fxlst.exists()) {
      return new StreamSource(fxlst);
    }
    return null;

  }
}

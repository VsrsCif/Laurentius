/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.zpp.doc;

/**
 *
 * @author logos
 */
public class DocumentBuilderFactory {

  /**
   *
   * @param strVal
   * @return
   */
  public static DocumentBuilder getDocumentBuilder(String strVal) {
    DocumentBuilder db = null;

    if (strVal != null && strVal.equalsIgnoreCase(DocumentBuilder.CREA_V1)) {
      db = new DocumentEVemBuilder();
    } else {
      db = new DocumentSodBuilder();
    }
    return db;
  }

}

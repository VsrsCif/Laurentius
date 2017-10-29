package si.laurentius.msh.web.gui.dlg;

import java.util.List;
import java.util.Objects;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import org.primefaces.context.RequestContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.pmode.XPath;
import si.laurentius.msh.web.abst.AbstractJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("dialogXPath")
public class DialogXPath extends AbstractJSFView {

  private static final SEDLogger LOG = new SEDLogger(DialogXPath.class);

  int editIndex = -1;
  XPath editable;
  List<XPath> originXPathList;
  String updateTableId;
  XPath.Namespace selectedNamespace;

  public XPath getEditable() {
    return editable;
  }

  public void createNewXPath(List<XPath> originList) {
    XPath pth = new XPath();
    setEditable(pth, originList);
  }

  public void setEditable(XPath editable, List<XPath> originList) {
    assert editable != null : "Editable xpath is null";
    assert originList != null : "Editable originList is null";

    originXPathList = originList;
    editIndex = originList.indexOf(editable);
    // if not new  - clone object
    this.editable = editIndex >= 0 ? XMLUtils.deepCopyJAXB(editable) : editable;
  }

  public String getUpdateTableId() {
    return updateTableId;
  }

  public void setUpdateTableId(String updateTableId) {
    this.updateTableId = updateTableId;
  }

  public XPath.Namespace getSelectedNamespace() {
    return selectedNamespace;
  }

  public void setSelectedNamespace(XPath.Namespace selectedNamespace) {
    this.selectedNamespace = selectedNamespace;
  }

  public void addOrUpdateEditable() {
    if (editable != null) {
      if (editIndex >= 0) {
        originXPathList.remove(editIndex);
        originXPathList.add(editIndex, editable);
        editable = null;
      } else {
        originXPathList.add(editable);
        editable = null;
      }
    }
    RequestContext.getCurrentInstance().addCallbackParam("saved", true);

  }

  public void createNamespace() {
    XPath ed = getEditable();
    if (ed != null) {
      String sbname = "ns%d";
      int i = 1;
      while (namespacePrefixExists(String.format(sbname, i))) {
        i++;
      }
      XPath.Namespace ns = new XPath.Namespace();
      ns.setPrefix(String.format(sbname, i));
      ns.setNamespace("http://");
      ed.getNamespaces().add(ns);
      setSelectedNamespace(ns);
    } else {
      LOG.logWarn("No editable XPath setted!", null);
    }
  }

  public void removeSelectedNamespace() {
    XPath ed = getEditable();
    if (ed != null) {
      XPath.Namespace ns = getSelectedNamespace();
      if (ns != null) {
        ed.getNamespaces().remove(ns);
        setSelectedNamespace(null);
      } else {
        LOG.logWarn("No selected Namespace to delete!", null);
      }

    } else {
      LOG.logWarn("No editable XPath setted!", null);
    }
  }

  private boolean namespacePrefixExists(String ns) {
    XPath ed = getEditable();
    if (ed != null) {
      for (XPath.Namespace xns : ed.getNamespaces()) {
        if (Objects.equals(ns, xns.getPrefix())) {
          return true;
        }
      }
    } else {
      LOG.logWarn("No editable XPath setted!", null);
    }
    return false;
  }

}

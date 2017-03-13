package si.laurentius.plg.web;

import java.util.List;
import java.util.Objects;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.context.RequestContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.plugin.imp.Namespace;
import si.laurentius.plugin.imp.XPath;
import si.laurentius.plugin.imp.XSLTRule;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "dialogXPath")
public class DialogXPath {

  private static final SEDLogger LOG = new SEDLogger(DialogXPath.class);

  int editIndex = -1;
  XSLTRule editable;
  List<XSLTRule> originXPathList;
  String updateTableId;
  Namespace selectedNamespace;

  public XSLTRule getEditable() {
    return editable;
  }

  public void createNewXPath(List<XSLTRule> originList) {
    XPath pth = new XPath();
    XSLTRule xr = new XSLTRule();
    xr.setXPath(pth);

    setEditable(xr, originList);
  }

  public void setEditable(XSLTRule editable, List<XSLTRule> originList) {
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

  public Namespace getSelectedNamespace() {
    return selectedNamespace;
  }

  public void setSelectedNamespace(Namespace selectedNamespace) {
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
    XSLTRule ed = getEditable();
    if (ed != null) {
      if (ed.getXPath() == null) {
        ed.setXPath(new XPath());
      }
      String sbname = "ns%d";
      int i = 1;
      while (namespacePrefixExists(String.format(sbname, i))) {
        i++;
      }
      Namespace ns = new Namespace();
      ns.setPrefix(String.format(sbname, i));
      ns.setNamespace("http://");
      ed.getXPath().getNamespaces().add(ns);
      setSelectedNamespace(ns);
    } else {
      LOG.logWarn("No editable XPath setted!", null);
    }
  }

  public void removeSelectedNamespace() {
    XSLTRule ed = getEditable();
    if (ed != null) {
      if (ed.getXPath() == null) {
        ed.setXPath(new XPath());
      }

      Namespace ns = getSelectedNamespace();
      if (ns != null) {
        ed.getXPath().getNamespaces().remove(ns);
        setSelectedNamespace(null);
      } else {
        LOG.logWarn("No selected Namespace to delete!", null);
      }

    } else {
      LOG.logWarn("No editable XPath setted!", null);
    }
  }

  private boolean namespacePrefixExists(String ns) {
    XSLTRule ed = getEditable();
    if (ed != null) {
      if (ed.getXPath() == null) {
        ed.setXPath(new XPath());
      }
      for (Namespace xns : ed.getXPath().getNamespaces()) {
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

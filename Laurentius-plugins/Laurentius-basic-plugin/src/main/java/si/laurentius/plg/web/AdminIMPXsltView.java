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
package si.laurentius.plg.web;

import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.plg.db.IMPDBInterface;
import si.laurentius.plugin.imp.IMPXslt;
import si.laurentius.plugin.imp.Namespace;
import si.laurentius.plugin.imp.XPathRule;


/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminIMPXsltView")
public class AdminIMPXsltView extends AbstractAdminJSFView<IMPXslt> {
  
  private static final SEDLogger LOG = new SEDLogger(AdminIMPXsltView.class);
  
  @EJB
  private IMPDBInterface mDB;
   Namespace selectedNamespace = null;
  XPathRule selectedPathRule = null;
  @Override
  public void createEditable() {
    IMPXslt imp = new IMPXslt();
    int i = 1;
    String base = "xslt_%03d";
    while (mDB.getExport(String.format(base, i)) != null) {
      i++;
    }
    imp.setInstance(String.format(base, i));
    
    
    setNew(imp);
  }
  public void createNamespace() {

    if (getEditable()!= null) {

      String sbname = "ns%d";
      int i = 1;
      while (namespacePrefixExists(String.format(sbname, i)) ) {
        i++;
      }
      Namespace ns = new Namespace();
      ns.setPrefix(String.format(sbname, i));
      getEditable().getNamespaces().add(ns);
    }
  }
  public void createXPathRule() {
    if (getEditable() != null) {
      getEditable().getXPathRules().add(new XPathRule());
    }
  }
  @Override
  public List<IMPXslt> getList() {
    return mDB.getXSLTs();
  }

  public Namespace getSelectedNamespace() {
    return selectedNamespace;
  }


  public XPathRule getSelectedPathRule() {
    return selectedPathRule;
  }
  public XPathRule getSelectedXPathRule() {
    return selectedPathRule;
  }

  public boolean namespacePrefixExists(String val) {
    boolean bExists = false;
    if (getEditable() != null) {
      for (Namespace ns : getEditable().getNamespaces()) {
        if (Objects.equals(ns.getPrefix(), val)) {
          bExists = true;
          break;
        }
      }
    }
    return bExists;

  }
  @Override
  public boolean persistEditable() {
    return mDB.addXSLT(getEditable());
  }
  @Override
  public void removeSelected() {
    mDB.removeXSLT(getEditable());
  }

  public void removeSelectedNamespace() {
    if (getSelectedNamespace() != null && getEditable() != null) {
      List<Namespace> lst = getEditable().getNamespaces();
      for (Namespace ns : lst) {
        if (Objects.equals(ns, getSelectedNamespace())) {
          lst.remove(ns);
          break;
        }
      }

    }
  }


  public void removeSelectedXPathRule() {
    if (getSelectedXPathRule() != null && getEditable() != null) {
      List<XPathRule> lst = getEditable().getXPathRules();
      for (XPathRule xp : lst) {
        if (Objects.equals(xp, getSelectedXPathRule())) {
          lst.remove(xp);
          break;
        }
      }

    }
  }
  public void setSelectedNamespace(Namespace selectedNamespace) {
    this.selectedNamespace = selectedNamespace;
  }
  public void setSelectedPathRule(XPathRule selectedPathRule) {
    this.selectedPathRule = selectedPathRule;
  }
  public void setSelectedXPathRule(XPathRule selectedPathRule) {
    this.selectedPathRule = selectedPathRule;
  }
  
  
  @Override
  public boolean updateEditable() {
    return mDB.updateXSLT(getEditable());
  }
  @Override
  public boolean validateData() {
    if (Utils.isEmptyString(getEditable().getInstance())) {
      addError("Instance parametere must not be null!");
      return false;
    }
    
    if (isEditableNew() && mDB.getExport(getEditable().getInstance()) != null) {
      addError("Instance parametere must not be unique!");
      return false;
    }
    return true;
  }
  
}

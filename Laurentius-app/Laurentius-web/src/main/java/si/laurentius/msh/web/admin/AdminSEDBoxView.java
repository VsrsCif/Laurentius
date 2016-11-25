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
package si.laurentius.msh.web.admin;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.ebox.Execute;
import si.laurentius.ebox.Export;
import si.laurentius.ebox.SEDBox;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.xslt.Namespace;
import si.laurentius.xslt.SEDXslt;
import si.laurentius.xslt.XPathRule;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDBoxView")
public class AdminSEDBoxView extends AbstractAdminJSFView<SEDBox> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDBoxView.class);

  @EJB(mappedName = SEDJNDI.JNDI_DBSETTINGS)
  private DBSettingsInterface mdbSettings;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPMode;

  Namespace selectedNamespace = null;
  XPathRule selectedPathRule = null;

  /**
   *
   * @param sedBox
   * @return
   */
  public SEDBox getSEDBoxByLocalName(String sedBox) {
    return mdbLookups.getSEDBoxByAddressName(sedBox);
  }

  /**
   *
   */
  @Override
  public void createEditable() {
    long l = LOG.logStart();

    String sbname = "name.%03d";
    int i = 1;
    while (getSEDBoxByLocalName(String.format(sbname, i)) != null) {
      i++;
    }
    SEDBox sbx = new SEDBox();
    sbx.setLocalBoxName(String.format(sbname, i));
    sbx.setActiveFromDate(Calendar.getInstance().getTime());
    sbx.setExport(new Export());
    sbx.setExecute(new Execute());
    sbx.setXSLT(new SEDXslt());
    setNew(sbx);
    LOG.logEnd(l);
  }

  /**
   *
   */
  @Override
  public void removeSelected() {
    SEDBox sb = getSelected();
    if (sb != null) {

      mdbLookups.removeSEDBox(sb);
      setSelected(null);

    }

  }

  /**
   *
   */
  @Override
  public void startEditSelected() {
    if (getSelected() != null && getSelected().getExport() == null) {
      getSelected().setExport(new Export());
    }
    if (getSelected() != null && getSelected().getExecute() == null) {
      getSelected().setExecute(new Execute());
    }
    super.startEditSelected();
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    SEDBox sb = getEditable();
    boolean bsuc = false;
    if (sb != null) {
      mdbLookups.addSEDBox(sb);
      setEditable(null);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {

    SEDBox sb = getEditable();
    boolean bsuc = false;
    if (sb != null) {
      mdbLookups.updateSEDBox(sb);
      setEditable(null);
      return bsuc;

    }
    return bsuc;
  }

  @Override
  public boolean validateData() {

    return true;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDBox> getList() {
    return mdbLookups.getSEDBoxes();
  }

  public SEDXslt getCurrentXSLT() {
    SEDXslt res = null;
    if (getEditable() != null) {
      if (getEditable().getXSLT() == null) {
        getEditable().setXSLT(new SEDXslt());
      }
      res = getEditable().getXSLT();
    }
    return res;
  }

  public Execute getCurrentExecution() {
    Execute res = null;
    if (getEditable() != null) {
      if (getEditable().getExecute() == null) {
        getEditable().setExecute(new Execute());
      }
      res = getEditable().getExecute();
    }
    return res;
  }

  public Export getCurrentExport() {
    Export res = null;
    if (getEditable() != null) {
      if (getEditable().getExport() == null) {
        getEditable().setExport(new Export());
      }
      res = getEditable().getExport();
    }
    return res;
  }

  public Namespace getSelectedNamespace() {
    return selectedNamespace;
  }

  public void setSelectedNamespace(Namespace selectedNamespace) {
    this.selectedNamespace = selectedNamespace;
  }

  public void createNamespace() {

    if (getCurrentXSLT() != null) {

      String sbname = "ns%d";
      int i = 1;
      while (namespacePrefixExists(String.format(sbname, i)) ) {
        i++;
      }
      Namespace ns = new Namespace();
      ns.setPrefix(String.format(sbname, i));
      getCurrentXSLT().getNamespaces().add(ns);
    }
  }

  public boolean namespacePrefixExists(String val) {
    boolean bExists = false;
    if (getCurrentXSLT() != null) {
      for (Namespace ns : getCurrentXSLT().getNamespaces()) {
        if (Objects.equals(ns.getPrefix(), val)) {
          bExists = true;
          break;
        }
      }
    }
    return bExists;

  }

  public void removeSelectedNamespace() {
    if (getSelectedNamespace() != null && getCurrentXSLT() != null) {
      List<Namespace> lst = getCurrentXSLT().getNamespaces();
      for (Namespace ns : lst) {
        if (Objects.equals(ns, getSelectedNamespace())) {
          lst.remove(ns);
          break;
        }
      }

    }
  }

  public XPathRule getSelectedXPathRule() {
    return selectedPathRule;
  }

  public void setSelectedXPathRule(XPathRule selectedPathRule) {
    this.selectedPathRule = selectedPathRule;
  }

  public void createXPathRule() {
    if (getCurrentXSLT() != null) {
      getCurrentXSLT().getXPathRules().add(new XPathRule());
    }
  }

  public void removeSelectedXPathRule() {
    if (getSelectedXPathRule() != null && getCurrentXSLT() != null) {
      List<XPathRule> lst = getCurrentXSLT().getXPathRules();
      for (XPathRule xp : lst) {
        if (Objects.equals(xp, getSelectedXPathRule())) {
          lst.remove(xp);
          break;
        }
      }

    }
  }

}

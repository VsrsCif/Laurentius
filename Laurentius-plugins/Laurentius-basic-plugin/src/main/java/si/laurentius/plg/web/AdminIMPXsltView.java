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

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.context.RequestContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.plg.db.IMPDBInterface;
import si.laurentius.plugin.imp.IMPXslt;
import si.laurentius.plugin.imp.Namespace;
import si.laurentius.plugin.imp.XSLTRule;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("adminIMPXsltView")
public class AdminIMPXsltView extends AbstractAdminJSFView<IMPXslt>   
        implements Serializable {

  private static final SEDLogger LOG = new SEDLogger(AdminIMPXsltView.class);

  @EJB
  private IMPDBInterface mDB;
  Namespace selectedNamespace = null;
  XSLTRule selectedXSLTRule = null;
  File transformationFolder = null;
  File schemaFolder = null;

  @Override
  public void createEditable() {
    IMPXslt imp = new IMPXslt();
    int i = 1;
    String base = "xslt_%03d";
    while (mDB.getXSLT(String.format(base, i)) != null) {
      i++;
    }
    imp.setInstance(String.format(base, i));

    setNew(imp);
  }

  public void createNamespace() {

    if (getEditable() != null) {

      String sbname = "ns%d";
      int i = 1;
      while (namespacePrefixExists(String.format(sbname, i))) {
        i++;
      }
      Namespace ns = new Namespace();
      ns.setPrefix(String.format(sbname, i));
      //getEditable().getNamespaces().add(ns);
    }
  }

  @Override
  public List<IMPXslt> getList() {
    return mDB.getXSLTs();
  }

  public Namespace getSelectedNamespace() {
    return selectedNamespace;
  }

  public XSLTRule getSelectedXSLTRule() {
    return selectedXSLTRule;
  }

  public boolean namespacePrefixExists(String val) {
    boolean bExists = false;
    if (getEditable() != null) {
      /*
      for (Namespace ns : getEditable().getNamespaces()) {
        if (Objects.equals(ns.getPrefix(), val)) {
          bExists = true;
          break;
        }
      }*/
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
      /*
      List<Namespace> lst = getEditable().getNamespaces();
      for (Namespace ns : lst) {
        if (Objects.equals(ns, getSelectedNamespace())) {
          lst.remove(ns);
          break;
        }
      }*/

    }
  }

  public void removeSelectedXSLTRule() {
    if (getSelectedXSLTRule() != null && getEditable() != null) {
      List<XSLTRule> lst = getEditable().getXSLTRules();
      for (XSLTRule xp : lst) {
        if (Objects.equals(xp, getSelectedXSLTRule())) {
          lst.remove(xp);
          break;
        }
      }

    }
  }

  public void setSelectedNamespace(Namespace selectedNamespace) {
    this.selectedNamespace = selectedNamespace;
  }

  public void setSelectedXSLTRule(XSLTRule selectedXSLTRule) {
    this.selectedXSLTRule = selectedXSLTRule;
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

    if (isEditableNew() && mDB.getXSLT(getEditable().getInstance()) != null) {
      addError("Instance parametere must not be unique!");
      return false;
    }
    return true;
  }

  public void createXSLTRule() {
    IMPXslt ed = getEditable();
    if (ed != null) {
      DialogXPath dp = (DialogXPath) getBean("dialogXPath");
      dp.createNewXPath(ed.getXSLTRules());
      dp.setUpdateTableId(":dlgXslt:xsltDialogForm:TblTransformation");
      RequestContext context = RequestContext.getCurrentInstance();
      context.execute("PF('xPathDialog').show();");
      context.update(":dlgXPath:xPathDialog");
    }

  }

  public void editXSLTRule() {
    IMPXslt ed = getEditable();
    if (ed != null) {
      DialogXPath dp = (DialogXPath) getBean("dialogXPath");

      dp.setEditable(getSelectedXSLTRule(), ed.getXSLTRules());
      dp.setUpdateTableId(":dlgXslt:xsltDialogForm:TblTransformation");
      RequestContext context = RequestContext.getCurrentInstance();
      context.execute("PF('xPathDialog').show();");
      context.update(":dlgXPath:xPathDialog");
    }
  }

  public Object getBean(final String beanName) {
    final Object returnObject = facesContext().getELContext().getELResolver().
            getValue(facesContext().getELContext(), null, beanName);
    if (returnObject == null) {
      LOG.formatedWarning("Bean with name %s was not found!", beanName);
    }
    return returnObject;
  }

  public File[] getTransformationFiles() {
    return PlgSystemProperties.getXSLTFolder().listFiles(
            (File dir, String name) -> name.toLowerCase().endsWith(".xslt") || name.
            toLowerCase().endsWith(".xsl"));

  }

  public File[] getSchemaFiles() {
    return PlgSystemProperties.getSchemaFolder().listFiles(
            (File dir, String name) -> name.toLowerCase().endsWith(".xsd"));
  }

}

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
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.plg.db.IMPDBInterface;
import si.laurentius.plugin.imp.IMPExecute;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminIMPExecuteView")
public class AdminIMPExecuteView extends AbstractAdminJSFView<IMPExecute> {
  
  private static final SEDLogger LOG = new SEDLogger(AdminIMPExecuteView.class);
  
  @EJB
  private IMPDBInterface mDB;
  
  
  @Override
  public void createEditable() {
    IMPExecute imp = new IMPExecute();
    int i = 1;
    String base = "execute_%03d";
    while (mDB.getExecute(String.format(base, i)) != null) {
      i++;
    }
    imp.setInstance(String.format(base, i));
    
    imp.setCommand("${laurentius.home}/scripts/export-appl.sh");
    imp.setParameters("${Id} ${SenderEBox} ${Service}");

    setNew(imp);
  }
  
  @Override
  public List<IMPExecute> getList() {
    return mDB.getExecutes();
  }
  
  @Override
  public boolean persistEditable() {
    return mDB.addExecute(getEditable());
  }
  
  @Override
  public void removeSelected() {
    mDB.removeExecute(getEditable());
  }
  
  @Override
  public boolean updateEditable() {
    return mDB.updateExecute(getEditable());
  }
  @Override
  public boolean validateData() {
    if (Utils.isEmptyString(getEditable().getInstance())) {
      addError("Instance parametere must not be null!");
      return false;
    }
    
    if (isEditableNew() && mDB.getExecute(getEditable().getInstance()) != null) {
      addError("Instance parametere must not be unique!");
      return false;
    }
    return true;
  }
  
}

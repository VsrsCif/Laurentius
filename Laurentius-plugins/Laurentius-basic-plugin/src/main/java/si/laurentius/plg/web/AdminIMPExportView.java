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
import si.laurentius.plugin.imp.IMPExport;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminIMPExportView")
public class AdminIMPExportView extends AbstractAdminJSFView<IMPExport> {
  
  private static final SEDLogger LOG = new SEDLogger(AdminIMPExportView.class);
  
  @EJB
  private IMPDBInterface mDB;
  
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
  
  @Override
  public void createEditable() {
    IMPExport imp = new IMPExport();
    int i = 1;
    String base = "export_%03d";
    while (mDB.getExport(String.format(base, i)) != null) {
      i++;
    }
    imp.setInstance(String.format(base, i));
    
    imp.setExportMetaData(Boolean.TRUE);
    imp.setMetaDataFilename("metadata.xml");
    imp.setMetaDataFilename("metadata.xml");
    imp.setFileMask("${Id}_${SenderEBox}_${Service}");
    imp.setFolder("${laurentius.home}/test-export");
    imp.setOverwrite(Boolean.TRUE);
    
    setNew(imp);
  }
  
  @Override
  public List<IMPExport> getList() {
    return mDB.getExports();
  }
  
  @Override
  public boolean persistEditable() {
    return mDB.addExport(getEditable());
  }
  
  @Override
  public void removeSelected() {
    mDB.removeExport(getEditable());
  }
  
  @Override
  public boolean updateEditable() {
    return mDB.updateExport(getEditable());
  }
  
}

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
package si.laurentius.msh.web.abst;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import si.laurentius.commons.utils.ReflectUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.msh.mail.MSHMailType;

/**
 *
 * @author Jože Rihtaršič
 * @param <T>  -- mail table data 
 * @param <S> -- mail event data
 * @param <M> -- mail Type
 * 
 */
public abstract class AbstractMailView<T, M, S> {

    private static final SEDLogger LOG = new SEDLogger(AbstractMailView.class);
  /**
   *
   */
  private SimpleDateFormat msdfDateFormat = new SimpleDateFormat(
          "dd.MM.yyyy HH:mm:ss");

  /**
   *
   */
  private List<T> selected;

  /**
   *
   */
  protected AbstractMailDataModel<T> mMailModel = null;
  StringFormater mStringFomrater = new StringFormater();

  /**
   *
   */
  protected int mTabActiveIndex = 0;

  /**
   *
   */
  protected M mCurrentMail = null;

  protected List<S> mlstCurrentMailEvents = null;
  

  private DualListModel<String> msbCBExportDualList = null;
  
  
  protected void addError(String desc) {
    facesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    desc));
  } 

  /**
   *
   * @return
   */
  public StreamedContent exportTableData() {

    List<String> mehtods = getCurrentPickupDualExportData().getTarget();
     File f;
    try {
      f = File.createTempFile("export", ".txt");
    } catch (IOException ex) {
      LOG.logError( "Error occrured while creating temp export file!", ex);
      return null; 
    }
    try (  FileWriter fw = new FileWriter(f)) {
     
      List<T> lst = mMailModel.getData(0, 1000);

      fw.write("St., ");
      fw.write(String.join(",", mehtods));
      fw.write("\n");
      int i = 1;
      for (T o : lst) {
        fw.write(mStringFomrater.formatCVS(mehtods, o, i++));
        fw.write("\n");

      }
      fw.flush();


      return new DefaultStreamedContent(new FileInputStream(f), "text/plain",
              "export-data.txt",
              "utf-8");
    } catch (IOException ex) {
       LOG.logError( "Error occrured while exporting data file!", ex);
     
    }
    return null;
  }

  /**
   *
   * @param date
   * @return
   */
  public String formatDate(Date date) {
    return msdfDateFormat.format(date);
  }

  /**
   *
   * @return
   */
  public M getCurrentMail() {
    if (selected == null || selected.isEmpty()){
      mCurrentMail = null;
      mlstCurrentMailEvents = null;
      return null;
    }else{
      return  mCurrentMail;
    }
  }

  public List<T> getSelected() {
    return selected;
  }

  public void setSelected(List<T> selected) {
    this.selected = selected;
    
  }

  /**
   *
   * @return
   */
  public DualListModel<String> getCurrentPickupDualExportData() {

    if (msbCBExportDualList == null) {
      List<String> sbIDs = new ArrayList<>();
      List<String> sbTrg = new ArrayList<>();
      sbIDs.addAll(ReflectUtils.getBeanProperties(MSHMailType.class));
     
      msbCBExportDualList = new DualListModel<>(sbIDs, sbTrg);
    }
    return msbCBExportDualList;
  }

  /**
   *
   * @param bi
   * @return
   */
  abstract public StreamedContent getFile(BigInteger bi);

  /**
   *
   * @param bi
   * @return
   */
  abstract public StreamedContent getEventEvidenceFile(String filePath);

  /**
   *
   * @return
   */
  public List<S> getMailEvents() {
    return mlstCurrentMailEvents;
  }

  /**
   *
   * @return
   */
  public LazyDataModel<T> getMailList() {
    return mMailModel;
  }

  /**
   *
   * @param status
   * @return
   */
  abstract public String getStatusColor(String status);

  /**
   *
   * @return
   */
  public int getTabActiveIndex() {
    return mTabActiveIndex;
  }

  /**
   *
   * @param event
   */
  public void onRowSelect(SelectEvent event) {
    updateCurrentMailData(event != null?(T) event.getObject():null);
    
  }

  /**
   *
   * @param event
   */
  public void onRowUnselect(UnselectEvent event) {
    updateCurrentMailData(null);

  }

  /**
   *
   * @param event
   */
  public void onTabChange(TabChangeEvent event) {
    if (event != null) {
      TabView tv = (TabView) event.getComponent();
      mTabActiveIndex = tv.getActiveIndex();
    } else {
      mTabActiveIndex = 0;
    }
  }

  /**
   *
   * @param om
   * @return
   */
  public int rowIndex(T om) {
    return mMailModel.getRowIndex();
  }

  /**
   *
   * @param event
   */
  public void search(ActionEvent event) {
    String res = (String) event.getComponent().getAttributes().get("status");
  }

  /**
   *
   * @param mail
   */


  /**
   *
   * @param dl
   */
  public void setCurrentPickupDualExportData(DualListModel<String> dl) {
    msbCBExportDualList = dl;
  }

  /**
   *
   * @param itindex
   */
  public void setTabActiveIndex(int itindex) {
    mTabActiveIndex = itindex;
  }


  /**
     *
     */
  public abstract void updateCurrentMailData(T mailData);

  /**
   *
   * @return
   */
  protected ExternalContext externalContext() {
    return facesContext().getExternalContext();
  }

  /**
   *
   * @return
   */
  protected FacesContext facesContext() {
    return FacesContext.getCurrentInstance();
  }

}

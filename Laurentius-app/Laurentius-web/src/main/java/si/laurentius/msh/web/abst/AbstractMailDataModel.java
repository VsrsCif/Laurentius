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

import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.msh.web.gui.UserSessionData;

/**
 *
 * @author sluzba
 * @param <T>
 */
public abstract class AbstractMailDataModel<T> extends LazyDataModel<T> {

  /**
     *
     */
  protected static final long serialVersionUID = 1L;
  SEDDaoInterface mDB;

  /**
     *
     */
  protected List<T> mDataList;

  /**
     *
     */
  protected UserSessionData messageBean;

  /**
     *
     */
  protected final Class<T> type;

  /**
   *
   * @param type
   */
  public AbstractMailDataModel(Class<T> type) {
    this.type = type;
  }

  /**
   *
   * @return
   */
  abstract public Object externalFilters();

  /**
   *
   * @return
   */
  public List<T> getCurrentData() {
    return mDataList;
  }

  /**
   *
   * @param startingAt
   * @param maxPerPage
   * @param sortField
   * @param sortOrder
   * @param filters
   * @return
   */
  public List<T> getData(int startingAt, int maxPerPage, String sortField, SortOrder sortOrder,
      Object filters) {
    String strSortOrder = "DESC";
    return mDB.getDataList(type, startingAt, maxPerPage, sortField, strSortOrder, filters);
  }

  /**
   *
   * @param startingAt
   * @param maxPerPage
   * @return
   */
  public List<T> getData(int startingAt, int maxPerPage) {
    String strSortOrder = "DESC";
    return mDB.getDataList(type, startingAt, maxPerPage, "Id", strSortOrder, externalFilters());
  }

  /**
   *
   * @return
   */
  public Class<T> getType() {
    return type;
  }

  /**
   *
   * @return
   */
  public UserSessionData getUserSessionData() {
    return this.messageBean;
  }

  /**
   *
   * @param startingAt
   * @param maxPerPage
   * @param sortField
   * @param sortOrder
   * @param filters
   * @return
   */
  @Override
  public List<T> load(int startingAt, int maxPerPage, String sortField, SortOrder sortOrder,
      Map<String, Object> filters) {
    String strSortOrder = "DESC";
    // validate data
    Object filterObject = externalFilters();
    mDataList = getData(startingAt, maxPerPage, sortField, sortOrder, filterObject);
    long l = mDB.getDataListCount(type, filterObject);
    setRowCount((int) l);

    setPageSize(maxPerPage);
    return mDataList;
  }

  // must povide the setter method
  /**
   *
   * @param messageBean
   * @param db
   */
  public void setUserSessionData(UserSessionData messageBean, SEDDaoInterface db) {
    mDB = db;
    this.messageBean = messageBean;
  }

}

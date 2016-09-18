/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.admin;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;
import si.laurentius.ebox.SEDBox;

/**
 *
 * @author Jože Rihtaršič
 */
@FacesConverter(value = "userSedBoxPickListConverter")
public class UserSedBoxPickListConverter implements Converter {

  /**
   *
   * @param arg0
   * @param arg1
   * @param arg2
   * @return
   */
  @Override
  public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
    Object ret = null;
    if (arg1 instanceof PickList) {
      PickList pl = (PickList) arg1;
      DualListModel dl = (DualListModel) pl.getValue();

      for (Object o : dl.getSource()) {
        String id = ((SEDBox) o).getLocalBoxName();
        if (arg2.equals(id)) {
          ret = o;
          break;
        }
      }
      if (ret == null) {
        for (Object o : dl.getTarget()) {
          String id = "" + ((SEDBox) o).getLocalBoxName();
          if (arg2.equals(id)) {
            ret = o;
            break;
          }
        }
      }
    }
    return ret;
  }

  /**
   *
   * @param arg0
   * @param arg1
   * @param arg2
   * @return
   */
  @Override
  public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
    String str = "";
    if (arg2 instanceof SEDBox) {
      str = "" + ((SEDBox) arg2).getLocalBoxName();
    }
    return str;
  }
}

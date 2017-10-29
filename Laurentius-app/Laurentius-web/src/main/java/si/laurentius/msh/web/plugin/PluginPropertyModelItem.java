/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.plugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import si.laurentius.commons.utils.Utils;
import si.laurentius.plugin.component.PluginPropertyDef;

/**
 *
 * @author sluzba
 */
public class PluginPropertyModelItem implements Serializable {

    PluginPropertyDef mPluginPropDef;
    String mValue;
    List<String> listValue = new ArrayList<>();

    /**
     *
     * @param ctp
     * @param val
     */
    public PluginPropertyModelItem(PluginPropertyDef ctp,
            String val) {
      mValue = val;
      mPluginPropDef = ctp;

    }

    /**
     *
     * @return
     */
    public PluginPropertyDef getPropertyDef() {
      return mPluginPropDef;
    }

    /**
     *
     * @return
     */
    public String getValue() {
      return mValue;
    }

    /**
     *
     * @param v
     */
    public void setValue(String v) {
      this.mValue = v;
    }

    /**
     *
     * @return
     */
    public Integer getIntValue() {
      return mValue != null ? new Integer(mValue) : null;
    }

    /**
     *
     * @param v
     */
    public void setIntValue(Integer v) {

      this.mValue = v != null ? v.toString() : null;

    }

    /**
     *
     * @return
     */
    public Boolean getBooleanValue() {
      return mValue != null ? mValue.equalsIgnoreCase("true") : null;
    }

    /**
     *
     * @param v
     */
    public void setBooleanValue(Boolean v) {
      this.mValue = v ? "true" : "false";
    }

     /**
     *
     * @return
     */
    public List<String> getListValue() {
      listValue.clear();
      if (!Utils.isEmptyString(mValue)){
        listValue.addAll(Arrays.asList(mValue.split(",")) );
      } 
      return listValue;
    }

    /**
     *
     * @param v
     */
    public void setListValue(List<String> v) {
      this.mValue = v.isEmpty()? null :String.join(",", v);
    }
   
  }
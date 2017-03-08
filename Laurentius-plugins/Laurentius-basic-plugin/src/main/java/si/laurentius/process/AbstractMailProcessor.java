/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.process;

import si.laurentius.plugin.interfaces.InMailProcessorInterface;
import si.laurentius.plugin.processor.MailProcessorPropertyDef;

/**
 *
 * @author sluzba
 */
public abstract class AbstractMailProcessor implements InMailProcessorInterface {

  protected MailProcessorPropertyDef createProperty(String key, String defValue,
          String desc, boolean mandatory, String type, String valFormat,
          String valList) {
    MailProcessorPropertyDef ttp = new MailProcessorPropertyDef();
    ttp.setKey(key);
    ttp.setDefValue(defValue);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
    return ttp;
  }

}

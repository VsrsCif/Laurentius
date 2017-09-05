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
package si.jrc.msh.plugin.example;

import java.util.Properties;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.apache.cxf.binding.soap.SoapMessage;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interceptor.MailInterceptorPropertyDef;
import si.laurentius.plugin.interfaces.PropertyListType;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ExampleSoapInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(ExampleSoapInterceptor.class);

  @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef mid = new MailInterceptorDef();
    mid.setDescription("Exaple of soap intercepor");
    mid.setName("Interceptor example");
    mid.setType("ExampleSoapInterceptor");
    mid.getMailInterceptorPropertyDeves().add(createTTProperty("key.one.list",
            "First list of sedboxes (without domain).", true, PropertyType.List.
                    getType(), null, PropertyListType.LocalBoxes.getType()));
    mid.getMailInterceptorPropertyDeves().add(createTTProperty("key.two.list",
            "Second list of keystores .", true, PropertyType.List.
                    getType(), null, PropertyListType.KeystoreCertKeys.getType()));
    mid.getMailInterceptorPropertyDeves().add(createTTProperty("key.three.int",
            "Insert integer.", true, PropertyType.Integer.getType(),
            null, null));
     mid.getMailInterceptorPropertyDeves().add(createTTProperty("key.four.string",
            "Insert string.", true, PropertyType.String.getType(),
            null, null));
    return mid;
  }
  
  private MailInterceptorPropertyDef createTTProperty(String key, String desc,
          boolean mandatory,
          String type, String valFormat, String valList) {
    MailInterceptorPropertyDef ttp = new MailInterceptorPropertyDef();
    ttp.setKey(key);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
    return ttp;
  }

  /**
   *
   * @param msg
   * @return 
   */
  @Override
  public boolean handleMessage(SoapMessage msg,Properties contextProperties) {
    long l = LOG.logStart();

    boolean isBackChannel = SoapUtils.isRequestMessage(msg) && SoapUtils.isInboudMessage(msg);
    MSHInMail in = SoapUtils.getMSHInMail(msg);
    MSHOutMail out = SoapUtils.getMSHOutMail(msg);

    LOG.formatedlog("Soap message is '%s' with incomming user mail: '%s' and outgoing user mail: '%s'!", 
        isBackChannel?"Response":"Request", 
        (out==null?"NULL": out.getService()+":"+out.getAction()),
        (in==null?"NULL": in.getService()+":"+in.getAction()) 
        );
    
    LOG.logEnd(l);
    return true;
  }

  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t,Properties contextProperties) {
   LOG.logWarn("Fault occured - handle soap fault", null);
  }

}

/*
 * Copyright 2015, Supreme Court Republic of Slovenia
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
package si.jrc.msh.interceptor;

import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.pmode.PluginType;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.interfaces.SoapInterceptorInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;

/**
 *
 * @author sluzba
 */
public class MSHPluginInInterceptor extends AbstractSoapInterceptor {

  /**
   *
   */
  protected final SEDLogger mlog = new SEDLogger(MSHPluginInInterceptor.class);

  /**
   *
   */
  public MSHPluginInInterceptor() {
    super(Phase.PRE_INVOKE);
  }

  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg)
      throws Fault {
    long l = mlog.logStart();
    EBMSMessageContext ectx = SoapUtils.getEBMSMessageInContext(msg);

    if (ectx != null &&
         ectx.getPMode().getInPlugins() != null) {

      List<PluginType> lst = ectx.getPMode().getInPlugins().getPlugins();
      for (PluginType pt : lst) {
        // todo
        String str = pt.getValue();
        if (!Utils.isEmptyString(str)) {
          try {
            SoapInterceptorInterface example = InitialContext.doLookup(str);
            example.handleMessage(msg);
          } catch (NamingException ex) {
            mlog.logError(l, ex);
          }
        }
      }
    }
    mlog.logEnd(l);
  }

}

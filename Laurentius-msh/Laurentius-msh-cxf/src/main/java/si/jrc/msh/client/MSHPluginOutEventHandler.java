/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.client;

import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.PluginType;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.plugin.interfaces.OutMailEventInterface;

/**
 *
 * @author Jože Rihtaršič
 */
public class MSHPluginOutEventHandler {

  /**
     *
     */
  protected final static SEDLogger LOG = new SEDLogger(MSHPluginOutEventHandler.class);


 public void outEvent(MSHOutMail outMail, EBMSMessageContext ectx, OutMailEventInterface.PluginOutEvent evnt) {
    long l = LOG.logStart();
/*
    if (outMail == null){
      LOG.logWarn("No MSHOutMail object  found to process!", null);
    } else if (ectx == null){
      LOG.formatedlog("No EBMSMessageContext context for out mail: '%d'." ,outMail.getId() );
    } else if (ectx.getPMode()!= null 
              && ectx.getPMode().getPlugins()!= null 
              && ectx.getPMode().getPlugins().getOutMailEventPlugins()!= null 
              && !ectx.getPMode().getPlugins().getOutMailEventPlugins().getPlugins().isEmpty()) {
      List<PluginType> lst = ectx.getPMode().getPlugins().getOutMailEventPlugins().getPlugins();
      for (PluginType pt : lst) {
        // todo
        String str = pt.getValue();
        if (!Utils.isEmptyString(str)) {
          try {
            OutMailEventInterface listener = InitialContext.doLookup(str);
            listener.outEvent(outMail, ectx, evnt);
          } catch (NamingException ex) {
            LOG.logError(l, String.format("OutMailEventLisneter '%s' not found!", str),  ex);
          } catch (Exception ex) {
            LOG.logError(l, String.format("OutMailEventLisneter '%s' throws an  error with message: %s!", str, ex.getMessage()),  ex);
          }
        }
      }
    } else {
      LOG.formatedlog("No plugin OutMailEventLisneter found for mail: '%d' pmode '%s'." ,outMail.getId(), ectx.getPMode().getId() );
    }
    LOG.logEnd(l);
*/
  }

}

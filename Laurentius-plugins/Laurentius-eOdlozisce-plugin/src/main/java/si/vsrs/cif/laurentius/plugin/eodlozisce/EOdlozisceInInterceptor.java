package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.ebox.SEDBox;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.plugin.component.ComponentBase;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;
import si.vsrs.cif.laurentius.plugin.eodlozisce.exception.EOdlozisceException;

import javax.ejb.*;
import java.util.Date;
import java.util.Properties;

@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class EOdlozisceInInterceptor implements SoapInterceptorInterface {
    @EJB
    EOdlozisceInProcessor inProcessor;

    protected final SEDLogger LOG = new SEDLogger(EOdlozisceInInterceptor.class);

    @Override
    public ComponentBase getDefinition() {
        MailInterceptorDef definition = new MailInterceptorDef();
        definition.setDescription("EOdlozisce in interceptor");
        definition.setName("eOdlozisce in interceptor");
        definition.setType(EOdlozisceConstants.EODLOZISCE_IN_INTERCEPTOR);
        return definition;
    }

    @Override
    public boolean handleMessage(SoapMessage msg, Properties contextProperties) {
        long l = LOG.logStart();
        LOG.log(String.format("EODL IN message received on time = %d", l));

        SEDBox receiverBox = SoapUtils.getMSHInMailReceiverBox(msg);
        MSHInMail inMail = SoapUtils.getMSHInMail(msg);
        MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);

        LOG.log(String.format("EODL IN\nreceiverBox = %s\ninMail = %s\noutMail = %s", receiverBox, inMail, outMail));

        if (inMail != null) {
            // process

            //EOdlozisceInProcessor
            try {
                inProcessor.processMessage(msg, inMail, contextProperties);
            } catch (EOdlozisceException e) {
                // TODO only client? or is there a FAULT_CODE_SERVER case?
                throw new SoapFault(e.getMessage(), SoapFault.FAULT_CODE_CLIENT);
            }
        }

        // TODO validate message
        // valid XML metadata
        // valid data
        // valid XML signature
        // valid main PDF attachment signature

        // TODO process message
        // XML signature
        // timestamp signature (external service?)
        //

        return true;
    }

    @Override
    public void handleFault(SoapMessage t, Properties contextProperties) {
    }
}

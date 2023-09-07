package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.ebox.SEDBox;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.plugin.component.ComponentBase;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.Properties;

@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class EOdlozisceOutInterceptor implements SoapInterceptorInterface {
    protected final SEDLogger LOG = new SEDLogger(EOdlozisceOutInterceptor.class);
    @Override
    public ComponentBase getDefinition() {
        MailInterceptorDef definition = new MailInterceptorDef();
        definition.setDescription("EOdlozisce out interceptor");
        definition.setName("eOdlozisce out interceptor");
        definition.setType(EOdlozisceConstants.EODLOZISCE_OUT_INTERCEPTOR);
        return definition;
    }

    @Override
    public boolean handleMessage(SoapMessage msg, Properties contextProperties) throws Fault {
        long l = LOG.logStart();
        LOG.log(String.format("EODL OUT message received on time = %d", l));

        SEDBox receiverBox = SoapUtils.getMSHInMailReceiverBox(msg);
        MSHInMail inMail = SoapUtils.getMSHInMail(msg);
        MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);

        LOG.log(String.format("EODL OUT\nreceiverBox = %s\ninMail = %s\noutMail = %s", receiverBox, inMail, outMail));

        return true;
    }

    @Override
    public void handleFault(SoapMessage t, Properties contextProperties) {

    }
}

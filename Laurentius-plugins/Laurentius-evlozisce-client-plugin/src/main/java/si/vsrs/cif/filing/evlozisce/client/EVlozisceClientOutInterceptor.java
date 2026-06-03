package si.vsrs.cif.filing.evlozisce.client;

import java.util.Properties;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.apache.cxf.binding.soap.SoapMessage;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class EVlozisceClientOutInterceptor implements SoapInterceptorInterface {

    protected final SEDLogger LOG = new SEDLogger(EVlozisceClientOutInterceptor.class);

    @Override
    public MailInterceptorDef getDefinition() {
        MailInterceptorDef def = new MailInterceptorDef();
        def.setType(EVlozisceClientConstants.OUT_INTERCEPTOR_TYPE);
        def.setName(EVlozisceClientConstants.OUT_INTERCEPTOR_TYPE);
        def.setDescription("eVlozisce Client out interceptor");
        return def;
    }

    @Override
    public boolean handleMessage(SoapMessage msg, Properties contextProperties) {
        long l = LOG.logStart();

        MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);
        if (outMail != null) {
            LOG.log(String.format("eVlozisce Client OUT: messageId=%s, service=%s, action=%s",
                    outMail.getMessageId(), outMail.getService(), outMail.getAction()));
        }

        return true;
    }

    @Override
    public void handleFault(SoapMessage t, Properties contextProperties) {
    }
}

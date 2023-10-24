package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.log4j.Logger;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.ebms.EBMSError;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.ebox.SEDBox;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.plugin.component.ComponentBase;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;
import si.vsrs.cif.laurentius.plugin.eodlozisce.exception.EOdlozisceErrorCode;
import si.vsrs.cif.laurentius.plugin.eodlozisce.exception.EOdlozisceException;

import javax.ejb.*;
import java.util.Calendar;
import java.util.Properties;

@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class EOdlozisceInInterceptor implements SoapInterceptorInterface {

    @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
    SEDDaoInterface mDB;

    protected final SEDLogger LOG = new SEDLogger(EOdlozisceInInterceptor.class);

    final static Logger log = Logger.getLogger(EOdlozisceInInterceptor.class);

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

        log.warn("Handling eOdlozisce message");

        SEDBox receiverBox = SoapUtils.getMSHInMailReceiverBox(msg);
        MSHInMail inMail = SoapUtils.getMSHInMail(msg);
        MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);

        LOG.log(String.format("EODL IN\nreceiverBox = %s\ninMail = %s\noutMail = %s", receiverBox, inMail, outMail));

        if (inMail != null) {
            // Accepting and storing the message. The task will pick it up later while looking for PLOCKED messages.
            try {
                inMail.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
                inMail.setStatusDate(Calendar.getInstance().getTime());
                mDB.serializeInMail(inMail, EOdlozisceConstants.EODLOZISCE_PLUGIN_TYPE);
            } catch (StorageException ex) {
                String errorMsg = String.format(
                        "Server error occured while receiving mail: %s, Error: %s." + inMail.
                                getId(), ex.getMessage());
                LOG.logError(l, errorMsg, ex);
                throw new EBMSError(EOdlozisceErrorCode.ServerError,
                        inMail != null ? inMail.getMessageId() : (outMail != null ? outMail.getMessageId() : ""),
                        errorMsg, SoapFault.FAULT_CODE_SERVER);
            }
        }

        return true;
    }

    @Override
    public void handleFault(SoapMessage t, Properties contextProperties) {
    }
}

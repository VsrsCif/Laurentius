package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.apache.cxf.binding.soap.SoapMessage;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.IMPartProperty;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.msh.inbox.property.MSHInProperties;
import si.laurentius.msh.inbox.property.MSHInProperty;
import si.vsrs.cif.laurentius.plugin.eodlozisce.exception.EOdlozisceException;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

@Stateless
@Local
@TransactionManagement(TransactionManagementType.BEAN)
public class EOdlozisceInProcessor {
    protected final SEDLogger LOG = new SEDLogger(EOdlozisceInProcessor.class);

    public void processMessage(SoapMessage msg, MSHInMail inMail, Properties contextProperties) throws EOdlozisceException {
        String action = inMail.getAction();
        LOG.log("EODL inProcessor called with action = " + action);

        if (!action.equals(EOdlozisceConstants.EODLOZISCE_ACTION)) {
            // TODO plugin exception?
            throw new EOdlozisceException("Wrong action for EOdlozisce processor");
        }

        // TODO this is just a concept from here on

        MSHInProperties mshInProperties = inMail.getMSHInProperties();
        List<MSHInProperty> mshInProperties1 = mshInProperties.getMSHInProperties();

        String mailProperties = mshInProperties1.stream().map(mailProperty -> mailProperty.getName() + " : " + mailProperty.getValue() + " [" + mailProperty.getType() + "]").collect(Collectors.joining(", "));

        MSHInPayload mshInPayload = inMail.getMSHInPayload();
        List<MSHInPart> mshInParts = mshInPayload.getMSHInParts();

        Optional<MSHInPart> firstPart = mshInParts.stream().findFirst();

        if (!firstPart.isPresent()) {
            throw new EOdlozisceException("No payload");
        }

        MSHInPart mshInPart = firstPart.get();

        String filepath = mshInPart.getFilepath();
        List<IMPartProperty> imPartProperties = mshInPart.getIMPartProperties();
        String props = imPartProperties.stream().map(partProperty -> partProperty.getName() + partProperty.getValue()).collect(Collectors.joining(","));

        LOG.log("EODL inProcessor got message with properties = " + mailProperties);
        LOG.log("EODL inProcessor got file with path = " + filepath);
        LOG.log("EODL inProcessor got file with properties = " + props);

    }
}

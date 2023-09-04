package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.apache.cxf.binding.soap.SoapMessage;
import si.laurentius.commons.utils.SEDLogger;
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
public class EOdlozisceInInterceptor implements SoapInterceptorInterface {
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
    public boolean handleMessage(SoapMessage t, Properties contextProperties) {
        return false;
    }

    @Override
    public void handleFault(SoapMessage t, Properties contextProperties) {

    }
}

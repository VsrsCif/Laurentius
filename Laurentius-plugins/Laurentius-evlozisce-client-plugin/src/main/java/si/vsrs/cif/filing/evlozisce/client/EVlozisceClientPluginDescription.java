package si.vsrs.cif.filing.evlozisce.client;

import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.bind.JAXBException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.plugin.def.DefaultInitData;
import si.laurentius.plugin.def.MenuItem;
import si.laurentius.plugin.interfaces.AbstractPluginDescription;
import si.laurentius.plugin.interfaces.PluginDescriptionInterface;
import si.laurentius.plugin.interfaces.exception.PluginException;

@Singleton
@Startup
@Local(PluginDescriptionInterface.class)
public class EVlozisceClientPluginDescription extends AbstractPluginDescription {

    private static final SEDLogger LOG = new SEDLogger(EVlozisceClientPluginDescription.class);

    @Override
    public DefaultInitData getDefaultInitData() {
        try {
            DefaultInitData did = (DefaultInitData) XMLUtils.deserialize(
                    EVlozisceClientPluginDescription.class.getResourceAsStream("/init/def-init-data.xml"),
                    DefaultInitData.class);
            return did;
        } catch (JAXBException ex) {
            LOG.logError("Error parsing default init data!", ex);
        }
        return null;
    }

    @PostConstruct
    private void postConstruct() {
        try {
            registerPluginComponentInterface(EVlozisceClientOutInterceptor.class);
            registerPlugin();
        } catch (PluginException ex) {
            LOG.logError("Error occured while registering plugin: " + ex.getMessage(), ex);
        }
    }

    @Override
    public MenuItem getMenu() {
        return null;
    }

    @Override
    public MenuItem getProcessMenu() {
        return null;
    }

    @Override
    public String getDesc() {
        return "eVlozisce Client plugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getName() {
        return "eVlozisce Client plugin";
    }

    @Override
    public String getWebUrlContext() {
        return null;
    }

    @Override
    public List<String> getWebPageRoles() {
        return Collections.emptyList();
    }

    @Override
    public String getType() {
        return EVlozisceClientConstants.PLUGIN_TYPE;
    }
}

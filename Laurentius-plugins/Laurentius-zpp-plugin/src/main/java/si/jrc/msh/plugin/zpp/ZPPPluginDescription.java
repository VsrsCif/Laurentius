/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.zpp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.plugin.interfaces.AbstractPluginDescription;
import si.laurentius.plugin.interfaces.PluginDescriptionInterface;
import si.laurentius.plugin.interfaces.exception.PluginException;

/**
 *
 * @author Jože Rihtaršič
 */
@Singleton
@Startup
@Local(PluginDescriptionInterface.class)
public class ZPPPluginDescription extends AbstractPluginDescription {

    private static final SEDLogger LOG = new SEDLogger(
            ZPPPluginDescription.class);

    @PostConstruct
    private void postConstruct() {
        try {
            // and log further application specific info
            registerPluginComponentInterface(ZPPOutInterceptor.class);
            registerPluginComponentInterface(ZPPInInterceptor.class);

            registerPluginComponentInterface(ZPPTask.class);
            registerPluginComponentInterface(ZPPTaskFiction.class);

            // register plugin
            registerPlugin();
        } catch (PluginException ex) {
            LOG.logError("Error occured while registering plugin: " + ex.
                    getMessage(), ex);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String getDesc() {
        return "ZPP - e-delivery: SVEV 2.0 service implementation";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return "ZPP plugin";
    }

    /**
     *
     * @return
     */
    @Override
    public String getWebUrlContext() {
        return null;
    }
    /**
     *
     * @return
     */
    @Override
    public List<String> getWebPageRoles() {
        return Collections.emptyList();
    }


    /**
     *
     * @return
     */
    @Override
    public String getType() {
        return ZPPConstants.S_ZPP_PLUGIN_TYPE;
    }

}

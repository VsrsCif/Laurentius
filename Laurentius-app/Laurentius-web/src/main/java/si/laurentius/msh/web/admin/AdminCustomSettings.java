package si.laurentius.msh.web.admin;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.context.RequestContext;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.SEDInitDataInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.abst.AbstractJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminCustomSettings")
public class AdminCustomSettings extends AbstractJSFView{

  private static final SEDLogger LOG = new SEDLogger(AdminCustomSettings.class);

  private boolean exportLookupsWithPasswords = true;

  ProxySettings mpsProxy = new ProxySettings();

  @EJB(mappedName = SEDJNDI.JNDI_DATA_INIT)
  private SEDInitDataInterface msedInitData;

  @EJB(mappedName = SEDJNDI.JNDI_DBSETTINGS)
  private DBSettingsInterface msedSettingsData;

  public boolean isExportLookupsWithPasswords() {
    return exportLookupsWithPasswords;
  }

  public void setExportLookupsWithPasswords(boolean exportLookupsWithPasswords) {
    this.exportLookupsWithPasswords = exportLookupsWithPasswords;
  }

  public void exportLookups() {
    msedInitData.exportLookups(SEDSystemProperties.getInitFolder(),
            isExportLookupsWithPasswords());
  }

  /**
   *
   * @param summary
   * @param detail
   */
  public void addMessage(String summary, String detail) {
    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,
            detail);
    FacesContext.getCurrentInstance().addMessage(null, message);
  }

  public ProxySettings getProxySettings() {
    return mpsProxy;
  }

  public void updateProxyFromSystemProperties() {

    mpsProxy.setHttpHost(System.getProperty(SEDSystemProperties.PROXY_HTTP_HOST));
    mpsProxy.setHttpPort(getPort(System.getProperty(SEDSystemProperties.PROXY_HTTP_PORT)));
    mpsProxy.setNoProxyHosts(System.getProperty(SEDSystemProperties.PROXY_HTTP_NO_PROXY));

    mpsProxy.setHttpsHost(System.getProperty(SEDSystemProperties.PROXY_HTTPS_HOST));
    mpsProxy.setHttpsPort(getPort(System.getProperty(SEDSystemProperties.PROXY_HTTPS_PORT)));

    mpsProxy.setFtpHost(System.getProperty(SEDSystemProperties.PROXY_FTP_HOST));
    mpsProxy.setFtpPort(getPort(System.getProperty(SEDSystemProperties.PROXY_FTP_PORT)));

  }

  public void setHttpProxyToAllProtocols() {
    mpsProxy.setFtpHost(mpsProxy.getHttpHost());
    mpsProxy.setHttpsHost(mpsProxy.getHttpHost());
    mpsProxy.setSocksHost(mpsProxy.getHttpHost());

    mpsProxy.setFtpPort(mpsProxy.getHttpPort());
    mpsProxy.setHttpsPort(mpsProxy.getHttpPort());
    mpsProxy.setSocksPort(mpsProxy.getHttpPort());

  }

  public void updateProxyToSystemProperties() {
    if (Utils.isEmptyString(mpsProxy.getHttpHost())) {
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_HTTP_HOST,
              DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_HTTP_PORT,
              DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_HTTP_NO_PROXY,
              DBSettingsInterface.SYSTEM_SETTINGS);
    } else {
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTP_HOST, mpsProxy.
              getHttpHost(), DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTP_PORT, mpsProxy.
              getHttpPort() + "", DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTP_NO_PROXY, mpsProxy.
              getNoProxyHosts(), DBSettingsInterface.SYSTEM_SETTINGS);
    }

    if (Utils.isEmptyString(mpsProxy.getHttpsHost())) {
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_HTTPS_HOST,
              DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_HTTPS_PORT,
              DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_HTTPS_NO_PROXY,
              DBSettingsInterface.SYSTEM_SETTINGS);
    } else {
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTPS_HOST, mpsProxy.
              getHttpsHost(), DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTPS_PORT, mpsProxy.
              getHttpsPort() + "", DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTPS_NO_PROXY, mpsProxy.
              getNoProxyHosts(), DBSettingsInterface.SYSTEM_SETTINGS);
    }

    if (Utils.isEmptyString(mpsProxy.getFtpHost())) {
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_FTP_HOST,
              DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_FTP_PORT,
              DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_FTP_NO_PROXY,
              DBSettingsInterface.SYSTEM_SETTINGS);
    } else {
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_FTP_HOST, mpsProxy.
              getFtpHost(), DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_FTP_PORT, mpsProxy.
              getFtpPort() + "", DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_FTP_NO_PROXY, mpsProxy.
              getNoProxyHosts(), DBSettingsInterface.SYSTEM_SETTINGS);
    }

   
    
    addCallbackParam("saved", true);
    update(":forms:SettingsCustomPanel:settingsPanel:sysProperties", ":forms:SettingsCustomPanel:settingsPanel:sedProperties");
    
  }

  protected int getPort(String port) {
    try {
      return Utils.isEmptyString(port) ? 80 : Integer.parseInt(port);
    } catch (NumberFormatException nf) {
      LOG.formatedWarning("Invalid proxy port number: '%s' ", port);
      return 80;
    }
  }

  static public class ProxySettings {

    

    ;

    String httpHost;
    int httpPort = 80;

    String httpsHost;
    int httpsPort = 80;

    String ftpHost;
    int ftpPort = 80;

    String socksHost;
    int socksPort = 80;

    String noProxyHosts;

    public String getHttpHost() {
      return httpHost;
    }

    public void setHttpHost(String httpHost) {
      this.httpHost = httpHost;
    }

    public int getHttpPort() {
      return httpPort;
    }

    public void setHttpPort(int httpPort) {
      this.httpPort = httpPort;
    }

    public String getHttpsHost() {
      return httpsHost;
    }

    public void setHttpsHost(String httpsHost) {
      this.httpsHost = httpsHost;
    }

    public int getHttpsPort() {
      return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
      this.httpsPort = httpsPort;
    }

    public String getFtpHost() {
      return ftpHost;
    }

    public void setFtpHost(String ftpHost) {
      this.ftpHost = ftpHost;
    }

    public int getFtpPort() {
      return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
      this.ftpPort = ftpPort;
    }

    public String getSocksHost() {
      return socksHost;
    }

    public void setSocksHost(String socksHost) {
      this.socksHost = socksHost;
    }

    public int getSocksPort() {
      return socksPort;
    }

    public void setSocksPort(int socksPort) {
      this.socksPort = socksPort;
    }

    public String getNoProxyHosts() {
      return noProxyHosts;
    }

    public void setNoProxyHosts(String noProxyHosts) {
      this.noProxyHosts = noProxyHosts;
    }

  }
}

package si.laurentius.msh.web.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.SEDInitDataInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.abst.AbstractJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("adminCustomSettings")
public class AdminCustomSettings extends AbstractJSFView {

  private static final SEDLogger LOG = new SEDLogger(AdminCustomSettings.class);

  private boolean exportLookupsWithPasswords = true;

  ProxySettings mpsProxy = new ProxySettings();
  WorkFreeDaysSettings mWFD = new WorkFreeDaysSettings();

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
    try {
      File  exportFolder= SEDSystemProperties.getInitFolder();
      msedInitData.exportLookups(SEDSystemProperties.getInitFolder(),
              isExportLookupsWithPasswords());
       addMessage("Backup saved!", String.format("Backup saved to folder: %s with %s passwords.",
       exportFolder.getAbsolutePath(), isExportLookupsWithPasswords()?"":"NO")
       );
    } catch (StorageException ex) {
      LOG.logError(ex.getMessage(), ex);
      addError(ex.getMessage());
    }
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

    mpsProxy.
            setHttpHost(System.getProperty(SEDSystemProperties.PROXY_HTTP_HOST));
    mpsProxy.setHttpPort(getPort(System.getProperty(
            SEDSystemProperties.PROXY_HTTP_PORT)));
    mpsProxy.setNoProxyHosts(System.getProperty(
            SEDSystemProperties.PROXY_HTTP_NO_PROXY));

    mpsProxy.setHttpsHost(System.getProperty(
            SEDSystemProperties.PROXY_HTTPS_HOST));
    mpsProxy.setHttpsPort(getPort(System.getProperty(
            SEDSystemProperties.PROXY_HTTPS_PORT)));

    mpsProxy.setFtpHost(System.getProperty(SEDSystemProperties.PROXY_FTP_HOST));
    mpsProxy.setFtpPort(getPort(System.getProperty(
            SEDSystemProperties.PROXY_FTP_PORT)));

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
      msedSettingsData.
              removeSEDProperty(SEDSystemProperties.PROXY_HTTP_NO_PROXY,
                      DBSettingsInterface.SYSTEM_SETTINGS);
    } else {
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTP_HOST,
              mpsProxy.
                      getHttpHost(), DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTP_PORT,
              mpsProxy.
                      getHttpPort() + "", DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTP_NO_PROXY,
              mpsProxy.
                      getNoProxyHosts(), DBSettingsInterface.SYSTEM_SETTINGS);
    }

    if (Utils.isEmptyString(mpsProxy.getHttpsHost())) {
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_HTTPS_HOST,
              DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.removeSEDProperty(SEDSystemProperties.PROXY_HTTPS_PORT,
              DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.removeSEDProperty(
              SEDSystemProperties.PROXY_HTTPS_NO_PROXY,
              DBSettingsInterface.SYSTEM_SETTINGS);
    } else {
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTPS_HOST,
              mpsProxy.
                      getHttpsHost(), DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTPS_PORT,
              mpsProxy.
                      getHttpsPort() + "", DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_HTTPS_NO_PROXY,
              mpsProxy.
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
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_FTP_HOST,
              mpsProxy.
                      getFtpHost(), DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_FTP_PORT,
              mpsProxy.
                      getFtpPort() + "", DBSettingsInterface.SYSTEM_SETTINGS);
      msedSettingsData.setSEDProperty(SEDSystemProperties.PROXY_FTP_NO_PROXY,
              mpsProxy.
                      getNoProxyHosts(), DBSettingsInterface.SYSTEM_SETTINGS);
    }

    addCallbackParam("saved", true);
    update(":forms:SettingsCustomPanel:settingsPanel:sysProperties",
            ":forms:SettingsCustomPanel:settingsPanel:sedProperties");

  }

  protected int getPort(String port) {
    try {
      return Utils.isEmptyString(port) ? 80 : Integer.parseInt(port);
    } catch (NumberFormatException nf) {
      LOG.formatedWarning("Invalid proxy port number: '%s' ", port);
      return 80;
    }
  }

  public WorkFreeDaysSettings getWorkFreeDays() {
    return mWFD;
  }

  public void updateWorkFreeDaysFromSystemProperties() {
    mWFD.getDays().clear();
    mWFD.setLastSelectedDate(null);

    String wfd = System.getProperty(SEDSystemProperties.SYS_PROP_WORK_FREE_DAYS, "");
    if (!wfd.isEmpty()) {
      mWFD.getDays().addAll(Arrays.asList(wfd.split(";")));
    }
  }

  public void updateWorkFreeDaysToSystemProperties() {
    msedSettingsData.setSEDProperty(SEDSystemProperties.SYS_PROP_WORK_FREE_DAYS,
            mWFD.getWorkFreeDaysAsString(), DBSettingsInterface.SYSTEM_SETTINGS);
    
    addCallbackParam("saved", true);
    update(":forms:SettingsCustomPanel:settingsPanel:sysProperties",
            ":forms:SettingsCustomPanel:settingsPanel:sedProperties");
  }

  public void removeSelectedDayFromList() {
    String sl = mWFD.getSelectedDateFromList();
    if (sl!=null && mWFD.getDays().contains(sl)){
      mWFD.getDays().remove(sl);
    }
  }

  static public class WorkFreeDaysSettings {

    Date lastSelectedDate;
    List<String> workFreeDays = new ArrayList<>();
    String selecteDateFromList;

    public WorkFreeDaysSettings() {
    }

    public Date getLastSelectedDate() {
      return lastSelectedDate;
    }

    public void setLastSelectedDate(Date lastSelectedDate) {
      if (this.lastSelectedDate != lastSelectedDate) {
        this.lastSelectedDate = lastSelectedDate;

        if (lastSelectedDate != null) {
          String val = StringFormater.formatToISO8601Date(lastSelectedDate);
          if (!workFreeDays.contains(val)) {
            workFreeDays.add(val);
            // sort list
            java.util.Collections.sort(workFreeDays);
            
          }
        }
      }
    }

    public List<String> getDays() {
      return workFreeDays;
    }

    public void setDays(List<String> workfreeDays) {
      this.workFreeDays = workfreeDays;
    }

    public String getWorkFreeDaysAsString() {
      return String.join(";", this.workFreeDays);
    }

    public String getSelectedDateFromList() {
      return selecteDateFromList;
    }

    public void setSelectedDateFromList(String select) {
      this.selecteDateFromList = select;
    }

  }

  static public class ProxySettings {

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

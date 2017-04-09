/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDProperties;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.SEDNetworkUtilsInterface;
import si.laurentius.commons.utils.Utils;
import static si.laurentius.ejb.SEDNetworkUtilsBean.LOG;
import si.laurentius.property.SEDProperty;

/**
 *
 * @author sluzba
 */
@Startup
@Singleton
@AccessTimeout(value = 60000)
public class SEDScheduler {

  @EJB(mappedName = SEDJNDI.JNDI_NETWORK)
  SEDNetworkUtilsInterface networkUtils;

  @EJB(mappedName = SEDJNDI.JNDI_DBSETTINGS)
  private DBSettingsInterface mdbSettings;

  @PostConstruct
  void init() {
    testNetwork();
  }

  // do not lock thread!
  @Schedule(info = "network test", second = "*/30", minute = "*", hour = "*", persistent = false)
  public void testNetwork() {
    long l = LOG.logStart();
    // because method is not locked

    boolean iConNet = false;
    boolean iConInt = false;
    // test network
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.
              getNetworkInterfaces();

      while (interfaces.hasMoreElements()) {
        NetworkInterface interf = interfaces.nextElement();

        if (interf.isUp() && !interf.isLoopback()) {
          iConNet = true;
          break;
        }
      }
    } catch (SocketException ex) {
      LOG.formatedWarning("Errror occured while checking network status %s.",
              ex.getMessage());
    }

    // test internet
    if (iConNet) {

      SEDProperty sp = mdbSettings.getSEDProperty(
              SEDProperties.S_KEY_TEST_NETOWRK, DBSettingsInterface.LAU_SETTINGS);
      if (sp != null && !Utils.isEmptyString(sp.getValue())) {
        String[] lstNtw = sp.getValue().split(",");
        for (String ntw : lstNtw) {
          if (testWebSite(ntw)) {
            iConInt = true;
            break;
          }
        }
      }
    }
    networkUtils.setConnectedToNetwork(iConNet);
    networkUtils.setConnectedToInternet(iConInt);
    LOG.logEnd(l, String.
            format("network connected: %s, internet connected %s",
                    iConNet ? "true" : "false", iConInt ? "true" : "false"));

  }

  private boolean testWebSite(String site) {

    try {
      //make a URL to a known source
      URL url = new URL("http://" + site);

      //open a connection to that source
      HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
      urlConnect.setConnectTimeout(3000);
      urlConnect.setReadTimeout(3000);

      //trying to retrieve data from the source. If there
      //is no connection, this line will fail
      urlConnect.getContent();
      LOG.formatedDebug("Successful internet connection: %s.", site);

    } catch (UnknownHostException e) {
      LOG.formatedDebug("Connection failed (UnknownHostException): %s.", site);
      return false;
    } catch (IOException e) {
      LOG.formatedDebug("Connection failed (IOException): %s.", site);
      return false;
    }
    return true;
  }
}

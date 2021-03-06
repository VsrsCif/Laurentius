/*
 * Copyright 2016, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.msh.web.gui;

import java.io.Serializable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author Jože Rihtaršič
 */
@ApplicationScoped
@Named("AppConstant")
public class AppConstant implements Serializable {

  /**
   *
   */
  public static final String S_PANEL_ADMIN_CRON = "PANEL_ADMIN_CRON";

  /**
   *
   */
  public static final String S_PANEL_ADMIN_EBOXES = "PANEL_ADMIN_EBOXES";

  /**
   *
   */
  public static final String S_PANEL_ADMIN_PLUGIN = "PANEL_ADMIN_PLUGIN";

  /**
   *
   */
  public static final String S_PANEL_ADMIN_USERS = "PANEL_ADMIN_USERS";

  /**
   *
   */
  public static final String S_PANEL_ADMIN_APPL = "PANEL_ADMIN_APPL";

  /**
   *
   */
  public static final String S_PANEL_CRON_EXEC = "PANEL_CRON_EXEC";

  public static final String S_PANEL_INMAIL_PROCESS = "PANEL_INMAIL_PROCESS";

  public static final String S_PANEL_INTERCEPTOR = "PANEL_INTERCEPTOR";
  /**
   *
   */
  public static final String S_PANEL_INBOX = "PANEL_INBOX";

  /**
   *
   */
  public static final String S_PANEL_OUTBOX = "PANEL_OUTBOX";

  /**
   *
   */
  public static final String S_PANEL_PLUGIN = "PANEL_PLUGIN";

  public static final String S_SETTINGS_PLUGIN = "SETTINGS_PLUGIN";



  /**
   *
   */
  public static final String S_PANEL_SETT_CERTS = "PANEL_SETT_CERTS";

  /**
   *
   */
  public static final String S_PANEL_SETT_CERT_ROOT_CA = "PANEL_SETT_CERT_ROOT_CA";
  public static final String S_PANEL_SETT_CERT_CRL = "PANEL_SETT_CERT_CRL";

  public static final String S_PANEL_SETT_JMS = "PANEL_SETT_JMS";

  /**
   *
   */
  public static final String S_PANEL_SETT_CUSTOM = "PANEL_SETT_CUSTOM";

  /**
   *
   */
  public static final String S_PANEL_SETT_PMODE = "PANEL_SETT_PMODE";

  public static final String S_PANEL_SETT_PMODE_SERVICES = "PANEL_SETT_PMODE_SERVICES";
  public static final String S_PANEL_SETT_PMODE_PARTIES = "PANEL_SETT_PMODE_PARTIES";
  public static final String S_PANEL_SETT_PMODE_SECURITIES = "PANEL_SETT_PMODE_SECURITIES";
  public static final String S_PANEL_SETT_PMODE_AS4_RA = "PANEL_SETT_PMODE_AS4_RA";

  public static final String S_APPLICATION_CODE = "laurentius-web";

  public static String getS_APPLICATION_CODE() {
    return S_APPLICATION_CODE;
  }

  /**
   *
   * @return
   */
  public String getS_PANEL_ADMIN_CRON() {
    return S_PANEL_ADMIN_CRON;
  }

  /**
   *
   * @return
   */
  public String getS_PANEL_ADMIN_EBOXES() {
    return S_PANEL_ADMIN_EBOXES;
  }

  /**
   *
   * @return
   */
  public String getS_PANEL_ADMIN_PLUGIN() {
    return S_PANEL_ADMIN_PLUGIN;
  }

  /**
   *
   * @return
   */
  public String getS_PANEL_ADMIN_USERS() {
    return S_PANEL_ADMIN_USERS;
  }

  public String getS_PANEL_ADMIN_APPL() {
    return S_PANEL_ADMIN_APPL;
  }

  /**
   *
   * @return
   */
  public String getS_PANEL_CRON_EXEC() {
    return S_PANEL_CRON_EXEC;
  }

  /**
   *
   * @return
   */
  public String getS_PANEL_INBOX() {
    return S_PANEL_INBOX;
  }

  /**
   *
   * @return
   */
  public String getS_PANEL_OUTBOX() {
    return S_PANEL_OUTBOX;
  }

  /**
   *
   * @return
   */
  public String getS_PANEL_PLUGIN() {
    return S_PANEL_PLUGIN;
  }

  public String getS_SETTINGS_PLUGIN() {
    return S_SETTINGS_PLUGIN;
  }

  public String getS_PANEL_INMAIL_PROCESS() {
    return S_PANEL_INMAIL_PROCESS;
  }

  public String getS_PANEL_INTERCEPTOR() {
    return S_PANEL_INTERCEPTOR;
  }



  /**
   *
   * @return
   */
  public String getS_PANEL_SETT_CERTS() {
    return S_PANEL_SETT_CERTS;
  }

  /**
   *
   * @return
   */
  public String getS_PANEL_SETT_CUSTOM() {
    return S_PANEL_SETT_CUSTOM;
  }

  /**
   *
   * @return
   */
  public String getS_PANEL_SETT_PMODE() {
    return S_PANEL_SETT_PMODE;
  }

  public String getS_PANEL_SETT_PMODE_SERVICES() {
    return S_PANEL_SETT_PMODE_SERVICES;
  }

  public String getS_PANEL_SETT_PMODE_PARTIES() {
    return S_PANEL_SETT_PMODE_PARTIES;
  }

  public String getS_PANEL_SETT_PMODE_SECURITIES() {
    return S_PANEL_SETT_PMODE_SECURITIES;
  }

  public String getS_PANEL_SETT_PMODE_AS4_RA() {
    return S_PANEL_SETT_PMODE_AS4_RA;
  }

  public String getS_PANEL_SETT_CERT_ROOT_CA() {
    return S_PANEL_SETT_CERT_ROOT_CA;
  }

  public String getS_PANEL_SETT_CERT_CRL() {
    return S_PANEL_SETT_CERT_CRL;
  }

  public String getS_PANEL_SETT_JMS() {
    return S_PANEL_SETT_JMS;
  }

}

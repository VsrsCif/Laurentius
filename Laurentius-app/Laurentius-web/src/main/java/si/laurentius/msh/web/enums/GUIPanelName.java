/*
 * Copyright 2016 Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis(, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.msh.web.enums;

/**
 *
 * @author Jože Rihtaršič
 */
public enum GUIPanelName {
PANEL_INBOX("PANEL_INBOX", 0),
PANEL_OUTBOX("PANEL_OUTBOX", 1),
PANEL_PLUGIN("PANEL_PLUGIN", 2),


PANEL_SETT_CUSTOM("PANEL_SETT_CUSTOM", 3),
PANEL_ADMIN_EBOXES("PANEL_ADMIN_EBOXES", 3),
PANEL_ADMIN_USERS("PANEL_ADMIN_USERS", 3),
PANEL_ADMIN_APPL("PANEL_ADMIN_APPL", 3),

PANEL_SETT_CERTS("PANEL_SETT_CERTS", 3),
PANEL_SETT_CERT_ROOT_CA("PANEL_SETT_CERT_ROOT_CA", 3),
PANEL_SETT_CERT_CRL("PANEL_SETT_CERT_CRL", 3),

PANEL_SETT_PMODE("PANEL_SETT_PMODE", 3),
PANEL_SETT_PMODE_SERVICES("PANEL_SETT_PMODE_SERVICES", 3),
PANEL_SETT_PMODE_PARTIES("PANEL_SETT_PMODE_PARTIES", 3),
PANEL_SETT_PMODE_SECURITIES("PANEL_SETT_PMODE_SECURITIES", 3),
PANEL_SETT_PMODE_AS4_RA("PANEL_SETT_PMODE_AS4_RA", 3),

PANEL_ADMIN_PLUGIN("PANEL_ADMIN_PLUGIN", 3),
PANEL_INTERCEPTOR("PANEL_INTERCEPTOR", 3),
PANEL_INMAIL_PROCESS("PANEL_INMAIL_PROCESS", 3),
PANEL_ADMIN_CRON("PANEL_ADMIN_CRON", 3),
SETTINGS_PLUGIN("SETTINGS_PLUGIN", 3),

PANEL_CRON_EXEC("PANEL_CRON_EXEC", 4),
PANEL_SETT_JMS("PANEL_SETT_JMS", 4);



String mstrCode;
int miGroupIndex;

  private GUIPanelName(String code, int groupIndex) {
    this.mstrCode = code;
    this.miGroupIndex = groupIndex;
  }

  public String getCode() {
    return mstrCode;
  }

  public int getGroupIndex() {
    return miGroupIndex;
  }
  
}

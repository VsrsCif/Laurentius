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
package si.laurentius.ejb;

/**
 *
 * @author Jože Rihtaršič
 */
public class SEDNamedQueries {

  public static final String GET_CERTSTORE_BY_NAME = "si.laurentius.cert.SEDCertStore.getByName";

  public static final String UPDATE_INMAIL = "si.laurentius.msh.inbox.mail.MSHInMail.updateStatus";
  public static final String UPDATE_OUTMAIL = "si.laurentius.msh.outbox.mail.MSHOutMail.updateStatus";
  public static final String UPDATE_OUTMAIL_DELIVERED_DATE = "si.laurentius.msh.outbox.mail.MSHOutMail.updateDeliveredDate";
  public static final String UPDATE_OUTMAIL_RECEIVED_DATE = "si.laurentius.msh.outbox.mail.MSHOutMail.updateReceivedDate";
  public static final String UPDATE_OUTMAIL_SENT_DATE = "si.laurentius.msh.outbox.mail.MSHOutMail.updateSentDate";

}

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
package si.laurentius.commons.utils;

/**
 *
 * @author Jože Rihtaršič
 */
public class SEDNamedQueries {

  /**
     *
     */
  public static String SEDBOX_ALL = "laurentius.si/ebox.SEDBox.getAll";

  /**
     *
     */
  public static String SEDBOX_BY_ID = "laurentius.si/ebox.SEDBox.getByName";

  /**
     *
     */
  public static String SEDUSER_BY_ID = "laurentius.si/user.SEDUser.getByUserId";

  /**
     *
     */
  public static String UPDATE_INMAIL = "si.laurentius.msh.inbox.mail.MSHInMail.updateStatus";

  /**
     *
     */
  public static String UPDATE_OUTMAIL = "si.laurentius.msh.outbox.mail.MSHOutMail.updateStatus";
}

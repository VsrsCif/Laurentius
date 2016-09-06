/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.ws;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class NamedQueries {

  /**
     *
     */
  public static final String NQ_PARAM_MAIL_ID = "mailId";

  /**
     *
     */
  public static final String NQ_PARAM_RECEIVER_EBOX = "receiverEBox";

  /**
     *
     */
  public static final String NQ_PARAM_SENDER_EBOX = "senderEBox";

  /**
     *
     */
  public static final String NQ_PARAM_SENDER_MAIL_ID = "senderMessageId";

  /**
     *
     */
  public static final String LAU_NQ_INMAIL_GET_BY_ID_AND_RECBOX =
      "si.laurentius.inbox.mail.InMail.getByIdAndReceiverBox";

  /**
     *
     */
  public static final String LAU_NQ_INMAIL_GET_EVENTS = "si.laurentius.inbox.event.InEvent.getList";

  /**
     *
     */
  public static final String LAU_NQ_INMAIL_GET_LIST = "si.laurentius.inbox.mail.InMail.getList";

  /**
     *
     */
  public static final String LAU_NQ_OUTMAIL_GET_EVENTS =
      "si.laurentius.outbox.event.OutEvent.getList";

  /**
     *
     */
  public static final String LAU_NQ_OUTMAIL_GET_LIST = "si.laurentius.outbox.mail.OutMail.getList";

  /**
     *
     */
  public static final String LAU_NQ_OUTMAIL_getByMessageIdAndSenderBox =
      "si.laurentius.outbox.mail.OutMail.getByMessageIdAndSenderBox";

}

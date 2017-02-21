/*
 * Copyright 2015, Supreme Court Republic of Slovenia
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
package si.laurentius.commons;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class SEDValues {

  /**
     *
     */
  public static final String EBMS_JMS_CONNECTION_FACTORY_JNDI = "ConnectionFactory";

  /**
     * ActiveMQ value for delay!
     */
  public static final String EBMS_QUEUE_DELAY_AMQ = "AMQ_SCHEDULED_DELAY"; // ActiveMQ value for delay!

  /**
     * Artemis value for delay!
     */
    public static final String EBMS_QUEUE_DELAY_Artemis = "_AMQ_SCHED_DELIVERY"; // artemis value for delay!

  /**
     *
     */
  public static final String EBMS_QUEUE_PARAM_DELAY = "ebms_queue_mail_delay";

  /**
     *
     */
  public static final String EBMS_QUEUE_PARAM_MAIL_ID = "ebms_queue_mail_id";
  

  
  /**
     *
     */
  public static final String EBMS_QUEUE_PARAM_RETRY = "ebms_queue_mail_retry";

  /**
     *
     */
  public static final String ENCODING_BASE64 = "base64";

  /**
     *
     */
  public static final String ENCODING_UTF8 = "UTF-8";

  /**
     *
     */
  public static final String EXEC_COMMAND = "lau_exec_command";

  /**
     *
     */
  public static final String EXEC_PARAMS = "lau_exec_params";

  /**
     *
     */
  public static final String JNDI_QUEUE_EBMS = "queue/MSHQueue";

  /**
     * queue name for executing 
     */
  public static final String JNDI_QUEUE_IN_MAIL_PROCESS= "queue/MSHInMailProcessQueue";

  

}

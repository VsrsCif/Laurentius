/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.enums;

/**
 *
 * @author sluzba
 */
public enum JMSArtemisAttribute {
  Address("Address", java.lang.String.class, "address this queue is bound to"),
  ConsumerCount("ConsumerCount",  java.lang.Integer.class,
          "number of consumers consuming messages from this queue"),
  DeadLetterAddress("DeadLetterAddress", java.lang.String.class,
          "dead-letter address associated with this queue"),
  
  Durable("Durable ", boolean.class, "whether this queue is durable"),
  ExpiryAddress("ExpiryAddress", java.lang.String.class,
          "expiry address associated with this queue"),
  Filter("Filter", java.lang.String.class, "filter associated with this queue"),
  FirstMessageAge("FirstMessageAge ", java.lang.Long.class,
          "age of the first message in milliseconds"),
  FirstMessageAsJSON("FirstMessageAsJSON", java.lang.String.class,
          "first message on the queue as JSON"),
  FirstMessageTimestamp("FirstMessageTimestamp", java.lang.Long.class,
          "timestamp of the first message in milliseconds"),
  MessageCount("MessageCount", java.lang.Integer.class,
          "number of messages currently in this queue (includes scheduled, paged, and in-delivery messages)"),
 
  MessagesAdded("MessagesAdded", java.lang.Integer.class,
          "number of messages added to this queue since it was created"),
 
  Paused("Paused ", boolean.class, "whether the queue is paused"),
  ScheduledCount("ScheduledCount", java.lang.Integer.class,
          "number of scheduled messages in this queue"),
  Temporary("Temporary ", boolean.class, "whether this queue is temporary");

  String attName;
  Class attValueClass;
  String attDesc;

  private JMSArtemisAttribute(String name, Class cls, String desc) {
    this.attName = name;
    this.attValueClass = cls;
    this.attDesc = desc;
  }

  public String getName() {
    return attName;
  }

  public Class getValueClass() {
    return attValueClass;
  }

  public String getDesc() {
    return attDesc;
  }

}

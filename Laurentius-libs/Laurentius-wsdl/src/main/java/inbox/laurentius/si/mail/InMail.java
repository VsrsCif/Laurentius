
package inbox.laurentius.si.mail;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import inbox.laurentius.si.payload.InPayload;
import inbox.laurentius.si.property.InProperties;
import org.w3._2001.xmlschema.Adapter1;


/**
 * Shema določa xml infoset izhodne in dohodne pošte, ki se vroča preko SVEV MSH modula. Sestavni deli vsakega maila so:
 *                 - pošiljatelj
 *                 - prejemnik
 *                 - storitev (Storitev, akcija v storitvi)
 *                 - vsebine, ki se vročajo				
 *             
 * 
 * <p>Java class for InMailType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InMailType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://si.laurentius.inbox/property}InProperties" minOccurs="0"/>
 *         &lt;element ref="{http://si.laurentius.inbox/payload}InPayload" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="messageId">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="64"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="senderMessageId" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="64"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="service" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;maxLength value="64"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="action" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;maxLength value="64"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="conversationId">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="64"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="subject">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="512"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="submittedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="sentDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="receivedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="deliveredDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="receiverEBox" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;maxLength value="64"/>
 *             &lt;pattern value="[_\-a-zA-Z0-9\.\+]+@[a-zA-Z0-9](\.?[\-a-zA-Z0-9]*[a-zA-Z0-9])*"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="receiverName" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="128"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="senderEBox" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;maxLength value="64"/>
 *             &lt;pattern value="[_\-a-zA-Z0-9\.\+]+@[a-zA-Z0-9](\.?[\-a-zA-Z0-9]*[a-zA-Z0-9])*"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="senderName" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="128"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="status">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;maxLength value="32"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="statusDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InMailType", propOrder = {
    "inProperties",
    "inPayload"
})
@XmlRootElement(name = "InMail")
public class InMail
    implements Serializable
{

    @XmlElement(name = "InProperties", namespace = "http://si.laurentius.inbox/property")
    protected InProperties inProperties;
    @XmlElement(name = "InPayload", namespace = "http://si.laurentius.inbox/payload")
    protected InPayload inPayload;
    @XmlAttribute(name = "id")
    protected BigInteger id;
    @XmlAttribute(name = "messageId")
    protected String messageId;
    @XmlAttribute(name = "senderMessageId", required = true)
    protected String senderMessageId;
    @XmlAttribute(name = "service", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String service;
    @XmlAttribute(name = "action", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String action;
    @XmlAttribute(name = "conversationId")
    protected String conversationId;
    @XmlAttribute(name = "subject")
    protected String subject;
    @XmlAttribute(name = "submittedDate")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date submittedDate;
    @XmlAttribute(name = "sentDate")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date sentDate;
    @XmlAttribute(name = "receivedDate")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date receivedDate;
    @XmlAttribute(name = "deliveredDate")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date deliveredDate;
    @XmlAttribute(name = "receiverEBox", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String receiverEBox;
    @XmlAttribute(name = "receiverName", required = true)
    protected String receiverName;
    @XmlAttribute(name = "senderEBox", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String senderEBox;
    @XmlAttribute(name = "senderName", required = true)
    protected String senderName;
    @XmlAttribute(name = "status")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String status;
    @XmlAttribute(name = "statusDate")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date statusDate;

    /**
     * Gets the value of the inProperties property.
     * 
     * @return
     *     possible object is
     *     {@link InProperties }
     *     
     */
    public InProperties getInProperties() {
        return inProperties;
    }

    /**
     * Sets the value of the inProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link InProperties }
     *     
     */
    public void setInProperties(InProperties value) {
        this.inProperties = value;
    }

    /**
     * Gets the value of the inPayload property.
     * 
     * @return
     *     possible object is
     *     {@link InPayload }
     *     
     */
    public InPayload getInPayload() {
        return inPayload;
    }

    /**
     * Sets the value of the inPayload property.
     * 
     * @param value
     *     allowed object is
     *     {@link InPayload }
     *     
     */
    public void setInPayload(InPayload value) {
        this.inPayload = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setId(BigInteger value) {
        this.id = value;
    }

    /**
     * Gets the value of the messageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the value of the messageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageId(String value) {
        this.messageId = value;
    }

    /**
     * Gets the value of the senderMessageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderMessageId() {
        return senderMessageId;
    }

    /**
     * Sets the value of the senderMessageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderMessageId(String value) {
        this.senderMessageId = value;
    }

    /**
     * Gets the value of the service property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getService() {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setService(String value) {
        this.service = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAction(String value) {
        this.action = value;
    }

    /**
     * Gets the value of the conversationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConversationId() {
        return conversationId;
    }

    /**
     * Sets the value of the conversationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConversationId(String value) {
        this.conversationId = value;
    }

    /**
     * Gets the value of the subject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubject(String value) {
        this.subject = value;
    }

    /**
     * Gets the value of the submittedDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getSubmittedDate() {
        return submittedDate;
    }

    /**
     * Sets the value of the submittedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubmittedDate(Date value) {
        this.submittedDate = value;
    }

    /**
     * Gets the value of the sentDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getSentDate() {
        return sentDate;
    }

    /**
     * Sets the value of the sentDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSentDate(Date value) {
        this.sentDate = value;
    }

    /**
     * Gets the value of the receivedDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getReceivedDate() {
        return receivedDate;
    }

    /**
     * Sets the value of the receivedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivedDate(Date value) {
        this.receivedDate = value;
    }

    /**
     * Gets the value of the deliveredDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getDeliveredDate() {
        return deliveredDate;
    }

    /**
     * Sets the value of the deliveredDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeliveredDate(Date value) {
        this.deliveredDate = value;
    }

    /**
     * Gets the value of the receiverEBox property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiverEBox() {
        return receiverEBox;
    }

    /**
     * Sets the value of the receiverEBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiverEBox(String value) {
        this.receiverEBox = value;
    }

    /**
     * Gets the value of the receiverName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiverName() {
        return receiverName;
    }

    /**
     * Sets the value of the receiverName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiverName(String value) {
        this.receiverName = value;
    }

    /**
     * Gets the value of the senderEBox property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderEBox() {
        return senderEBox;
    }

    /**
     * Sets the value of the senderEBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderEBox(String value) {
        this.senderEBox = value;
    }

    /**
     * Gets the value of the senderName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Sets the value of the senderName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderName(String value) {
        this.senderName = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the statusDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getStatusDate() {
        return statusDate;
    }

    /**
     * Sets the value of the statusDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusDate(Date value) {
        this.statusDate = value;
    }

}

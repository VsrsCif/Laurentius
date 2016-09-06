
package laurentius.si;

import java.io.Serializable;
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
import control.laurentius.si.Control;
import org.w3._2001.xmlschema.Adapter1;


/**
 *  
 *                     Poizvedba za pridobivanje sezname izhodne pošte.
 *                 
 * 
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Control" type="{http://si.laurentius.Control}Control"/>
 *         &lt;element name="Data">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="senderEBox">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *                       &lt;maxLength value="64"/>
 *                       &lt;pattern value="[_\-a-zA-Z0-9\.\+]+@[a-zA-Z0-9](\.?[\-a-zA-Z0-9]*[a-zA-Z0-9])*"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;attribute name="receiverEBox">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *                       &lt;maxLength value="64"/>
 *                       &lt;pattern value="[_\-a-zA-Z0-9\.\+]+@[a-zA-Z0-9](\.?[\-a-zA-Z0-9]*[a-zA-Z0-9])*"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;attribute name="submittedDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                 &lt;attribute name="submittedDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                 &lt;attribute name="status">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *                       &lt;length value="20"/>
 *                       &lt;enumeration value="received"/>
 *                       &lt;enumeration value="processed"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;attribute name="service" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *                       &lt;maxLength value="64"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;attribute name="action" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *                       &lt;maxLength value="64"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;attribute name="conversationId">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;maxLength value="64"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "control",
    "data"
})
@XmlRootElement(name = "OutMailListRequest")
public class OutMailListRequest
    implements Serializable
{

    @XmlElement(name = "Control", required = true)
    protected Control control;
    @XmlElement(name = "Data", required = true)
    protected OutMailListRequest.Data data;

    /**
     * Gets the value of the control property.
     * 
     * @return
     *     possible object is
     *     {@link Control }
     *     
     */
    public Control getControl() {
        return control;
    }

    /**
     * Sets the value of the control property.
     * 
     * @param value
     *     allowed object is
     *     {@link Control }
     *     
     */
    public void setControl(Control value) {
        this.control = value;
    }

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     {@link OutMailListRequest.Data }
     *     
     */
    public OutMailListRequest.Data getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link OutMailListRequest.Data }
     *     
     */
    public void setData(OutMailListRequest.Data value) {
        this.data = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="senderEBox">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
     *             &lt;maxLength value="64"/>
     *             &lt;pattern value="[_\-a-zA-Z0-9\.\+]+@[a-zA-Z0-9](\.?[\-a-zA-Z0-9]*[a-zA-Z0-9])*"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *       &lt;attribute name="receiverEBox">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
     *             &lt;maxLength value="64"/>
     *             &lt;pattern value="[_\-a-zA-Z0-9\.\+]+@[a-zA-Z0-9](\.?[\-a-zA-Z0-9]*[a-zA-Z0-9])*"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *       &lt;attribute name="submittedDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *       &lt;attribute name="submittedDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *       &lt;attribute name="status">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
     *             &lt;length value="20"/>
     *             &lt;enumeration value="received"/>
     *             &lt;enumeration value="processed"/>
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
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Data
        implements Serializable
    {

        @XmlAttribute(name = "senderEBox")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String senderEBox;
        @XmlAttribute(name = "receiverEBox")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String receiverEBox;
        @XmlAttribute(name = "submittedDateFrom")
        @XmlJavaTypeAdapter(Adapter1 .class)
        @XmlSchemaType(name = "dateTime")
        protected Date submittedDateFrom;
        @XmlAttribute(name = "submittedDateTo")
        @XmlJavaTypeAdapter(Adapter1 .class)
        @XmlSchemaType(name = "dateTime")
        protected Date submittedDateTo;
        @XmlAttribute(name = "status")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String status;
        @XmlAttribute(name = "service", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String service;
        @XmlAttribute(name = "action", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String action;
        @XmlAttribute(name = "conversationId")
        protected String conversationId;

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
         * Gets the value of the submittedDateFrom property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public Date getSubmittedDateFrom() {
            return submittedDateFrom;
        }

        /**
         * Sets the value of the submittedDateFrom property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSubmittedDateFrom(Date value) {
            this.submittedDateFrom = value;
        }

        /**
         * Gets the value of the submittedDateTo property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public Date getSubmittedDateTo() {
            return submittedDateTo;
        }

        /**
         * Sets the value of the submittedDateTo property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSubmittedDateTo(Date value) {
            this.submittedDateTo = value;
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

    }

}

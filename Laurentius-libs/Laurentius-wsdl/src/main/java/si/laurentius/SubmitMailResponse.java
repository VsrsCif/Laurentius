
package si.laurentius;

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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.w3._2001.xmlschema.Adapter1;
import si.laurentius.rcontrol.RControl;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RControl" type="{http://si.laurentius.RControl}RControl"/>
 *         &lt;element name="RData">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="mailId" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="senderMessageId" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;maxLength value="64"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;attribute name="submittedDate" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
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
    "rControl",
    "rData"
})
@XmlRootElement(name = "SubmitMailResponse")
public class SubmitMailResponse
    implements Serializable
{

    @XmlElement(name = "RControl", required = true)
    protected RControl rControl;
    @XmlElement(name = "RData", required = true)
    protected SubmitMailResponse.RData rData;

    /**
     * Gets the value of the rControl property.
     * 
     * @return
     *     possible object is
     *     {@link RControl }
     *     
     */
    public RControl getRControl() {
        return rControl;
    }

    /**
     * Sets the value of the rControl property.
     * 
     * @param value
     *     allowed object is
     *     {@link RControl }
     *     
     */
    public void setRControl(RControl value) {
        this.rControl = value;
    }

    /**
     * Gets the value of the rData property.
     * 
     * @return
     *     possible object is
     *     {@link SubmitMailResponse.RData }
     *     
     */
    public SubmitMailResponse.RData getRData() {
        return rData;
    }

    /**
     * Sets the value of the rData property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubmitMailResponse.RData }
     *     
     */
    public void setRData(SubmitMailResponse.RData value) {
        this.rData = value;
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
     *       &lt;attribute name="mailId" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="senderMessageId" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;maxLength value="64"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *       &lt;attribute name="submittedDate" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class RData
        implements Serializable
    {

        @XmlAttribute(name = "mailId", required = true)
        protected BigInteger mailId;
        @XmlAttribute(name = "senderMessageId", required = true)
        protected String senderMessageId;
        @XmlAttribute(name = "submittedDate", required = true)
        @XmlJavaTypeAdapter(Adapter1 .class)
        @XmlSchemaType(name = "dateTime")
        protected Date submittedDate;

        /**
         * Gets the value of the mailId property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getMailId() {
            return mailId;
        }

        /**
         * Sets the value of the mailId property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setMailId(BigInteger value) {
            this.mailId = value;
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

    }

}

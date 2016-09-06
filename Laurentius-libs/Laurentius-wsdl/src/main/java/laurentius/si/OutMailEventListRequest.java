
package laurentius.si;

import java.io.Serializable;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import control.laurentius.si.Control;


/**
 *  
 *                     Poizvedba za pridobivanje dogodkov/statusov na izhodni pošiljki. Dogodki so sortirani od najnovejšega do najstarejšega dogodka.
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
 *                 &lt;attribute name="senderEBox" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *                       &lt;maxLength value="64"/>
 *                       &lt;pattern value="[_\-a-zA-Z0-9\.\+]+@[a-zA-Z0-9](\.?[\-a-zA-Z0-9]*[a-zA-Z0-9])*"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;attribute name="mailId" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="senderMessageId">
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
@XmlRootElement(name = "OutMailEventListRequest")
public class OutMailEventListRequest
    implements Serializable
{

    @XmlElement(name = "Control", required = true)
    protected Control control;
    @XmlElement(name = "Data", required = true)
    protected OutMailEventListRequest.Data data;

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
     *     {@link OutMailEventListRequest.Data }
     *     
     */
    public OutMailEventListRequest.Data getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link OutMailEventListRequest.Data }
     *     
     */
    public void setData(OutMailEventListRequest.Data value) {
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
     *       &lt;attribute name="senderEBox" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
     *             &lt;maxLength value="64"/>
     *             &lt;pattern value="[_\-a-zA-Z0-9\.\+]+@[a-zA-Z0-9](\.?[\-a-zA-Z0-9]*[a-zA-Z0-9])*"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *       &lt;attribute name="mailId" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="senderMessageId">
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

        @XmlAttribute(name = "senderEBox", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String senderEBox;
        @XmlAttribute(name = "mailId")
        protected BigInteger mailId;
        @XmlAttribute(name = "senderMessageId")
        protected String senderMessageId;

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

    }

}

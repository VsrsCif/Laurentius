
package laurentius.si;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SEDException complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SEDException">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="errorCode" type="{http://si.laurentius}SEDExceptionCode" minOccurs="0"/>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="errorDump" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SEDException", propOrder = {
    "errorCode",
    "message",
    "errorDump"
})
@XmlRootElement(name = "SEDException")
public class SEDException
    implements Serializable
{

    @XmlElement(namespace = "http://si.laurentius")
    protected SEDExceptionCode errorCode;
    @XmlElement(namespace = "http://si.laurentius")
    protected String message;
    @XmlElement(namespace = "http://si.laurentius")
    protected String errorDump;

    /**
     * Gets the value of the errorCode property.
     * 
     * @return
     *     possible object is
     *     {@link SEDExceptionCode }
     *     
     */
    public SEDExceptionCode getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the value of the errorCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link SEDExceptionCode }
     *     
     */
    public void setErrorCode(SEDExceptionCode value) {
        this.errorCode = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Gets the value of the errorDump property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorDump() {
        return errorDump;
    }

    /**
     * Sets the value of the errorDump property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorDump(String value) {
        this.errorDump = value;
    }

}

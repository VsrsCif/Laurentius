
package rcontrol.laurentius.si;

import java.io.Serializable;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.w3._2001.xmlschema.Adapter3;


/**
 * <p>Java class for RControl complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RControl">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="resultSize" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="startIndex" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="responseSize" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="returnValue" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;enumeration value="1"/>
 *             &lt;enumeration value="2"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="returnCode" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="returnText" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="errorDump" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RControl")
public class RControl
    implements Serializable
{

    @XmlAttribute(name = "resultSize")
    protected BigInteger resultSize;
    @XmlAttribute(name = "startIndex")
    protected BigInteger startIndex;
    @XmlAttribute(name = "responseSize")
    protected BigInteger responseSize;
    @XmlAttribute(name = "returnValue", required = true)
    @XmlJavaTypeAdapter(Adapter3 .class)
    protected Integer returnValue;
    @XmlAttribute(name = "returnCode", required = true)
    @XmlJavaTypeAdapter(Adapter3 .class)
    @XmlSchemaType(name = "int")
    protected Integer returnCode;
    @XmlAttribute(name = "returnText")
    protected String returnText;
    @XmlAttribute(name = "errorDump")
    protected String errorDump;

    /**
     * Gets the value of the resultSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getResultSize() {
        return resultSize;
    }

    /**
     * Sets the value of the resultSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setResultSize(BigInteger value) {
        this.resultSize = value;
    }

    /**
     * Gets the value of the startIndex property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getStartIndex() {
        return startIndex;
    }

    /**
     * Sets the value of the startIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setStartIndex(BigInteger value) {
        this.startIndex = value;
    }

    /**
     * Gets the value of the responseSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getResponseSize() {
        return responseSize;
    }

    /**
     * Sets the value of the responseSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setResponseSize(BigInteger value) {
        this.responseSize = value;
    }

    /**
     * Gets the value of the returnValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Integer getReturnValue() {
        return returnValue;
    }

    /**
     * Sets the value of the returnValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReturnValue(Integer value) {
        this.returnValue = value;
    }

    /**
     * Gets the value of the returnCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Integer getReturnCode() {
        return returnCode;
    }

    /**
     * Sets the value of the returnCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReturnCode(Integer value) {
        this.returnCode = value;
    }

    /**
     * Gets the value of the returnText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReturnText() {
        return returnText;
    }

    /**
     * Sets the value of the returnText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReturnText(String value) {
        this.returnText = value;
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

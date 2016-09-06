
package si.laurentius;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import si.laurentius.control.Control;
import si.laurentius.outbox.mail.OutMail;


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
 *         &lt;element name="Control" type="{http://si.laurentius.Control}Control"/>
 *         &lt;element name="Data">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://si.laurentius.outbox/mail}OutMail"/>
 *                 &lt;/sequence>
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
@XmlRootElement(name = "SubmitMailRequest")
public class SubmitMailRequest
    implements Serializable
{

    @XmlElement(name = "Control", required = true)
    protected Control control;
    @XmlElement(name = "Data", required = true)
    protected SubmitMailRequest.Data data;

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
     *     {@link SubmitMailRequest.Data }
     *     
     */
    public SubmitMailRequest.Data getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubmitMailRequest.Data }
     *     
     */
    public void setData(SubmitMailRequest.Data value) {
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
     *       &lt;sequence>
     *         &lt;element ref="{http://si.laurentius.outbox/mail}OutMail"/>
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
        "outMail"
    })
    public static class Data
        implements Serializable
    {

        @XmlElement(name = "OutMail", namespace = "http://si.laurentius.outbox/mail", required = true)
        protected OutMail outMail;

        /**
         * Gets the value of the outMail property.
         * 
         * @return
         *     possible object is
         *     {@link OutMail }
         *     
         */
        public OutMail getOutMail() {
            return outMail;
        }

        /**
         * Sets the value of the outMail property.
         * 
         * @param value
         *     allowed object is
         *     {@link OutMail }
         *     
         */
        public void setOutMail(OutMail value) {
            this.outMail = value;
        }

    }

}

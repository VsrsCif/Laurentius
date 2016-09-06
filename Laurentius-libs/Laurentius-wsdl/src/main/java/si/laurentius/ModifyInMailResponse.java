
package si.laurentius;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import si.laurentius.inbox.event.InEvent;
import si.laurentius.rcontrol.RControl;


/**
 *  
 *                     Poizvedba za pridobivanje statusov izhodnih pošiljkah. Dogodki so sortirani od  najnovejšega do najstarejšega!
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
 *         &lt;element name="RControl" type="{http://si.laurentius.RControl}RControl"/>
 *         &lt;element name="RData">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="InEvent" type="{http://si.laurentius.inbox/event}InEvent"/>
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
    "rControl",
    "rData"
})
@XmlRootElement(name = "ModifyInMailResponse")
public class ModifyInMailResponse
    implements Serializable
{

    @XmlElement(name = "RControl", required = true)
    protected RControl rControl;
    @XmlElement(name = "RData", required = true)
    protected ModifyInMailResponse.RData rData;

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
     *     {@link ModifyInMailResponse.RData }
     *     
     */
    public ModifyInMailResponse.RData getRData() {
        return rData;
    }

    /**
     * Sets the value of the rData property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModifyInMailResponse.RData }
     *     
     */
    public void setRData(ModifyInMailResponse.RData value) {
        this.rData = value;
    }


    /**
     *  
     *                                 Potrjevanje prejema dohodne pošte. Pošta je ob potrditvi prestavljena v status "Processed"
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
     *         &lt;element name="InEvent" type="{http://si.laurentius.inbox/event}InEvent"/>
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
        "inEvent"
    })
    public static class RData
        implements Serializable
    {

        @XmlElement(name = "InEvent", required = true)
        protected InEvent inEvent;

        /**
         * Gets the value of the inEvent property.
         * 
         * @return
         *     possible object is
         *     {@link InEvent }
         *     
         */
        public InEvent getInEvent() {
            return inEvent;
        }

        /**
         * Sets the value of the inEvent property.
         * 
         * @param value
         *     allowed object is
         *     {@link InEvent }
         *     
         */
        public void setInEvent(InEvent value) {
            this.inEvent = value;
        }

    }

}

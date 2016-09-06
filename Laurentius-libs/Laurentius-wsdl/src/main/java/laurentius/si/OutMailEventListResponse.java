
package laurentius.si;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import outbox.laurentius.si.event.OutEvent;
import rcontrol.laurentius.si.RControl;


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
 *                   &lt;element name="OutEvent" type="{http://si.laurentius.outbox/event}OutEvent" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlRootElement(name = "OutMailEventListResponse")
public class OutMailEventListResponse
    implements Serializable
{

    @XmlElement(name = "RControl", required = true)
    protected RControl rControl;
    @XmlElement(name = "RData", required = true)
    protected OutMailEventListResponse.RData rData;

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
     *     {@link OutMailEventListResponse.RData }
     *     
     */
    public OutMailEventListResponse.RData getRData() {
        return rData;
    }

    /**
     * Sets the value of the rData property.
     * 
     * @param value
     *     allowed object is
     *     {@link OutMailEventListResponse.RData }
     *     
     */
    public void setRData(OutMailEventListResponse.RData value) {
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
     *       &lt;sequence>
     *         &lt;element name="OutEvent" type="{http://si.laurentius.outbox/event}OutEvent" maxOccurs="unbounded" minOccurs="0"/>
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
        "outEvents"
    })
    public static class RData
        implements Serializable
    {

        @XmlElement(name = "OutEvent")
        protected List<OutEvent> outEvents;

        /**
         * Gets the value of the outEvents property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the outEvents property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOutEvents().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link OutEvent }
         * 
         * 
         */
        public List<OutEvent> getOutEvents() {
            if (outEvents == null) {
                outEvents = new ArrayList<OutEvent>();
            }
            return this.outEvents;
        }

    }

}

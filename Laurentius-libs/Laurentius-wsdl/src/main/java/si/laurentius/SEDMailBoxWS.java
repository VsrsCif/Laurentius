
package si.laurentius;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b14002
 * Generated source version: 2.2
 * 
 */
@WebService(name = "SEDMailBoxWS", targetNamespace = "http://si.laurentius")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    si.laurentius.ObjectFactory.class,
    si.laurentius.inbox.payload.ObjectFactory.class,
    si.laurentius.outbox.payload.ObjectFactory.class,
    si.laurentius.control.ObjectFactory.class,
    si.laurentius.inbox.event.ObjectFactory.class,
    si.laurentius.inbox.mail.ObjectFactory.class,
    si.laurentius.inbox.property.ObjectFactory.class,
    si.laurentius.outbox.event.ObjectFactory.class,
    si.laurentius.outbox.mail.ObjectFactory.class,
    si.laurentius.outbox.property.ObjectFactory.class,
    si.laurentius.rcontrol.ObjectFactory.class
})
public interface SEDMailBoxWS {


    /**
     * 
     *                 Oddaja pošiljke v vročanje/dostavo
     *             
     * 
     * @param submitMailRequest
     * @return
     *     returns si.laurentius.SubmitMailResponse
     * @throws SEDException_Exception
     */
    @WebMethod(action = "http://si.laurentius.submitMail")
    @WebResult(name = "SubmitMailResponse", targetNamespace = "http://si.laurentius", partName = "SubmitMailResponse")
    public SubmitMailResponse submitMail(
        @WebParam(name = "SubmitMailRequest", targetNamespace = "http://si.laurentius", partName = "SubmitMailRequest")
        SubmitMailRequest submitMailRequest)
        throws SEDException_Exception
    ;

    /**
     * 
     *                 Pregled pošte v vročanje
     *             
     * 
     * @param outMailListRequest
     * @return
     *     returns si.laurentius.OutMailListResponse
     * @throws SEDException_Exception
     */
    @WebMethod(action = "http://si.laurentius.getOutMailList")
    @WebResult(name = "OutMailListResponse", targetNamespace = "http://si.laurentius", partName = "OutMailListResponse")
    public OutMailListResponse getOutMailList(
        @WebParam(name = "OutMailListRequest", targetNamespace = "http://si.laurentius", partName = "OutMailListRequest")
        OutMailListRequest outMailListRequest)
        throws SEDException_Exception
    ;

    /**
     * 
     *                 Pregled dohodne pošte
     *             
     * 
     * @param intMailListRequest
     * @return
     *     returns si.laurentius.InMailListResponse
     * @throws SEDException_Exception
     */
    @WebMethod(action = "http://si.laurentius.getInMailList")
    @WebResult(name = "InMailListResponse", targetNamespace = "http://si.laurentius", partName = "InMailListResponse")
    public InMailListResponse getInMailList(
        @WebParam(name = "InMailListRequest", targetNamespace = "http://si.laurentius", partName = "IntMailListRequest")
        InMailListRequest intMailListRequest)
        throws SEDException_Exception
    ;

    /**
     * 
     *                 Pregled dogodtkov na dohodni pošiljke
     *             
     * 
     * @param inMailEventListRequest
     * @return
     *     returns si.laurentius.InMailEventListResponse
     * @throws SEDException_Exception
     */
    @WebMethod(action = "http://si.laurentius.getInMailEventList")
    @WebResult(name = "InMailEventListResponse", targetNamespace = "http://si.laurentius", partName = "InMailEventListResponse")
    public InMailEventListResponse getInMailEventList(
        @WebParam(name = "InMailEventListRequest", targetNamespace = "http://si.laurentius", partName = "InMailEventListRequest")
        InMailEventListRequest inMailEventListRequest)
        throws SEDException_Exception
    ;

    /**
     * 
     *                 Pregled dogodkov na izhodni pošiljke
     *             
     * 
     * @param outMailEventListRequest
     * @return
     *     returns si.laurentius.OutMailEventListResponse
     * @throws SEDException_Exception
     */
    @WebMethod(action = "http://si.laurentius.getOutMailEventList")
    @WebResult(name = "OutMailEventListResponse", targetNamespace = "http://si.laurentius", partName = "OutMailEventListResponse")
    public OutMailEventListResponse getOutMailEventList(
        @WebParam(name = "OutMailEventListRequest", targetNamespace = "http://si.laurentius", partName = "OutMailEventListRequest")
        OutMailEventListRequest outMailEventListRequest)
        throws SEDException_Exception
    ;

    /**
     * 
     *                 Zaklepanje in prevzemanje dohodne pošte
     *             
     * 
     * @param modifyInMailRequest
     * @return
     *     returns si.laurentius.ModifyInMailResponse
     * @throws SEDException_Exception
     */
    @WebMethod(action = "http://si.laurentius.modifyInMail")
    @WebResult(name = "ModifyInMailResponse", targetNamespace = "http://si.laurentius", partName = "ModifyInMailResponse")
    public ModifyInMailResponse modifyInMail(
        @WebParam(name = "ModifyInMailRequest", targetNamespace = "http://si.laurentius", partName = "ModifyInMailRequest")
        ModifyInMailRequest modifyInMailRequest)
        throws SEDException_Exception
    ;

    /**
     * 
     *                 Zaklepanje in prevzemanje dohodne pošte
     *             
     * 
     * @param modifyOutMailRequest
     * @return
     *     returns si.laurentius.ModifyOutMailResponse
     * @throws SEDException_Exception
     */
    @WebMethod(action = "http://si.laurentius.modifyOutMail")
    @WebResult(name = "ModifyOutMailResponse", targetNamespace = "http://si.laurentius", partName = "ModifyOutMailResponse")
    public ModifyOutMailResponse modifyOutMail(
        @WebParam(name = "ModifyOutMailRequest", targetNamespace = "http://si.laurentius", partName = "ModifyOutMailRequest")
        ModifyOutMailRequest modifyOutMailRequest)
        throws SEDException_Exception
    ;

    /**
     * 
     *                 Pridobivanje binarne vsebine dohodne pošiljke
     *             
     * 
     * @param getInMailRequest
     * @return
     *     returns si.laurentius.GetInMailResponse
     * @throws SEDException_Exception
     */
    @WebMethod(action = "http://si.laurentius.getInMail")
    @WebResult(name = "GetInMailResponse", targetNamespace = "http://si.laurentius", partName = "GetInMailResponse")
    public GetInMailResponse getInMail(
        @WebParam(name = "GetInMailRequest", targetNamespace = "http://si.laurentius", partName = "GetInMailRequest")
        GetInMailRequest getInMailRequest)
        throws SEDException_Exception
    ;

}

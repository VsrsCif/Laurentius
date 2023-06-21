/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.zkp.enums;

import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;

/**
 *
 */
public enum FopTransformation {

    AdviceOfDelivery("ZKP-AdviceOfDelivery.fo", MSHInMail.class),
    DeliveryNotification("ZKP-DeliveryNotification.fo", MSHOutMail.class),
    RecipientAddressNotExists("ZKP-RecipientAddressNotExists.fo", MSHOutMail.class),
    NotDeliveredNotification("ZKP-NotDeliveredNotification.fo", MSHOutMail.class),
    ;

    private final String mstrfileName;
    private Class mcJaxbClass;

    FopTransformation(String filename, Class jaxbClass) {
        mstrfileName = filename;
        mcJaxbClass = jaxbClass;
    }

    /**
     * @return
     */
    public String getFileName() {
        return mstrfileName;
    }

    public Class getJaxbClass() {
        return mcJaxbClass;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.digics.laurentius.plugin.zkp.enums;

import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;

/**
   *
   */
  public enum FopTransformation {

    AdviceOfDelivery("LegalDelivery_ZPP-AdviceOfDelivery.fo", MSHInMail.class),
    AdviceOfDeliveryFiction("LegalDelivery_ZPP-AdviceOfDeliveryFiction.fo", MSHOutMail.class ),
    AdviceOfDeliveryFiction_6Odst("LegalDelivery_ZPP-AdviceOfDeliveryFiction_6Odst.fo", MSHOutMail.class),
    AdviceOfDeliveryFictionNotification("LegalDelivery_ZPP-AdviceOfDeliveryFictionNotification.fo", MSHOutMail.class),
    AdviceOfDeliveryFictionNotification_6Odst("LegalDelivery_ZPP-AdviceOfDeliveryFictionNotification_6Odst.fo",MSHOutMail.class),
    DeliveryNotification("LegalDelivery_ZPP-DeliveryNotification.fo",MSHOutMail.class),
    DeliveryNotificationB("LegalDelivery_ZPPB-DeliveryNotification.fo",MSHOutMail.class),    
    DeliveryReciept("LegalDelivery_ZPP-DeliveryReciept.fo",MSHInMail.class),
    DeliveryRecieptB("LegalDelivery_ZPPB-DeliveryReciept.fo",MSHInMail.class),
    ReceiverAddressNotExists("ReceiverAddressNotExists.fo",MSHOutMail.class),

    
    ;
    
    
    

    private final String mstrfileName;
    private Class mcJaxbClass;

    FopTransformation(String filename, Class jaxbClass) {
      mstrfileName = filename;
      mcJaxbClass = jaxbClass;
    }

    /**
     *
     * @return
     */
    public String getFileName() {
      return mstrfileName;
    }
    
    public Class getJaxbClass() {
      return mcJaxbClass;
    }
  }

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.zpp.enums;

/**
   *
   */
  public enum FopTransformation {

    /**
     *
     */
    DeliveryNotification("LegalDelivery_ZPP-DeliveryNotification.fo"),
    AdviceOfDelivery("LegalDelivery_ZPP-AdviceOfDelivery.fo"),
    AdviceOfDeliveryFictionNotification("LegalDelivery_ZPP-AdviceOfDeliveryFictionNotification.fo"),
    AdviceOfDeliveryFiction("LegalDelivery_ZPP-AdviceOfDeliveryFiction.fo"),
    DeliveryReciept("LegalDelivery_ZPP-DeliveryReciept.fo");

    private final String mstrfileName;

    FopTransformation(String filename) {
      mstrfileName = filename;
    }

    /**
     *
     * @return
     */
    public String getFileName() {
      return mstrfileName;
    }
  }

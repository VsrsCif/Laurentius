/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author sluzba
 */
public class DisableServiceUtils {
  public static final SEDLogger LOG = new SEDLogger(DisableServiceUtils.class);

  public static List<DisableService> STDisableList = new ArrayList<>();

  public static synchronized void addNewDisableService(String service, String senderBox, String receiverBox) {

    if (!existsDisableService(service, senderBox, receiverBox)){
      STDisableList.add(new DisableService(service, receiverBox, senderBox));
    }

  }

  public static synchronized boolean existsDisableService(String service, String senderBox,
      String receiverBox) {
    LOG.formatedlog("Disable list count: %d",STDisableList.size() );
    return STDisableList.stream().anyMatch((ds) ->
        (Objects.equals(ds.getServiceId(), service) &&
            Objects.equals(ds.getReceiverBox(), receiverBox) &&
            Objects.equals(ds.getSenderBox(), senderBox)));
  }

}

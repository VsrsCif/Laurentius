package si.laurentius.msh.web.gui.dlg;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.PModeException;

import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.web.abst.AbstractJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "testAddressDialog")
public class DialogTestAddress extends AbstractJSFView {

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  private PModeInterface mPMode;

  private static final SEDLogger LOG = new SEDLogger(DialogTestAddress.class);

  String address;

  public void testAddress() {
     LOG.formatedlog("Test address %s", getAddress());
    if (Utils.isEmptyString(address)) {
      addDialogError("Address is empty!");
      return;
    }

    if (!Utils.isValidEmailAddress(address)) {
      addDialogError("Address is not valid email address");
      return;
    }

    PartyIdentitySet pis = null;
    try {
        pis = mPMode.getPartyIdentitySetForSEDAddress(address);
    } catch (PModeException pme) {
      addDialogError(pme.getMessage());
      return;
    }
    
    // submit signale
    // wait for response
    // show result
    
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  
  public void addDialogError(String message) {
    LOG.logWarn(message, null);
    addError(message);
    addCallbackParam(CB_PARA_SUCCESS, false);
  }

}

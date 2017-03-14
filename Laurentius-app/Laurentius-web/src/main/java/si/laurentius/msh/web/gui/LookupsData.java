package si.laurentius.msh.web.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDInterceptorEvent;
import si.laurentius.commons.enums.SEDInterceptorRole;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.enums.SEDRulePredicate;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.ReflectUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.ebox.SEDBox;
import si.laurentius.msh.mail.MSHMailType;
import si.laurentius.msh.web.abst.AbstractJSFView;
import si.laurentius.plugin.interfaces.PropertyListType;

/**
 *
 * @author Jože Rihtaršič
 */
@ApplicationScoped
@ManagedBean(name = "LookupsData")
public class LookupsData extends AbstractJSFView {

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPlgManager;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  private PModeInterface mPMode;
  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  private SEDCertStoreInterface mdbCertStore;

  /**
   *
   * @return
   */
  public List<String> getListValuesForPluginListType(String listType) {
    List<String> lstArr = new ArrayList<>();
    if (Utils.isEmptyString(listType)) {
      return Collections.emptyList();
    } else if (listType.equalsIgnoreCase(PropertyListType.LocalBoxes.getType())) {
      List<SEDBox> sblst = mdbLookups.getSEDBoxes();
      sblst.forEach(sb -> {
        lstArr.add(sb.getLocalBoxName());
      });
    } else if (listType.
            equalsIgnoreCase(PropertyListType.KeystoreCertAll.getType())) {
      lstArr.addAll(mdbCertStore.getKeystoreAliases(false));
    } else if (listType.equalsIgnoreCase(PropertyListType.KeystoreCertKeys.
            getType())) {
      lstArr.addAll(mdbCertStore.getKeystoreAliases(true));

    } else if (listType.
            equalsIgnoreCase(PropertyListType.InMailStatus.getType())) {
      for (SEDInboxMailStatus st : SEDInboxMailStatus.values()) {
        lstArr.add(st.getValue());

      }
    } else if (listType.equalsIgnoreCase(PropertyListType.OutMailStatus.
            getType())) {
      for (SEDOutboxMailStatus st : SEDOutboxMailStatus.values()) {
        lstArr.add(st.getValue());

      }
    }
    return lstArr;
  }

  public List<String> getMailProperties() {
    return ReflectUtils.getBeanProperties(MSHMailType.class);

  }


  public SEDRulePredicate[] getRulePredicates() {
    return SEDRulePredicate.values();
  }
   public SEDInterceptorRole[] getInterceptRoles() {
    return SEDInterceptorRole.values();
  }
    public SEDInterceptorEvent[] getInterceptEvents() {
    return SEDInterceptorEvent.values();
  }
    
     public SEDOutboxMailStatus[] getOutMailStatuses() {
    return SEDOutboxMailStatus.values();
  }
        public SEDInboxMailStatus[] getInMailStatuses() {
    return SEDInboxMailStatus.values();
  }


}

/*
 * Copyright 2015, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.commons.pmode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.JAXBException;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.AgreementRef;
import si.laurentius.msh.pmode.MSHSetings;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.PartyIdentitySetType;
import si.laurentius.msh.pmode.ReceptionAwareness;
import si.laurentius.msh.pmode.Security;
import si.laurentius.msh.pmode.Service;
import si.laurentius.commons.PModeConstants;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import java.util.Collections;
import static java.lang.String.format;
import si.laurentius.commons.cxf.EBMSConstants;
import static si.laurentius.commons.pmode.FilePModeManager.LOG;
import si.laurentius.commons.pmode.enums.ActionRole;
import static si.laurentius.commons.utils.xml.XMLUtils.deserialize;
import static si.laurentius.commons.utils.xml.XMLUtils.serialize;
import si.laurentius.msh.pmode.Action;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class FilePModeManager implements PModeInterface {

    /**
     *
     */
    public static final SEDLogger LOG = new SEDLogger(FilePModeManager.class);

    /* List<PMode> mlstPModes = new ArrayList<>();
  Map<String, PartyIdentitySet> mmpPartyIdentites = new HashMap<>();
  Map<String, ReceptionAwareness> mmpReceptionAwareness = new HashMap<>();
  Map<String, Security> mmpSecurity = new HashMap<>();
  Map<String, Service> mmpServiceDef = new HashMap<>();*/
    MSHSetings mshSettings = null;
    private long mFileLastModifiedDate = 0;

    public FilePModeManager() {
    }

    public FilePModeManager(InputStream is) {
        reload(is);
    }

    /**
     *
     * @param i
     * @param pmrNew
     */
    public void addPMode(int i, PMode pmrNew) {
        for (PMode pm : getPModes()) {
            if (Objects.equals(pm.getId(), pmrNew.getId())) {
                throw new IllegalArgumentException(String.format(
                        "PMode with Id '%s' already exists!", pmrNew.getId()));
            }
        }
        if (i >= mshSettings.getPModes().size()) {
            mshSettings.getPModes().add(pmrNew);
        } else {
            mshSettings.getPModes().add(i, pmrNew);
        }
    }

    @Override
    public void addPMode(PMode val) {
        addPMode(getPModes().size(), val);

    }

    @Override
    public void addPartyIdentitySet(PartyIdentitySet val) {
        if (partyIdentitySetExists(val.getId())) {
            throw new IllegalArgumentException(String.format(
                    "PartyIdentitySet with Id '%s' already exists!", val.getId()));
        }

        if (mshSettings.getParties() == null) {
            mshSettings.setParties(new MSHSetings.Parties());
        }
        mshSettings.getParties().getPartyIdentitySets().add(val);
        saveMSHSettings();

    }

    @Override
    public void addReceptionAwareness(ReceptionAwareness val) {

        if (getReceptionAwarenessById(val.getId()) != null) {
            throw new IllegalArgumentException(String.format(
                    "ReceptionAwareness with Id '%s' already exists!", val.getId()));
        }
        if (mshSettings.getReceptionAwarenessPatterns() == null) {
            mshSettings.setReceptionAwarenessPatterns(
                    new MSHSetings.ReceptionAwarenessPatterns());
        }
        mshSettings.getReceptionAwarenessPatterns().getReceptionAwarenesses().add(
                val);
        saveMSHSettings();
    }

    @Override
    public void addSecurity(Security val) {
        if (getSecurityById(val.getId()) != null) {
            throw new IllegalArgumentException(String.format(
                    "Security with Id '%s' already exists!", val.getId()));
        }

        if (mshSettings.getSecurityPolicies() == null) {
            mshSettings.setSecurityPolicies(new MSHSetings.SecurityPolicies());
        }
        mshSettings.getSecurityPolicies().getSecurities().add(val);
        saveMSHSettings();
    }

    @Override
    public void addService(Service val) {
        if (getServiceById(val.getId()) != null) {
            throw new IllegalArgumentException(String.format(
                    "Service with Id '%s' already exists!", val.getId()));
        }
        if (mshSettings.getServices() == null) {
            mshSettings.setServices(new MSHSetings.Services());
        }
        mshSettings.getServices().getServices().add(val);
        saveMSHSettings();
    }

    @Override
    public EBMSMessageContext createMessageContextForOutMail(MSHOutMail mail)
            throws PModeException {
        EBMSMessageContext emc = new EBMSMessageContext();
        // retrieve data
        PartyIdentitySet sPID = getPartyIdentitySetForSEDAddress(mail.
                getSenderEBox());
        if (!sPID.isIsLocalIdentity()) {
            String msg = String.format(
                    "Sender '%s' (identityId '%s') for mail '%d' is not local identity and can not send messages!",
                    mail.getSenderEBox(), sPID.getId(), mail.getId());
            LOG.logWarn(msg, null);
            throw new PModeException(msg);
        }

        PartyIdentitySet rPID = getPartyIdentitySetForSEDAddress(mail.
                getReceiverEBox());
        Service srv = getServiceById(mail.getService());
        Action act = PModeUtils.getActionFromService(mail.getAction(), srv);
        //receiving role
        String sendingRole = Objects.equals(act.getInvokeRole(),
                ActionRole.Executor.getValue())
                ? srv.getExecutor().getRole() : srv.getInitiator().getRole();

        String recRole = Objects.equals(act.getInvokeRole(), ActionRole.Executor.
                getValue())
                        ? srv.getInitiator().getRole() : srv.getExecutor().getRole();

        PMode pMode = getPModeForLocalPartyAsSender(sPID.getId(), sendingRole,
                rPID.getId(),
                mail.getService());

        AgreementRef ar = PModeUtils.getAgreementRefForExchangePartyId(rPID.getId(),
                pMode);
        // fill transprot type, transportchanneltype
        PModeUtils.fillTransportMEPForAction(emc, act.getName(), pMode);
        //  get security
        Security security = null;
        if (!Utils.isEmptyString(emc.getTransportChannelType().getSecurityIdRef())) {
            security = getSecurityById(emc.getTransportChannelType().
                    getSecurityIdRef());
        }
        // set getReceptionAwareness
        ReceptionAwareness ra = null;
        if (emc.getTransportChannelType().getReceptionAwareness() != null
                && !Utils.isEmptyString(
                        emc.getTransportChannelType().getReceptionAwareness().
                                getRaPatternIdRef())) {
            ra = getReceptionAwarenessById(
                    emc.getTransportChannelType().getReceptionAwareness().
                            getRaPatternIdRef());
        }

        // get transport data for push!
        PartyIdentitySetType.TransportProtocol transport = null;
        if (emc.isPushTransfrer()) {
            String transportId = null;
            for (PMode.ExchangeParties.PartyInfo epi : pMode.getExchangeParties().
                    getPartyInfos()) {
                // get def transport id
                if (Objects.equals(epi.getPartyIdentitySetIdRef(), rPID.getId())) {
                    transportId = epi.getPartyDefTransportIdRef();
                    break;
                }
            }
            if (transportId == null) {
                if (rPID.getTransportProtocols().size() > 0) {
                    transport = rPID.getTransportProtocols().get(0);
                    LOG.formatedWarning(
                            "PMode: '%s' does not have defined transport for exchange party '%s'. First transport '%s' will be used!",
                            pMode.getId(), rPID.getId(), transport.getId());
                } else {
                    throw new PModeException(
                            String.format(
                                    "PMode: '%s' does not have defined transport for exchange party '%s'. Action %s in MEP is pushed!  ",
                                    pMode.getId(), rPID.getId(), act.getName()));

                }
            } else {
                for (PartyIdentitySetType.TransportProtocol tp : rPID.
                        getTransportProtocols()) {
                    if (Objects.equals(tp.getId(), transportId)) {
                        transport = tp;
                        break;
                    }
                }
            }
        }

        // set context
        emc.setAction(act);
        emc.setExchangeAgreementRef(ar);
        emc.setPMode(pMode);
        emc.setReceiverPartyIdentitySet(rPID);
        emc.setSenderPartyIdentitySet(sPID);
        emc.setSecurity(security);
        emc.setSendingRole(sendingRole);
        emc.setReceivingRole(recRole);
        emc.setService(srv);
        emc.setTransportProtocol(transport);
        emc.setReceptionAwareness(ra);

        return emc;
    }

    /**
     * Method returs domain by domain and service
     *
     * @param agrRef aggrement ref
     * @param agrRefType aggrement type
     * @param agrPMode
     * @return
     * @throws PModeException
     */
    @Override
    public PMode getByAgreementRef(String agrRef, String agrRefType,
            String agrPMode)
            throws PModeException {

        for (PMode pm : mshSettings.getPModes()) {
            for (PMode.ExchangeParties.PartyInfo pi : pm.getExchangeParties().
                    getPartyInfos()) {
                if (pi.getAgreementRef() != null && pi.getAgreementRef().getValue().
                        equals(agrRef)) {
                    return pm;
                }

            }
        }
        throw new PModeException(String.format(
                "AgreementRef for value: '%s', type: '%s' pmode: '%s' not exist.",
                agrRef, agrRefType,
                agrPMode));

    }

    /**
     *
     * @param pModeId
     * @return
     */
    @Override
    public PMode getPModeById(String pModeId) {

        for (PMode pm : getPModes()) {
            if (Objects.equals(pm.getId(), pModeId)) {
                return pm;
            }
        }
        return null;
    }

    @Override
    public PMode getPModeMSHOutMail(MSHOutMail mail) throws PModeException {

        PartyIdentitySet sPID = getPartyIdentitySetForSEDAddress(mail.
                getSenderEBox());
        if (!sPID.isIsLocalIdentity()) {
            String msg = String.format(
                    "Sender '%s' (identityId '%s') for mail '%d' is not local identity and can not send messages!",
                    mail.getSenderEBox(), sPID.getId(), mail.getId());
            LOG.logWarn(msg, null);
            throw new PModeException(msg);
        }

        PartyIdentitySet rPID = getPartyIdentitySetForSEDAddress(mail.
                getReceiverEBox());
        Service srv = getServiceById(mail.getService());
        Action act = PModeUtils.getActionFromService(mail.getAction(), srv);
        //receiving role
        String sendingRole = Objects.equals(act.getInvokeRole(),
                ActionRole.Executor.getValue())
                ? srv.getExecutor().getRole() : srv.getInitiator().getRole();

        return getPModeForLocalPartyAsSender(sPID.getId(), sendingRole,
                rPID.getId(),
                mail.getService());

    }

    ;

  /**
   *
   * @return
   */
  /**
   * Method returs PMODE for given exchange sender Party. If pmode not exists or
   * more than one PMode is defined for given parameters PModeException is
   * thrown.
   *
   * @param senderRefId - exchange sender party identity set id
   * @param actionSendingRole - bussines role sender must have in current action
   * @param receiverRefId - local receiver party idetity set id
   * @param serviceId - bussines service
   * @return PMode
   * @throws PModeException
   */
  @Override
    public PMode getPModeForExchangePartyAsSender(String senderRefId,
            String actionSendingRole,
            String receiverRefId, String serviceId)
            throws PModeException {

        List<PMode> lstResult = new ArrayList<>();
        for (PMode pm : getPModes()) {
            // check if service match
            if (pm.getServiceIdRef() == null
                    || !Objects.equals(pm.getServiceIdRef(), serviceId)) {
                continue;
            }
            // check if local party match as receiver
            if (pm.getLocalPartyInfo() == null
                    || !Objects.equals(pm.getLocalPartyInfo().
                            getPartyIdentitySetIdRef(), receiverRefId)) {
                continue;
            }
            // check if exchange party list is not null
            if (pm.getExchangeParties() == null || pm.getExchangeParties().
                    getPartyInfos().isEmpty()) {
                continue;
            }

            // check if exchange party match
            boolean epmatch = false;
            for (PMode.ExchangeParties.PartyInfo ep : pm.getExchangeParties().
                    getPartyInfos()) {
                if (Objects.equals(ep.getPartyIdentitySetIdRef(), senderRefId) // if role match if role is null all roles are ok.
                        && (Utils.isEmptyString(actionSendingRole)
                        || ep.getRoles().contains(actionSendingRole))) {
                    epmatch = true;
                    break;
                }
            }
            if (epmatch) {
                lstResult.add(pm);
            }

        }

        if (lstResult.size() > 1) {
            throw new PModeException(String.format(
                    "more than one PMODE for exchange"
                    + " senderRefId '%s',actionSendingRole '%s',  receiverRefId '%s', serviceRefId: '%s' ",
                    senderRefId, actionSendingRole, receiverRefId, serviceId));
        } else if (lstResult.isEmpty()) {
            throw new PModeException(String.format("No PMODE for exchange"
                    + " senderRefId '%s',actionSendingRole '%s',  receiverRefId '%s', serviceRefId: '%s' ",
                    senderRefId, actionSendingRole, receiverRefId, serviceId));
        }
        return lstResult.get(0);
    }

    /**
     * Method returs PMODE for given local sender Party. If pmode not exists or
     * more than one PMode is defined for given parameters PModeException is
     * thrown.
     *
     * @param senderRefId - local sender party identity set id
     * @param actionSendingRole - bussines role sender must have in current
     * action
     * @param receiverRefId - exchange receiver party idetity set id
     * @param serviceId - bussines service
     * @return PMode
     * @throws PModeException
     */
    @Override
    public PMode getPModeForLocalPartyAsSender(String senderRefId,
            String actionSendingRole,
            String receiverRefId, String serviceId)
            throws PModeException {

        List<PMode> lstResult = new ArrayList<>();
        for (PMode pm : getPModes()) {
            // check if service match
            if (pm.getServiceIdRef() == null
                    || !Objects.equals(pm.getServiceIdRef(), serviceId)) {
                continue;
            }
            // check if local party match
            if (pm.getLocalPartyInfo() == null
                    || !Objects.equals(pm.getLocalPartyInfo().
                            getPartyIdentitySetIdRef(), senderRefId)) {
                continue;
            }
            // check if sending party contains sending role for action
            if (!Utils.isEmptyString(actionSendingRole)
                    && !pm.getLocalPartyInfo().getRoles().contains(actionSendingRole)) {
                continue;

            }
            // check if exchange party list is not null
            if (pm.getExchangeParties() == null || pm.getExchangeParties().
                    getPartyInfos().isEmpty()) {
                continue;
            }
            // check if exchange party match
            boolean epmatch = false;
            for (PMode.ExchangeParties.PartyInfo ep : pm.getExchangeParties().
                    getPartyInfos()) {
                if (Objects.equals(ep.getPartyIdentitySetIdRef(), receiverRefId)) {
                    epmatch = true;
                    break;
                }
            }
            if (!epmatch) {
                continue;
            }
            lstResult.add(pm);
        }

        if (lstResult.size() > 1) {
            throw new PModeException(String.format("more than one PMODE for local"
                    + " senderRefId '%s',actionSendingRole '%s',  receiverRefId '%s', serviceRefId: '%s' ",
                    senderRefId, actionSendingRole, receiverRefId, serviceId));
        } else if (lstResult.isEmpty()) {
            throw new PModeException(String.format("No PMODE for local"
                    + " senderRefId '%s',actionSendingRole '%s',  receiverRefId '%s', serviceRefId: '%s' ",
                    senderRefId, actionSendingRole, receiverRefId, serviceId));
        }
        return lstResult.get(0);
    }

    /**
     *
     * @return @throws PModeException
     */
    public MSHSetings getMSHSettings()
            throws PModeException {
        FilePModeManager.this.reload();
        return mshSettings;
    }

    @Override
    public List<PMode> getPModes() {
        FilePModeManager.this.reload();
        return mshSettings.getPModes();
    }

    @Override
    public PartyIdentitySet getPartyIdentitySetById(String id) {
        for (PartyIdentitySet pm : getPartyIdentitySets()) {
            if (Objects.equals(pm.getId(), id)) {
                return pm;
            }
        }
        LOG.formatedWarning("No PartyIdentitySet for id: '%s'.",
                id);
        return null;
    }

    public boolean partyIdentitySetExists(String id) {
        for (PartyIdentitySet pm : getPartyIdentitySets()) {
            if (Objects.equals(pm.getId(), id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method returs PartyIdentitySet for Authorization. Method is used for pull
     * signal
     *
     * @param username
     * @param password
     * @return first PartyIdentitySet or null if not found
     */
    public PartyIdentitySet getPartyIdentitySetForAuthorization(String username,
            String password) {

        for (PartyIdentitySet pis : getPartyIdentitySets()) {
            if (pis.getAuthorization() != null && username != null && password != null
                    && Objects.equals(pis.getAuthorization().getUsername(), username)
                    && Objects.equals(pis.getAuthorization().getPassword(), password)) {
                return pis;
            }
        }
        return null;
    }

    /**
     * Method returns PartyIdentitySet for partyId and type. Method returns
     * PartyIdentitySet if one of partyID has matches with given type and
     * partyIdValue ebmsMessage
     * <ns2:PartyId type="urn:oasis:names:tc:ebcore:partyid-type:unregistered">test</ns2:PartyId>
     *
     * @param partyType - type of party -
     * urn:oasis:names:tc:ebcore:partyid-type:unregistered
     * @param partyIdValue - partyId value: test
     * @return first PartyIdentitySet or null if not found
     */
    @Override
    public PartyIdentitySet getPartyIdentitySetForPartyId(String partyType,
            String partyIdValue)
            throws PModeException {

        if (Utils.isEmptyString(partyIdValue)) {
            LOG.logWarn("Empty partyIdValue", null);
            return null;
        }
        List<PartyIdentitySet> candidates = new ArrayList<>();
        for (PartyIdentitySet pis : getPartyIdentitySets()) {

            if (pis.isUseFourCornerModel()!=null && pis.isUseFourCornerModel()) {
                //check if partyType match
                if (!Objects.equals(EBMSConstants.EBMS_ECORE_PARTY_TYPE_UNREGISTERED, partyType)) {
                    continue;
                }

                // domain is case insensitive
                String domain = partyIdValue.toLowerCase();

                if (pis.isIsLocalIdentity()
                        ? domain.endsWith(getLocalDomain().toLowerCase())
                        : domain.endsWith(pis.getDomain().toLowerCase())) {
                    candidates.add(pis);
                }
                // fou
                continue;
            } else {

                for (PartyIdentitySet.PartyId pid : pis.getPartyIds()) {

                    //check if partyType match
                    if (!Objects.equals(partyType, pid.getType())) {
                        continue;
                    }
                    String srcType = pid.getValueSource();
                    // if pid has source IGNORE -than is not intended for submiting over ebms.
                    if (srcType.equals(PModeConstants.PARTY_ID_SOURCE_TYPE_IGNORE)) {
                        continue;
                    }
                    // check if ID "fixed value" than this is target identiy set
                    if (!Utils.isEmptyString(pid.getFixValue())
                            && Objects.equals(pid.getFixValue(), partyIdValue)) {
                        return pis;
                    }

                    // check if ID from list of identifiers
                    if (srcType.equals(PModeConstants.PARTY_ID_SOURCE_TYPE_IDENTIFIER)
                            && pid.getIdentifiers().contains(partyIdValue)) {
                        return pis;

                    }
                    // check if ID is SED 
                    if (srcType.equals(PModeConstants.PARTY_ID_SOURCE_TYPE_ADDRESS)
                            && partyIdValue.contains("@")) {

                        String domain = partyIdValue.substring(partyIdValue.indexOf("@") + 1);
                        // domain is case insensitive
                        domain = domain.toLowerCase();

                        if (pis.isIsLocalIdentity()
                                ? domain.endsWith(getLocalDomain().toLowerCase())
                                : domain.endsWith(pis.getDomain().toLowerCase())) {
                            candidates.add(pis);
                        }
                    }
                    // ignore other sources
                }
            }

        }

        if (candidates.size() > 1) {
            throw new PModeException(String.format(
                    "More than one (%d) for partyType '%s' and value %s!",
                    candidates.size(), partyType, partyIdValue));
        }
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    @Override
    public PartyIdentitySet getPartyIdentitySetForSEDAddress(String address)
            throws PModeException {
        if (Utils.isEmptyString(address) || !address.contains("@")) {
            throw new PModeException(String.format("SED Address must be "
                    + "composed with [localpart]@[domain]. Address '%s'", address));
        }

        String[] addrTb = address.split("@");
        String localPart = addrTb[0];
        String domainPart = addrTb[1].toLowerCase();

        String localDomain = getLocalDomain().toLowerCase();

        if (Utils.isEmptyString(localDomain)) {
            throw new PModeException(
                    "Bad aplication configuratin. Missing domain parameter");
        }

        int iDomainCount = 0;
        List<PartyIdentitySet> candidates = new ArrayList<>();
        for (PartyIdentitySet pis : getPartyIdentitySets()) {
            // check domain
            if (pis.isIsLocalIdentity()
                    ? domainPart.endsWith(localDomain)
                    : domainPart.endsWith(pis.getDomain().toLowerCase())) {
                iDomainCount++;
                boolean bContaisIdetifierId = false;
                for (PartyIdentitySetType.PartyId pi : pis.getPartyIds()) {
                    if (pi.getValueSource().equals(
                            PModeConstants.PARTY_ID_SOURCE_TYPE_IDENTIFIER)) {
                        bContaisIdetifierId = true;
                        // if contains with identifier return this Etity-set
                        if (pi.getIdentifiers().contains(localPart)) {
                            return pis;
                        }
                    }
                }
                // add only identifier  with no idetifiers
                if (!bContaisIdetifierId) {
                    candidates.add(pis);
                }
            }
        }
        if (candidates.isEmpty()) {
            throw new PModeException(String.format(
                    "No PartyIdentitySet for address '%s'. Count identitySets for domain %d with no matchin identifiers!",
                    address, iDomainCount));
        }

        if (candidates.size() > 1) {
            throw new PModeException(String.format(
                    "More than one (%d) PartyIdentitySet found for address '%s'!",
                    iDomainCount, address));
        }
        return candidates.get(0);
    }

    @Override
    public List<PartyIdentitySet> getPartyIdentitySets() {
        if (mshSettings == null) {
            reload();
        }
        return mshSettings != null && mshSettings.getParties() != null
                ? mshSettings.getParties().getPartyIdentitySets()
                : Collections.emptyList();
    }

    @Override
    public ReceptionAwareness getReceptionAwarenessById(String id) {
        for (ReceptionAwareness pm : getReceptionAwarenesses()) {
            if (Objects.equals(pm.getId(), id)) {
                return pm;
            }
        }
        LOG.formatedWarning("No ReceptionAwareness for id: '%s'.", id);
        return null;
    }

    @Override
    public List<ReceptionAwareness> getReceptionAwarenesses() {
        if (mshSettings == null) {
            reload();
        }
        return mshSettings != null && mshSettings.getReceptionAwarenessPatterns() != null
                ? mshSettings.getReceptionAwarenessPatterns().
                        getReceptionAwarenesses()
                : Collections.emptyList();
    }

    @Override
    public List<Security> getSecurities() {
        if (mshSettings == null) {
            reload();
        }
        return mshSettings != null && mshSettings.getSecurityPolicies() != null
                ? mshSettings.getSecurityPolicies().getSecurities()
                : Collections.emptyList();
    }

    @Override
    public Security getSecurityById(String securityId) {

        for (Security pm : getSecurities()) {
            if (Objects.equals(pm.getId(), securityId)) {
                return pm;
            }
        }
        LOG.formatedWarning("No SecurityPolicy for id: '%s'.",
                securityId);
        return null;
    }

    @Override
    public Service getServiceById(String serviceId) {

        for (Service pm : getServices()) {
            if (Objects.equals(pm.getId(), serviceId)) {
                return pm;
            }
        }
        LOG.formatedWarning("No Service for id: '%s'.", serviceId);
        return null;

    }

    @Override
    public Service getServiceByNameAndTypeAndAction(String serviceName,
            String serviceType,
            String action)
            throws PModeException {

        for (Service srv : getServices()) {
            if (!Objects.equals(srv.getServiceName(), serviceName)
                    || !(Utils.equalsEmptyString(srv.getServiceType(), serviceType))) {
                continue;
            }

            for (Action act : srv.getActions()) {
                if (Objects.equals(act.getName(), action)) {
                    return srv;
                }
            }
        }

        throw new PModeException(String.format(
                "Service with name '%s', serviceType: '%s' and action '%s' not exists!",
                serviceName,
                serviceType, action));

    }

    @Override
    public List<Service> getServices() {
        if (mshSettings == null) {
            reload();
        }
        return mshSettings != null && mshSettings.getServices() != null
                ? mshSettings.getServices().getServices()
                : Collections.emptyList();
    }

    /**
     *
     * @throws PModeException
     */
    public void reload() {
        long l = LOG.logStart();
        File pModeFile = SEDSystemProperties.getPModeFile();
        if (pModeFile.lastModified() > mFileLastModifiedDate) {
            try (FileInputStream fis = new FileInputStream(pModeFile)) {
                reload(fis);
                mFileLastModifiedDate = pModeFile.lastModified();
            } catch (IOException ex) {
                String msg = "Error init PModes from file '" + pModeFile.
                        getAbsolutePath() + "'";
                throw new RuntimeException(msg, ex);
            }
        }
        LOG.logEnd(l);
    }

    /**
     * Reload pmodes from input stream. If parse error occurred PModeException
     * is thrown
     *
     * @param is
     * @throws PModeException
     */
    public final void reload(InputStream is) {
        long l = LOG.logStart();
        try {
            mshSettings = (MSHSetings) deserialize(is, MSHSetings.class);
        } catch (JAXBException ex) {
            String msg = "Error init MSH Settings!";
            throw new RuntimeException(msg, ex);
        }
        LOG.logEnd(l);
    }

    /**
     *
     * @param pModeId
     * @return
     */
    public PMode removePModeById(String pModeId) {
        PMode removed = null;
        for (PMode pm : mshSettings.getPModes()) {
            if (pm.getId() != null && pm.getId().equals(pModeId)) {
                mshSettings.getPModes().remove(pm);
                removed = pm;
                break;
            }
        }
        saveMSHSettings();
        return removed;
    }

    @Override
    public void removePMode(PMode val) {
        mshSettings.getPModes().remove(val);
        saveMSHSettings();
    }

    @Override
    public void removePartyIdentitySet(PartyIdentitySet val) {
        if (mshSettings.getParties() != null) {
            mshSettings.getParties().getPartyIdentitySets().remove(val);
            saveMSHSettings();
        } else {
            LOG.logWarn("No parties defined in msh settings", null);
        }
        saveMSHSettings();
    }

    @Override
    public void removeReceptionAwareness(ReceptionAwareness val) {

        if (mshSettings.getReceptionAwarenessPatterns() != null) {
            mshSettings.getReceptionAwarenessPatterns().getReceptionAwarenesses().
                    remove(val);
            saveMSHSettings();
        } else {
            LOG.logWarn("No reception awareness defined in msh settings", null);
        }
        saveMSHSettings();
    }

    @Override
    public void removeSecurity(Security val) {
        if (mshSettings.getSecurityPolicies() != null) {
            mshSettings.getSecurityPolicies().getSecurities().remove(val);
            saveMSHSettings();
        } else {
            LOG.logWarn("No securityPolicies defined in msh settings", null);
        }
        saveMSHSettings();

    }

    @Override
    public void removeService(Service val) {
        if (mshSettings.getServices() != null) {
            mshSettings.getServices().getServices().remove(val);
            saveMSHSettings();
        } else {
            LOG.logWarn("No services defined in msh settings", null);
        }
        saveMSHSettings();
    }

    /**
     *
     *
     */
    public void saveMSHSettings() {
        long l = LOG.logStart();
        try {

            File pModeFile = SEDSystemProperties.getPModeFile();
            int i = 1;
            String fileFormat = pModeFile.getAbsolutePath() + ".%03d";
            File pModeFileTarget = new File(format(fileFormat, i++));

            while (pModeFileTarget.exists()) {
                pModeFileTarget = new File(format(fileFormat, i++));
            }

            move(pModeFile.toPath(), pModeFileTarget.toPath(), REPLACE_EXISTING);

            try (PrintWriter out = new PrintWriter(pModeFile)) {
                serialize(mshSettings, out);
            } catch (JAXBException | FileNotFoundException ex) {
                String msg = "ERROR serialize PMODE: " + ex.getMessage();
                throw new RuntimeException(msg, ex);
            }

        } catch (IOException ex) {
            String msg = "ERROR saving file: " + ex.getMessage();
            throw new RuntimeException(msg, ex);
        }
        LOG.logEnd(l);
    }

    public String getLocalDomain() {
        return System.getProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN);
    }

    @Override
    public void updatePMode(PMode val) {
        boolean suc = false;
        for (int index = 0; index < mshSettings.getPModes().size(); index++) {
            PMode pm = mshSettings.getPModes().get(index);
            if (pm.getId() != null && Objects.equals(pm.getId(), val.getId())) {
                mshSettings.getPModes().remove(pm);
                mshSettings.getPModes().add(index, val);
                suc = true;
                break;
            }
        }
        if (!suc) {
            LOG.formatedWarning("No PMode with id  %s. PMode was added as new PMode",
                    val.getId());
            mshSettings.getPModes().add(val);
        }
        saveMSHSettings();
    }

    @Override
    public void updatePartyIdentitySet(PartyIdentitySet val) {

        if (mshSettings.getParties() == null) {
            mshSettings.setParties(new MSHSetings.Parties());
        }

        boolean suc = false;
        for (int index = 0; index < mshSettings.getParties().getPartyIdentitySets().
                size(); index++) {
            PartyIdentitySet pm = mshSettings.getParties().getPartyIdentitySets().get(
                    index);

            if (pm.getId() != null && Objects.equals(pm.getId(), val.getId())) {
                mshSettings.getParties().getPartyIdentitySets().remove(pm);
                mshSettings.getParties().getPartyIdentitySets().add(index, val);
                suc = true;
                break;
            }
        }
        if (!suc) {
            mshSettings.getParties().getPartyIdentitySets().add(val);
            LOG.formatedWarning(
                    "No PartyIdentitySet with id  %s. PartyIdentitySet was added as new PartyIdentitySet",
                    val.getId());
        }
        saveMSHSettings();
    }

    @Override
    public void updateReceptionAwareness(ReceptionAwareness val) {

        if (mshSettings.getReceptionAwarenessPatterns() == null) {
            mshSettings.setReceptionAwarenessPatterns(
                    new MSHSetings.ReceptionAwarenessPatterns());
        }

        boolean suc = false;
        for (int index = 0; index
                < mshSettings.getReceptionAwarenessPatterns().
                        getReceptionAwarenesses().size(); index++) {
            ReceptionAwareness pm
                    = mshSettings.getReceptionAwarenessPatterns().
                            getReceptionAwarenesses().get(index);

            if (pm.getId() != null && Objects.equals(pm.getId(), val.getId())) {
                mshSettings.getReceptionAwarenessPatterns().getReceptionAwarenesses().
                        remove(pm);
                mshSettings.getReceptionAwarenessPatterns().getReceptionAwarenesses().
                        add(index, val);
                suc = true;
                break;
            }
        }
        if (!suc) {
            mshSettings.getReceptionAwarenessPatterns().getReceptionAwarenesses().add(
                    val);
            LOG.formatedWarning(
                    "No ReceptionAwareness with id  %s. ReceptionAwareness was added as new ReceptionAwareness",
                    val.getId());
        }
        saveMSHSettings();
    }

    @Override
    public void updateSecurity(Security val) {

        if (mshSettings.getSecurityPolicies() == null) {
            mshSettings.setSecurityPolicies(new MSHSetings.SecurityPolicies());
        }

        boolean suc = false;
        for (int index = 0; index
                < mshSettings.getSecurityPolicies().getSecurities().size(); index++) {
            Security pm
                    = mshSettings.getSecurityPolicies().getSecurities().get(index);

            if (pm.getId() != null && Objects.equals(pm.getId(), val.getId())) {
                mshSettings.getSecurityPolicies().getSecurities().remove(pm);
                mshSettings.getSecurityPolicies().getSecurities().add(index, val);
                suc = true;
                break;
            }
        }
        if (!suc) {
            mshSettings.getSecurityPolicies().getSecurities().add(val);
            LOG.formatedWarning(
                    "No Security with id  %s. Security was added as new Security",
                    val.getId());
        }
        saveMSHSettings();
    }

    @Override
    public void updateService(Service val) {
        if (mshSettings.getServices() == null) {
            mshSettings.setServices(new MSHSetings.Services());
        }

        boolean suc = false;
        for (int index = 0; index
                < mshSettings.getServices().getServices().size(); index++) {
            Service pm
                    = mshSettings.getServices().getServices().get(index);

            if (pm.getId() != null && Objects.equals(pm.getId(), val.getId())) {
                mshSettings.getServices().getServices().remove(pm);
                mshSettings.getServices().getServices().add(index, val);
                suc = true;
                break;
            }
        }
        if (!suc) {
            mshSettings.getServices().getServices().add(val);
            LOG.formatedWarning(
                    "No Service with id  %s. Service was added as new Service",
                    val.getId());
        }
        saveMSHSettings();
    }
}

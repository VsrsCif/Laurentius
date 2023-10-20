/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.xml.sax.SAXException;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;
import si.vsrs.cif.laurentius.plugin.eodlozisce.sig.XMLSignatureUtils;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.FilingValidationStage;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.SchemaValidationStage;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.XmlSignatureValidationStage;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

@Stateless
@Local(TaskExecutionInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ECFEVIPTask implements TaskExecutionInterface {

    private static final SEDLogger LOG = new SEDLogger(ECFEVIPTask.class);
    public static final String KEY_PAYLOAD_METADATA_NAME = "ecf.payload.metadata.name";
    private static final String SIGN_ALIAS = "ecf.sign.key.alias";

    @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
    SEDDaoInterface mDB;

    @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
    SEDCertStoreInterface mCertBean;

    @EJB(mappedName = SEDJNDI.JNDI_PMODE)
    PModeInterface mpModeManager;

    SchemaValidationStage schemaValidator;
    XmlSignatureValidationStage xmlSignatureValidator;
    FilingValidationStage filingValidationStage;
    XMLSignatureUtils signatureUtils;

    @PostConstruct
    public void init() {
        try {
            this.signatureUtils = new XMLSignatureUtils();
            this.schemaValidator = new SchemaValidationStage();
            this.filingValidationStage = new FilingValidationStage();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String executeTask(Properties properties) throws TaskException {

        long l = LOG.logStart();
        StringWriter sw = new StringWriter();
        sw.append("Start eOdlozisce plugin task: \n");

        MSHInMail mi = new MSHInMail();
        mi.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
        mi.setService(EOdlozisceConstants.EODLOZISCE_SERVICE);
        mi.setAction(EOdlozisceConstants.EODLOZISCE_ACTION);
        // mi.setReceiverEBox(sedBox + "@" + SEDSystemProperties.getLocalDomain());

        List<MSHInMail> inMailList = mDB.getDataList(MSHInMail.class, -1, 100, "Id", "ASC", mi);

        String metadataAttachmentName = properties.getProperty(KEY_PAYLOAD_METADATA_NAME);

        sw.append("End ecf plugin task");
        return sw.toString();
    }

    @Override
    public CronTaskDef getDefinition() {

        CronTaskDef tt = new CronTaskDef();
        tt.setType("ecf-evip-task");
        tt.setName("ECF EVIP task");
        tt.setDescription("Task for the EVIP integration");
        return tt;
    }
}

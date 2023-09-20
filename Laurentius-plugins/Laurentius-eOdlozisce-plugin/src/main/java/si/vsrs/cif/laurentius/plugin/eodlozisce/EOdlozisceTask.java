/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.xml.sax.SAXException;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.PropertyListType;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;
import si.vsrs.cif.laurentius.plugin.eodlozisce.exception.EOdlozisceException;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.SchemaValidator;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationOutput;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationResult;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Stateless
@Local(TaskExecutionInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class EOdlozisceTask implements TaskExecutionInterface {

  private static final SEDLogger LOG = new SEDLogger(EOdlozisceTask.class);
  private static final String SIGN_ALIAS = "zkp.sign.key.alias";
  private static final String REC_SEDBOX = "zkp.sedbox";
  private static final String PROCESS_MAIL_COUNT = "zkp.max.mail.count";

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertBean;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mpModeManager;

  SchemaValidator xmlValidator;

  @PostConstruct
  public void init() {
    try {
      this.xmlValidator = new SchemaValidator(EOdlozisceTask.class.getResourceAsStream("/schemas/SkupnoElementi.xsd"));
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String executeTask(Properties p)
          throws TaskException {

    long l = LOG.logStart();
    StringWriter sw = new StringWriter();
    sw.append("Start zkp plugin task: \n");

    MSHInMail mi = new MSHInMail();
    mi.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
    mi.setService(EOdlozisceConstants.EODLOZISCE_SERVICE);
    mi.setAction(EOdlozisceConstants.EODLOZISCE_ACTION);
    // mi.setReceiverEBox(sedBox + "@" + SEDSystemProperties.getLocalDomain());

    List<MSHInMail> lst = mDB.getDataList(MSHInMail.class, -1, 100, "Id", "ASC", mi);

    // set status to proccess
    lst.stream().forEach((m) -> {
      try {

        m.getMSHInPayload().getMSHInParts().stream().forEach((part) -> {
          try {

            ValidationResult validationResult = this.xmlValidator.validate(Files.newInputStream(Paths.get(part.getFilepath())));
            // TODO: generate report of validation errors
            if(validationResult.getValidationOutputs().stream().anyMatch((o) -> o.getSeverity().equals(ValidationOutput.ValidateionSeverity.ERROR))) {
              mDB.setStatusToInMail(m, SEDInboxMailStatus.ERROR, "Add message to zkp deliver proccess");
            }
          } catch (Exception ex) {
            LOG.logError(l, "Error decoding payload", ex);
          }
        });

        mDB.setStatusToInMail(m, SEDInboxMailStatus.PROCESS,
                "Add message to zkp deliver proccess");
      } catch (StorageException ex) {
        String msg = String.format(
                "Error occurred processing mail: '%s'. Err: %s.", m.getId(),
                ex.getMessage());
        LOG.logError(l, msg, ex);
        sw.append(msg);
      }
    });

    sw.append("End zkp plugin task");
    return sw.toString();
  }

  @Override
  public CronTaskDef getDefinition() {

    CronTaskDef tt = new CronTaskDef();
    tt.setType("eodlozisce-validation");
    tt.setName("Validate eOdlozisce package");
    tt.setDescription("Validate and forward eVlozisce package.");
    tt.getCronTaskPropertyDeves().add(createTTProperty(REC_SEDBOX,
            "Receiver sedbox (without domain).", true, PropertyType.List.
                    getType(), null, PropertyListType.LocalBoxes.getType()));
    tt.getCronTaskPropertyDeves().add(createTTProperty(SIGN_ALIAS,
            "Signature key alias defined in keystore.", true, PropertyType.List.
                    getType(), null, PropertyListType.KeystoreCertKeys.getType()));
    tt.getCronTaskPropertyDeves().add(createTTProperty(PROCESS_MAIL_COUNT,
            "Max mail count proccesed.", true, PropertyType.Integer.getType(),
            null, null));
    return tt;
  }

  private CronTaskPropertyDef createTTProperty(String key, String desc,
          boolean mandatory,
          String type, String valFormat, String valList) {
    CronTaskPropertyDef ttp = new CronTaskPropertyDef();
    ttp.setKey(key);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
    return ttp;
  }
}

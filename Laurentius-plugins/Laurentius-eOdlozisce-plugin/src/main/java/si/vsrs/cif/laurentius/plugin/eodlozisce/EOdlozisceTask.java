/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.xml.sax.SAXException;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.cxf.EBMSConstants;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaOvojnica;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaPosiljkaTip;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskiDokumentSeznamTip;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskiDokumentTip;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskoOpraviloTip;
import si.sodisce.sheme.skupno.skupno.v1.SifrantTip;
import si.sodisce.sheme.skupno.skupno.v1.UdelezenecTip;
import si.sodisce.sheme.skupno.skupno.v1.ZadevaTip;
import si.sodisce.sheme.skupno.splosno.v1.LastnostSeznamTip;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.CourtType;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.FieldOfLawType;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.RegisterType;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.FilingValidationStage;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.SchemaValidationStage;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationOutput;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationResult;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.XmlSignatureValidationStage;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Stateless
@Local(TaskExecutionInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class EOdlozisceTask implements TaskExecutionInterface {

  private static final SEDLogger LOG = new SEDLogger(EOdlozisceTask.class);
  public static final String KEY_PAYLOAD_METADATA_NAME = "ecf.payload.metadata.name";

  public static Source[] schemas = new Source[]{
          new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/CivilniElementi.xsd").toExternalForm()),
          new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/CivilniSkupnoTipi.xsd").toExternalForm()),
          new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/KazenskiElementi.xsd").toExternalForm()),
          new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/KazenskiSkupnoTipi.xsd").toExternalForm()),
          new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/SkupnoElementi.xsd").toExternalForm()),
          new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/SkupnoIzmenjaveTipi.xsd").toExternalForm()),
          new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/SkupnoSkupnoTipi.xsd").toExternalForm()),
          new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/SkupnoSplosnoTipi.xsd").toExternalForm()),
          new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/XAdES-1.1.1.xsd").toExternalForm()),
          new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/xmldsig-core-schema.xsd").toExternalForm())
  };
  public List<CourtType> courtTypes;
  public List<RegisterType> registerTypes;
  public List<FieldOfLawType> fieldOfLawTypes;

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertBean;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mpModeManager;

  SchemaValidationStage xmlValidator;
  XmlSignatureValidationStage xmlSignatureValidator;
  FilingValidationStage filingValidationStage;

  @PostConstruct
  public void init() {
    try {

      this.xmlValidator = new SchemaValidationStage(EOdlozisceTask.class.getResourceAsStream("schemas/SkupnoElementi.xsd"));
      this.filingValidationStage = new FilingValidationStage();
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String executeTask(Properties p) throws TaskException {

    long l = LOG.logStart();
    StringWriter sw = new StringWriter();
    sw.append("Start zkp plugin task: \n");

    MSHInMail mi = new MSHInMail();
    mi.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
    mi.setService(EOdlozisceConstants.EODLOZISCE_SERVICE);
    mi.setAction(EOdlozisceConstants.EODLOZISCE_ACTION);
    // mi.setReceiverEBox(sedBox + "@" + SEDSystemProperties.getLocalDomain());

    List<MSHInMail> lst = mDB.getDataList(MSHInMail.class, -1, 100, "Id", "ASC", mi);

    String metadataAttachmetName = p.getProperty(KEY_PAYLOAD_METADATA_NAME);
    // set status to proccess
    lst.stream().forEach((m) -> {
      try {
        Optional<ValidationResult> validationResultOption = m.getMSHInPayload().getMSHInParts().stream().filter((part) -> metadataAttachmetName.equals(part.getName())).findFirst().map((part) ->
                {
                  try {
                    return this.xmlValidator.validate(Files.newInputStream(Paths.get(part.getFilepath())));
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                }
        );

        if (!validationResultOption.isPresent()) {
          actUponMissingOrInvalidMetadataXML(metadataAttachmetName, m);

        } else {
          if (validationResultOption.get().getValidationOutputs().stream().anyMatch((o) -> o.getSeverity().equals(ValidationOutput.ValidateionSeverity.ERROR))) {
            actUponMissingOrInvalidMetadataXML(metadataAttachmetName, m);
          }
        }

        // TODO: validate data inside XML
        // TODO: create and sign "bogus" XML - and attach (rename original XML to something)
        // TODO: generate report JSON file and attach


        Optional<ValidationResult> signatureValidationResultOption = m.getMSHInPayload().getMSHInParts().stream().filter((part) -> metadataAttachmetName.equals(part.getName())).findFirst().map((part) ->
                    this.xmlSignatureValidator.validate(Paths.get(part.getFilepath()))
        );

        // TODO: generate report of validation errors
        if(validationResult.getValidationOutputs().stream().anyMatch((o) -> o.getSeverity().equals(ValidationOutput.ValidateionSeverity.ERROR))) {
          mDB.setStatusToInMail(m, SEDInboxMailStatus.ERROR, "Add message to zkp deliver proccess");
        }


        mDB.setStatusToInMail(m, SEDInboxMailStatus.PROCESS,
                "Add message to zkp deliver proccess");
      } catch (StorageException ex) {
        String msg = String.format(
                "Error occurred processing mail: '%s'. Err: %s.", m.getId(),
                ex.getMessage());
        LOG.logError(l, msg, ex);
        sw.append(msg);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    sw.append("End zkp plugin task");
    return sw.toString();
  }

  private void actUponMissingOrInvalidMetadataXML(String metadataAttachmetName, MSHInMail m) throws StorageException {
    mDB.setStatusToInMail(m, SEDInboxMailStatus.ERROR, "Error validating metadata file");

    ElektronskaPosiljkaTip elektronskaPosiljkaTip = new ElektronskaPosiljkaTip();
    UdelezenecTip udelezenecTip = new UdelezenecTip();
    udelezenecTip.setId("GeneratedByValidation");
    elektronskaPosiljkaTip.setPrejemnik(udelezenecTip);
    ElektronskaOvojnica elektronskaOvojnica = new ElektronskaOvojnica();
    elektronskaOvojnica.setPosiljka(elektronskaPosiljkaTip);

    MimeValue soapPartMime = MimeValue.MIME_XML;
    File f = StorageUtils.getNewStorageFile(soapPartMime.getSuffix(), EBMSConstants.SOAP_PART_REQUEST_PREFIX);
    // TODO: rename original metadata XML part to something else
    MSHInPart part = new MSHInPart();
    part.setName(metadataAttachmetName);
    part.setIsSent(Boolean.FALSE);
    part.setIsReceived(Boolean.TRUE);
    part.setEbmsId(m.getMessageId());
    part.setMimeType(soapPartMime.getMimeType());
    part.setDescription("XML Metadata");
    part.setSource(SEDMailPartSource.PLUGIN.getValue());
    part.setFilename(f.getName());
    part.setFilepath(StorageUtils.getRelativePath(f));
    m.getMSHInPayload().getMSHInParts().add(part);

    // TODO: sign and timestamp the signature

    try {
      JAXBContext jc = JAXBContext.newInstance(ElektronskaOvojnica.class);
      TransformerFactory.newInstance().newTransformer().transform(
              new JAXBSource(jc, elektronskaOvojnica),
              new StreamResult(f));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CronTaskDef getDefinition() {

    CronTaskDef tt = new CronTaskDef();
    tt.setType("eodlozisce-validation");
    tt.setName("Validate eOdlozisce package");
    tt.setDescription("Validate and forward eVlozisce package.");
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

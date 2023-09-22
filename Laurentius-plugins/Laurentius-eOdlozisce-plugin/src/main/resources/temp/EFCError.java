package si.vsrs.cif.filing.enums;

import org.apache.cxf.binding.soap.SoapFault;
import si.vsrs.cif.filing.exception.ECFFaultCode;

import javax.xml.namespace.QName;

public enum EFCError {
    MISSING_USER_MESSAGE("Missing user message for the ECFInInterceptor! The incoming messages must have user-message!", 0, ECFFaultCode.Other, SoapFault.FAULT_CODE_SERVER),
    MISSING_PAYLOAD("Message with ConversationId: [%s] from [%s] does not have payload!", 2, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    MISSING_METADATA("Message with ConversationId: [%s] from [%s] does not have metadata payload with name: [%s]!", 3, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_METADATA_COUNT("Message ConversationId: [%s] from [%s] has more than one XML payload with name: [%s]!", 3, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_METADATA_SCHEMA("Message ConversationId: [%s] from [%s] has invalid metadata! Metadata does not match schema SplosnaVloga! Err: [%s]", 3, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_METADATA_PARSE("Message ConversationId: [%s] from [%s] has invalid metadata! Can not read metadata! Err: [%s]", 3, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_PAYLOAD_MIMETYPE("Message ConversationId: [%s] from [%s] has payload with invalid mimetype: [%s]!", 3, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_PAYLOAD_HASH_ALGORITHM("Message ConversationId: [%s] from [%s] has invalid payload hash algorithm: [%s], expected [%s]!", 4, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_PAYLOAD_COUNT("Message ConversationId: [%s] from [%s] has invalid payload count: [%s], expected from medatada [%s]!", 4, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_PAYLOAD_VODILNI_COUNT("Message ConversationId: [%s] from [%s] has multiple documents set as 'JeVodilni'!", 2, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_PAYLOAD_HASH("Message ConversationId: [%s] from [%s] metadata hashes [%s] and payload hashes [%s] does not match!", 4, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    MISSING_PAYLOAD_VODILNI_COUNT("Message ConversationId: [%s] from [%s] has missing documents set as 'JeVodilni'!", 2, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_SERVICE("Message with ConversationId:[%s] from [%s] has invalid service: [%s] (expected:[%s])!", 4, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_ACTION("Messagewith  ConversationId:[%s] from [%s] has invalid action: [%s] (expected:[%s])!", 4, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),

    INVALID_METADATA_COURT_TYPE("Message ConversationId: [%s] from [%s] has invalid data: SodiSif [%s]!", 3, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_METADATA_OPRST("Message ConversationId: [%s] from [%s] has invalid data: Err: %s!", 3, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),
    INVALID_METADATA_FIELD_OF_LAW("Message ConversationId: [%s] from [%s] has invalid data: [%s]!", 3, ECFFaultCode.Other, SoapFault.FAULT_CODE_CLIENT),

    SERVER_ERROR("Message ConversationId: [%s] from [%s] was rejected due to server error [%s]!", 3, ECFFaultCode.Other, SoapFault.FAULT_CODE_SERVER),
    TIMESTAMP_ERROR("Timestamp error for Message ConversationId: [%s] from [%s]. Error [%s]!", 3, ECFFaultCode.Other, SoapFault.FAULT_CODE_SERVER),
    MISSING_SIGNATURE("Message ConversationId: [%s] from [%s] has unsigned VLoga! Signature is mandatory!", 2, ECFFaultCode.Other, SoapFault.FAULT_CODE_SERVER),
    INVALID_PDF("Message ConversationId: [%s] from [%s] was rejected due invalid pdf [%s]. Error: [%s]!", 4, ECFFaultCode.Other, SoapFault.FAULT_CODE_SERVER),
    ;

    String errorTemplate;
    int argumentCount;
    ECFFaultCode ecfFaultCode;
    QName soapFault;

    EFCError(String errorTemplate, int argumentCount, ECFFaultCode ecfFaultCode, QName soapFault) {
        this.errorTemplate = errorTemplate;
        this.argumentCount = argumentCount;
        this.ecfFaultCode = ecfFaultCode;
        this.soapFault = soapFault;
    }

    public String getErrorTemplate() {
        return errorTemplate;
    }

    public int getArgumentCount() {
        return argumentCount;
    }

    public ECFFaultCode getEcfFaultCode() {
        return ecfFaultCode;
    }

    public QName getSoapFault() {
        return soapFault;
    }
}

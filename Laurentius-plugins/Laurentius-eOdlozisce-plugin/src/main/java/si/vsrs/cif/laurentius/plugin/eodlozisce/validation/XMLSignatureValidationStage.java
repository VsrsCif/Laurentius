package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import org.w3._2000._09.xmldsig_.KeyInfo;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3._2000._09.xmldsig_.SignatureValue;
import si.vsrs.cif.laurentius.plugin.eodlozisce.sig.XMLSignatureUtils;

import java.util.List;

public class XMLSignatureValidationStage implements ValidationStage<List<Signature>>{
    private final XMLSignatureUtils xmlSignatureUtils;

    public XMLSignatureValidationStage() {
        this.xmlSignatureUtils = new XMLSignatureUtils();
    }

    @Override
    public ValidationResult validate(List<Signature> data) {
        ValidationResult result = new ValidationResult();

        data.forEach(signature -> {
            SignatureValue signatureValue = signature.getSignatureValue();
            KeyInfo keyInfo = signature.getKeyInfo();
//            xmlSignatureUtils.validateXAdESEnvelopedSignature(data);
        });


        return result;
    }
}

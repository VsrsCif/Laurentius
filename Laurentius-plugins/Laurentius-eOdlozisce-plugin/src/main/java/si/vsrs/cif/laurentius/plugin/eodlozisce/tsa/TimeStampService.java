package si.vsrs.cif.laurentius.plugin.eodlozisce.tsa;

import si.src.setcce.sign.wsclient.VerifyResult;

public interface TimeStampService {
    String timeStampXml(String xml) throws TimestampException;

    VerifyResult verifyTimestampSignature(String xml);
}

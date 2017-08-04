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
package si.laurentius.lce.sign.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import si.laurentius.commons.utils.SEDLogger;
import static java.lang.System.getProperty;
import java.math.BigInteger;
import java.util.Base64;
import javax.xml.crypto.dsig.DigestMethod;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.tsp.TimeStampResp;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;

/**
 *
 * @author Jože Rihtaršič
 */
public class SiTsaTimeStampRFC3161  {

  

  private static final String TSA_ALGORITHM = DigestMethod.SHA1;
                    

  
  public static final SEDLogger LOG = new SEDLogger(SiTsaTimeStampRFC3161.class);

  // String mstrTimeStampServerUrl = "http://ts.si-tsa.sigov.si:80/verificationserver/timestamp";
  String mstrResultLogFolder = getProperty("java.io.tmpdir");
  String mstrTimeStampServerUrl = null;
  
 

  public void stamp() {
    LOG.logStart();
    LOG.logWarn("start stamping", null);

    String ocspUrl = "http://ts.si-tsa.sigov.si:80/verificationserver/rfc3161timestamp";
    OutputStream out = null;
    HttpURLConnection con = null;

    try {

        TimeStampRequestGenerator timeStampRequestGenerator = new TimeStampRequestGenerator();
      //  timeStampRequestGenerator.setReqPolicy(new ASN1ObjectIdentifier("2.16.56.9.3.1"));
      timeStampRequestGenerator.setCertReq(true);
        //TimeStampRequest timeStampRequest = timeStampRequestGenerator.generate(TSPAlgorithms.SHA1, new byte[20], BigInteger.valueOf(100));
        TimeStampRequest timeStampRequest = timeStampRequestGenerator.generate(TSPAlgorithms.SHA1, new byte[20], BigInteger.valueOf(100));
        byte request[] = timeStampRequest.getEncoded();

        URL url = new URL(ocspUrl);
        con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-type", "application/timestamp-query");
        con.setRequestProperty("Content-length", String.valueOf(request.length));
        out = con.getOutputStream();
        out.write(request);
        out.flush();

        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Received HTTP error: " + con.getResponseCode() + " - " + con.getResponseMessage());
        } else {
          System.out.println("Response Code: ".concat(Integer.toString(con.getResponseCode())));
            LOG.logError("Response Code: ".concat(Integer.toString(con.getResponseCode())), null);
        }
        InputStream in = con.getInputStream();
        TimeStampResp resp = TimeStampResp.getInstance(new ASN1InputStream(in).readObject());
        TimeStampResponse response = new TimeStampResponse(resp);
        response.validate(timeStampRequest);
System.out.println("Status = "+ response.getStatusString());
        LOG.logError("Status = "+ response.getStatusString(), null);

        if (response.getFailInfo()
                != null) {

          System.out.println("Status = "+response.getFailInfo().intValue());
            switch (response.getFailInfo().intValue()) {
                case 0: {
                    LOG.logError( "unrecognized or unsupported Algorithm Identifier", null);
                    return;
                }

                case 2: {
                    LOG.logError("transaction not permitted or supported", null);
                    return;
                }

                case 5: {
                    LOG.logError("the data submitted has the wrong format", null);
                    return;
                }

                case 14: {
                    LOG.logError("the TSA’s time source is not available", null);
                    return;
                }

                case 15: {
                    LOG.logError("the requested TSA policy is not supported by the TSA", null);
                    return;
                }
                case 16: {
                    LOG.logError("the requested extension is not supported by the TSA", null);
                    return;
                }

                case 17: {
                    LOG.logError("the additional information requested could not be understood or is not available", null);
                    return;
                }

                case 25: {
                    LOG.logError("the request cannot be handled due to system failure", null);
                    return;
                }
            }
        }

        System.out.println("Timestamp: " + Base64.getEncoder().encodeToString(response.getEncoded()));
        System.out.println("TSA: " + response.getTimeStampToken().getTimeStampInfo().getTsa());
        System.out.println("Serial number:" + response.getTimeStampToken().getTimeStampInfo().getSerialNumber());
        System.out.println("Policy: " + response.getTimeStampToken().getTimeStampInfo().getPolicy());
        
        LOG.logError("Timestamp: " +  Base64.getEncoder().encodeToString(response.getEncoded()), null);
        LOG.logError("TSA: " + response.getTimeStampToken().getTimeStampInfo().getTsa(), null);
        LOG.logError("Serial number:" + response.getTimeStampToken().getTimeStampInfo().getSerialNumber(), null);
        LOG.logError("Policy: " + response.getTimeStampToken().getTimeStampInfo().getPolicy(), null);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

}

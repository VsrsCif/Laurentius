/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package si.laurentius.lce.sign.pdf;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import org.apache.pdfbox.io.IOUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

/**
 * This is an PDF signature utils inspired by Vakhtang Koroghlishvili (PDFBox example)
 */
public class SignUtils implements SignatureInterface {

  public static final String S_TEMPLATE_IMAGE = "/laurentius_pdf_template.png";

  public static final String S_SIGN_METHOD = "SHA256WithRSA";

  private SignatureOptions signatureOptions;
  private PDVisibleSignDesigner visibleSignDesigner;
  private PDVisibleSigProperties visibleSignatureProperties = null;

  private PrivateKey privateKey;
  private X509Certificate certificate;
  
  SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

  /**
   *
   * @param privateKey
   * @param certificate
   */
  public SignUtils(PrivateKey privateKey, X509Certificate certificate) {
    setPrivateKey(privateKey);
    setCertificate(certificate);
  }

  public final void setPrivateKey(PrivateKey privateKey) {
    this.privateKey = privateKey;
  }

  public final void setCertificate(X509Certificate certificate) {
    this.certificate = certificate;
  }

  /**
   * SignatureInterface implementation.
   *
   * This method will be called from inside of the pdfbox and create the PKCS #7 signature. The
   * given InputStream contains the bytes that are given by the byte range.
   *
   * This method is for internal use only.
   *
   * Use your favorite cryptographic library to implement PKCS #7 signature creation.
   *
   * @param content
   * @return
   * @throws java.io.IOException
   */
  @Override
  public byte[] sign(InputStream content)
      throws IOException {

    try {
      List<Certificate> certList = Collections.singletonList(certificate);
      Store certs = new JcaCertStore(certList);

      // get BC cert 
      org.bouncycastle.asn1.x509.Certificate cert =
          org.bouncycastle.asn1.x509.Certificate.getInstance(ASN1Primitive.fromByteArray(
              certificate.getEncoded()));

      //signer 
      ContentSigner sha1Signer = new JcaContentSignerBuilder(S_SIGN_METHOD).build(privateKey);
      // signed data generator
      CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
      gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
          new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer,
          new X509CertificateHolder(cert)));
      // append certificate
      gen.addCertificates(certs);

      CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
      //Generate a CMS Signed Data object which carrying a detached CMS signature, 
      CMSSignedData signedData = gen.generate(msg, false);
      // return signed values
      return signedData.getEncoded();
    } catch (GeneralSecurityException | CMSException | OperatorCreationException e) {
      throw new IOException(e);
    }
  }
  
   

  public void createDefaultVisualization(File pdfFile, X509Certificate xc, Date dt, String location, String reason) {
    try (InputStream imageStream = SignUtils.class.getResourceAsStream(S_TEMPLATE_IMAGE);
        InputStream isFile = new FileInputStream(pdfFile)) {

      
      int page = 1;
      String name = xc.getSubjectDN().toString();
      String issuer = xc.getIssuerDN().toString();
       String sn = xc.getSerialNumber().toString();
      try {
        LdapName ldName = new LdapName(name);
        LdapName ldIssuer = new LdapName(issuer);
        name = (String)ldName.getRdn(ldName.size()-1).getValue();
        issuer = (String)ldIssuer.getRdn(ldIssuer.size()-1).getValue();
      } catch (InvalidNameException ex) {
        Logger.getLogger(SignUtils.class.getName()).log(Level.SEVERE, null, ex);
      }

      
     
      BufferedImage bimage = ImageIO.read(imageStream);
      Graphics2D g2d = bimage.createGraphics();
      g2d.setColor(Color.BLACK);
      g2d.setFont(new Font("Arial", Font.PLAIN, 17));
      
      g2d.drawString("Podpisnik:", 10, 60);
      g2d.drawString("Serijska št.:", 10, 80);
      g2d.drawString("Izdajatelj:", 10, 100);
      g2d.drawString("Dat. podp.:", 10, 120);
      g2d.drawString(name, 120, 60);      
      g2d.drawString(sn, 120, 80);
      g2d.drawString(issuer, 120, 100);
      g2d.drawString(sdf.format(dt), 120, 120);

      visibleSignDesigner = new PDVisibleSignDesigner(isFile, bimage, page);
      visibleSignDesigner.xAxis(10).yAxis(10).zoom(-60);

      setVisibleSignatureProperties(name, location, reason, 0, page, true);
    } catch (IOException ex) {
      Logger.getLogger(SignUtils.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public void setVisibleSignDesigner(File pdfDoc, int x, int y, int zoomPercent,
      InputStream imageStream, int page)
      throws IOException {
    visibleSignDesigner = new PDVisibleSignDesigner(pdfDoc.getAbsolutePath(), imageStream, page);
    visibleSignDesigner.xAxis(x).yAxis(y).zoom(zoomPercent);
  }

  public void setVisibleSignatureProperties(String name,
      String location,
      String reason,
      int preferredSize,
      int page,
      boolean visualSignEnabled)
      throws IOException {
    visibleSignatureProperties = new PDVisibleSigProperties();
    visibleSignatureProperties.signerName(name).signerLocation(location).signatureReason(reason).
        preferredSize(preferredSize).page(page).visualSignEnabled(visualSignEnabled).
        setPdVisibleSignature(visibleSignDesigner);
  }

  

  /**
   * Sign pdf file and create new file that ends with "_signed.pdf".
   *
   * @param pdfInputStream
   * @param signedFile The file to be signed.
   * @param createDefVis
   * @throws IOException
   */
  public void signPDF(File inputFile,  File signedFile, boolean createDefVis, String location, String reason)
      throws IOException {

    // set sign date
    Calendar signDate = Calendar.getInstance();
    
    if (createDefVis){
      createDefaultVisualization(inputFile, certificate, signDate.getTime(), location, reason);
    }
    // creating output document and prepare the IO streams.
    // load document
    try ( PDDocument doc = PDDocument.load(inputFile);
        FileOutputStream fos = new FileOutputStream(signedFile)) {
      PDSignature signature;
      // create signature dictionary
      signature = new PDSignature();
      
      // default filter
      signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
      
      // subfilter for basic and PAdES Part 2 signatures
      signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
      
      if (visibleSignatureProperties != null) {
        // this builds the signature structures in a separate document
        visibleSignatureProperties.buildSignature();
        
        signature.setName(visibleSignatureProperties.getSignerName());
        signature.setLocation(visibleSignatureProperties.getSignerLocation());
        signature.setReason(visibleSignatureProperties.getSignatureReason());
      }
      
      // the signing date, needed for valid signature
      signature.setSignDate(signDate);
      
      // SignatureInterface instance
      SignatureInterface signatureInterface = this;
      
      // register signature dictionary and sign interface
      if (visibleSignatureProperties != null && visibleSignatureProperties.isVisualSignEnabled()) {
        signatureOptions = new SignatureOptions();
        signatureOptions.setVisualSignature(visibleSignatureProperties.getVisibleSignature());
        //signatureOptions.setVisualSignature(visibleSignatureProperties.getVisibleSignature());
        signatureOptions.setPage(visibleSignatureProperties.getPage() - 1);
        doc.addSignature(signature, signatureInterface, signatureOptions);
      } else {
        doc.addSignature(signature, signatureInterface);
      }
      
      // write incremental (only for signing purpose)
      doc.saveIncremental(fos);
    }

    // do not close options before saving, because some COSStream objects within options 
    // are transferred to the signed document.
    IOUtils.closeQuietly(signatureOptions);
  }

}

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
package si.jrc.msh.exception;

/**
 *
 * @author Jože Rihtaršič
 */
public enum EBMSErrorCode {

  /**
     *
     *//**
     *
     */
  ValueNotRecognized(
      "EBMS:0001",
      "ValueNotRecognized",
      "failure",
      "Content",
      "Although the message document is well formed and schema valid, some element/attribute contains a value that could not be recognized and therefore could not be used by the MSH.",
      "ebms"),

  /**
     *
     */
  FeatureNotSupportedWarning(
      "EBMS:0002",
      "FeatureNotSupported",
      "warning",
      "Content",
      "Although the message document is well formed and schema valid, some element/attribute value cannot be processed as expected because the related feature is not supported by the MSH.",
      "ebms"),

  /**
     *
     */
  ValueInconsistent(
      "EBMS:0003",
      "ValueInconsistent",
      "failure",
      "Content",
      "Although the message document is well formed and schema valid, some element/attribute value is inconsistent either with the content of other element/attribute, or with the processing mode of the MSH, or with the normative requirements of the ebMS specification.",
      "ebms"),

  /**
     *
     */
  Other("EBMS:0004", "Other", "failure", "Content", "Other error", "ebms"),

  /**
     *
     */
  ConnectionFailure(
      "EBMS:0005",
      "ConnectionFailure",
      "failure",
      "Communication",
      "The MSH is experiencing temporary or permanent failure in trying to open a transport connection with a remote MSH.",
      "ebms"),

  /**
     *
     */
  EmptyMessagePartitionChannel("EBMS:0006", "EmptyMessagePartitionChannel", "warning",
      "Communication", "There is no message available for pulling from this MPC at this moment.",
      "ebms"),

  /**
     *
     */
  MimeInconsistency("EBMS:0007", "MimeInconsistency", "failure", "Unpackaging",
      "The use of MIME is not consistent with the required usage in this specification.", "ebms"),

  /**
     *
     */
  FeatureNotSupportedFailure(
      "EBMS:0008",
      "FeatureNotSupported",
      "failure",
      "Unpackaging",
      "Although the message document is well formed and schema valid, the presence or absence of some element/ attribute is not consistent with the capability of the MSH, with respect to supported features.",
      "ebms"),

  /**
     *
     */
  InvalidHeader(
      "EBMS:0009",
      "InvalidHeader",
      "failure",
      "Unpackaging",
      "The ebMS header is either not well formed as an XML document, or does not conform to the ebMS packaging rules.",
      "ebms"),

  /**
     *
     */
  ProcessingModeMismatch(
      "EBMS:0010",
      "ProcessingModeMismatch",
      "failure",
      "Processing",
      "The ebMS header or another header (e.g. reliability, security) expected by the MSH is not compatible with the expected content, based on the associated P-Mode.",
      "ebms"),

  /**
     *
     */
  ExternalPayloadError(
      "EBMS:0011",
      "ExternalPayloadError",
      "failure",
      "Content",
      "The MSH is unable to resolve an external payload reference (i.e. a Part that is not contained within the ebMS Message, as identified by a PartInfo/href URI).",
      "ebms"),

  /**
     *
     */
  FailedAuthentication(
      "EBMS:0101",
      "FailedAuthentication",
      "failure",
      "Processing",
      "The signature in the Security header intended for the 'ebms' SOAP actor, could not be validated by the Security module.",
      "security"),

  /**
     *
     */
  FailedDecryption(
      "EBMS:0102",
      "FailedDecryption",
      "failure",
      "Processing",
      "The encrypted data reference the Security header intended for the 'ebms' SOAP actor could not be decrypted by the Security Module.",
      "security"),

  /**
     *
     */
  PolicyNoncompliance(
      "EBMS:0103",
      "PolicyNoncompliance",
      "failure",
      "Processing",
      "The processor determined that the message's security methods, parameters, scope or other security policy-level requirements or agreements were not satisfied.",
      "security"),

  /**
     *
     */
  DysfunctionalReliability(
      "EBMS:0201",
      "DysfunctionalReliability",
      "failure",
      "Processing",
      "Some reliability function as implemented by the Reliability module, is not operational, or the reliability state associated with this message sequence is not valid.",
      "reliability"),

  /**
     *
     */
  DeliveryFailure(
      "EBMS:0202",
      "DeliveryFailure",
      "failure",
      "Communication",
      "Although the message was sent under Guaranteed delivery requirement, the Reliability module could not get assurance that the message was properly delivered, in spite of resending efforts.",
      "reliability"),

  /**
     *
     */
  MissingReceipt(
      "EBMS:0301",
      "MissingReceipt",
      "failure",
      "Communication",
      "A Receipt has not been received  for a message that was previously sent by the MSH generating this error.",
      "reliability"),

  /**
     *
     */
  InvalidReceipt(
      "EBMS:0302",
      "InvalidReceipt",
      "failure",
      "Communication",
      "A Receipt has been received  for a message that was previously sent by the MSH generating this error, but the content does not match the message content (e.g. some part has not been acknowledged, or the digest associated does not match the signature digest, for NRR).",
      "reliability"),

  /**
     *
     */
  DecompressionFailure("EBMS:0303", 
      "DecompressionFailure", 
      "failure", 
      "Communication",
      "An error occurred during the decompression.",
      "reliability"),
  
  
      /*
  */
  DuplicateDeteced("EBMS:1000", "DuplicateDeteced", "warning",
      "Processing", "Message was already received in receivers duplicate detection time window! " +
          "According to receivers settings duplicate is eliminated!",
      "reliability"),
  
  
  InvalidSoapRequest("EBMS:1500", "InvalidSoapRequest", 
      "failure", 
      "Communication",
      "Error parsing soap message.", 
      "init"),
  
  /**
     *
     */
  PModeConfigurationError("EBMS:1501", 
      "BadPModeConfiguration", 
      "failure", 
      "Processing",
      "An error occurred during initializing pmode.", 
      "configuration"),

  
    /**
     *
     */
  ApplicationError("EBMS:1503", 
      "ApplicationError", 
      "failure", 
      "Processing",
      "Unexpected program error", 
      "application");
  
  
  


  String code;
  String name;
  String severity;
  String category;
  String description;
  String origin;

  EBMSErrorCode(String cd, String nm, String sv, String ct, String desc, String org) {
    code = cd;
    name = nm;
    severity = sv;
    category = ct;
    description = desc;
    origin = org;
  }

  /**
   *
   * @return
   */
  public String getCode() {
    return code;
  }

  /**
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @return
   */
  public String getSeverity() {
    return severity;
  }

  /**
   *
   * @return
   */
  public String getCategory() {
    return category;
  }

  /**
   *
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   *
   * @return
   */
  public String getOrigin() {
    return origin;
  }

}

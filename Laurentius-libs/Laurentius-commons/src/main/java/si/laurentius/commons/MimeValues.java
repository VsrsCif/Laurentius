/*
 * Copyright 2015, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence", true); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.commons;

import java.util.HashMap;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public enum MimeValues {

  /**
   *
   */
  MIME_7Z("7z", "application/x-7z-compressed", true),

  /**
   *
   */
  MIME_ABW("abw", "application/x-abiword", true),

  /**
   *
   */
  MIME_AI("ai", "application/postscript", true),

  /**
   *
   */
  MIME_AIF("aif", "audio/x-aiff", true),

  /**
   *
   */
  MIME_AIFC("aifc", "audio/x-aiff", true),

  /**
   *
   */
  MIME_AIFF("aiff", "audio/x-aiff", true),

  /**
   *
   */
  MIME_ALC("alc", "chemical/x-alchemy", true),

  /**
   *
   */
  MIME_AMR("amr", "audio/amr", true),

  /**
   *
   */
  MIME_ANX("anx", "application/annodex", true),

  /**
   *
   */
  MIME_APK("apk", "application/vnd.android.package-archive", true),

  /**
   *
   */
  MIME_APPCACHE("appcache", "text/cache-manifest", true),

  /**
   *
   */
  MIME_ART("art", "image/x-jg", true),

  /**
   *
   */
  MIME_ASC("asc", "text/plain", false),

  /**
   *
   */
  MIME_ASF("asf", "video/x-ms-asf", true),

  /**
   *
   */
  MIME_ASN("asn", "chemical/x-ncbi-asn1", true),

  /**
   *
   */
  MIME_ASN1("asn", "chemical/x-ncbi-asn1-spec", true),

  /**
   *
   */
  MIME_ASO("aso", "chemical/x-ncbi-asn1-binary", true),

  /**
   *
   */
  MIME_ASX("asx", "video/x-ms-asf", true),

  /**
   *
   */
  MIME_ATOM("atom", "application/atom+xml", true),

  /**
   *
   */
  MIME_ATOMCAT("atomcat", "application/atomcat+xml", true),

  /**
   *
   */
  MIME_ATOMSRV("atomsrv", "application/atomserv+xml", true),

  /**
   *
   */
  MIME_AU("au", "audio/basic", true),

  /**
   *
   */
  MIME_AVI("avi", "video/x-msvideo", true),

  /**
   *
   */
  MIME_AWB("awb", "audio/amr-wb", true),

  /**
   *
   */
  MIME_AXA("axa", "audio/annodex", true),

  /**
   *
   */
  MIME_AXV("axv", "video/annodex", true),

  /**
   *
   */
  MIME_BAK("bak", "application/x-trash", true),

  /**
   *
   */
  MIME_BAT("bat", "application/x-msdos-program", true),

  /**
   *
   */
  MIME_B("b", "chemical/x-molconn-Z", true),

  /**
   *
   */
  MIME_BCPIO("bcpio", "application/x-bcpio", true),

  /**
   *
   */
  MIME_BIB("bib", "text/x-bibtex", true),

  /**
   *
   */
  MIME_BIN("bin", "application/octet-stream", true),

  /**
   *
   */
  MIME_BMP("bmp", "image/x-ms-bmp", true),

  /**
   *
   */
  MIME_BOOK("book", "application/x-maker", true),

  /**
   *
   */
  MIME_BOO("boo", "text/x-boo", true),

  /**
   *
   */
  MIME_BRF("brf", "text/plain", false),

  /**
   *
   */
  MIME_BSD("bsd", "chemical/x-crossfire", true),

  /**
   *
   */
  MIME_C3D("c3d", "chemical/x-chem3d", true),

  /**
   *
   */
  MIME_CAB("cab", "application/x-cab", true),

  /**
   *
   */
  MIME_CAC("cac", "chemical/x-cache", true),

  /**
   *
   */
  MIME_CACHE("cache", "chemical/x-cache", true),

  /**
   *
   */
  MIME_CAP("cap", "application/vnd.tcpdump.pcap", true),

  /**
   *
   */
  MIME_CASCII("cascii", "chemical/x-cactvs-binary", true),

  /**
   *
   */
  MIME_CAT("cat", "application/vnd.ms-pki.seccat", true),

  /**
   *
   */
  MIME_CBIN("cbin", "chemical/x-cactvs-binary", true),

  /**
   *
   */
  MIME_CBR("cbr", "application/x-cbr", true),

  /**
   *
   */
  MIME_CBZ("cbz", "application/x-cbz", true),

  /**
   *
   */
  MIME_CC("cc", "text/x-c++src", true),

  /**
   *
   */
  MIME_CDA("cda", "application/x-cdf", true),

  /**
   *
   */
  MIME_CDF("cdf", "application/x-cdf", true),

  /**
   *
   */
  MIME_CDR("cdr", "image/x-coreldraw", true),

  /**
   *
   */
  MIME_CDT("cdt", "image/x-coreldrawtemplate", true),

  /**
   *
   */
  MIME_CDX("cdx", "chemical/x-cdx", true),

  /**
   *
   */
  MIME_CDY("cdy", "application/vnd.cinderella", true),

  /**
   *
   */
  MIME_CEF("cef", "chemical/x-cxf", true),

  /**
   *
   */
  MIME_CER("cer", "chemical/x-cerius", true),

  /**
   *
   */
  MIME_CHM("chm", "chemical/x-chemdraw", true),

  /**
   *
   */
  MIME_CHRT("chrt", "application/x-kchart", true),

  /**
   *
   */
  MIME_CIF("cif", "chemical/x-cif", true),

  /**
   *
   */
  MIME_CLS("cls", "text/x-tex", true),

  /**
   *
   */
  MIME_CMDF("cmdf", "chemical/x-cmdf", true),

  /**
   *
   */
  MIME_CML("cml", "chemical/x-cml", true),

  /**
   *
   */
  MIME_COD("cod", "application/vnd.rim.cod", true),

  /**
   *
   */
  MIME_COM("com", "application/x-msdos-program", true),

  /**
   *
   */
  MIME_CPA("cpa", "chemical/x-compass", true),

  /**
   *
   */
  MIME_CPIO("cpio", "application/x-cpio", true),

  /**
   *
   */
  MIME_CPP("cpp", "text/x-c++src", true),

  /**
   *
   */
  MIME_CPT("cpt", "application/mac-compactpro", true),

  /**
   *
   */
  MIME_CP1("cpt", "image/x-corelphotopaint", true),

  /**
   *
   */
  MIME_CR2("cr2", "image/x-canon-cr2", true),

  /**
   *
   */
  MIME_CRL("crl", "application/x-pkcs7-crl", true),

  /**
   *
   */
  MIME_CRT("crt", "application/x-x509-ca-cert", true),

  /**
   *
   */
  MIME_CRW("crw", "image/x-canon-crw", true),

  /**
   *
   */
  MIME_CSD("csd", "audio/csound", true),

  /**
   *
   */
  MIME_CSF("csf", "chemical/x-cache-csf", true),

  /**
   *
   */
  MIME_CSH("csh", "application/x-csh", true),

  /**
   *
   */
  MIME_CSH1("csh", "text/x-csh", true),

  /**
   *
   */
  MIME_CSM("csm", "chemical/x-csml", true),

  /**
   *
   */
  MIME_CSML("csml", "chemical/x-csml", true),

  /**
   *
   */
  MIME_CSS("css", "text/css", true),

  /**
   *
   */
  MIME_CSV("csv", "text/csv", true),

  /**
   *
   */
  MIME_CTAB("ctab", "chemical/x-cactvs-binary", true),

  /**
   *
   */
  MIME_C("c", "text/x-csrc", true),

  /**
   *
   */
  MIME_CTX("ctx", "chemical/x-ctx", true),

  /**
   *
   */
  MIME_CU("cu", "application/cu-seeme", true),

  /**
   *
   */
  MIME_CUB("cub", "chemical/x-gaussian-cube", true),

  /**
   *
   */
  MIME_CXF("cxf", "chemical/x-cxf", true),

  /**
   *
   */
  MIME_CXX("cxx", "text/x-c++src", true),

  /**
   *
   */
  MIME_DAT("dat", "application/x-ns-proxy-autoconfig", true),

  /**
   *
   */
  MIME_DAVMOUNT("davmount", "application/davmount+xml", true),

  /**
   *
   */
  MIME_DCM("dcm", "application/dicom", true),

  /**
   *
   */
  MIME_DCR("dcr", "application/x-director", true),

  /**
   *
   */
  MIME_DEB("deb", "application/x-debian-package", true),

  /**
   *
   */
  MIME_DIFF("diff", "text/x-diff", true),

  /**
   *
   */
  MIME_DIF("dif", "video/dv", true),

  /**
   *
   */
  MIME_DIR("dir", "application/x-director", true),

  /**
   *
   */
  MIME_DJV("djv", "image/vnd.djvu", true),

  /**
   *
   */
  MIME_DJVU("djvu", "image/vnd.djvu", true),

  /**
   *
   */
  MIME_DLL("dll", "application/x-msdos-program", true),

  /**
   *
   */
  MIME_DL("dl", "video/dl", true),

  /**
   *
   */
  MIME_DMG("dmg", "application/x-apple-diskimage", true),

  /**
   *
   */
  MIME_DMS("dms", "application/x-dms", true),

  /**
   *
   */
  MIME_DOC("doc", "application/msword", true),

  /**
   *
   */
  MIME_DOCM("docm", "application/vnd.ms-word.document.macroEnabled.12", true),

  /**
   *
   */
  MIME_DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", true),

  /**
   *
   */
  MIME_DOT("dot", "application/msword", true),

  /**
   *
   */
  MIME_DOTM("dotm", "application/vnd.ms-word.template.macroEnabled.12", true),

  /**
   *
   */
  MIME_DOTX("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template", true),

  /**
   *
   */
  MIME_D("d", "text/x-dsrc", true),

  /**
   *
   */
  MIME_DVI("dvi", "application/x-dvi", true),

  /**
   *
   */
  MIME_DV("dv", "video/dv", true),

  /**
   *
   */
  MIME_DX("dx", "chemical/x-jcamp-dx", true),

  /**
   *
   */
  MIME_DXR("dxr", "application/x-director", true),

  /**
   *
   */
  MIME_EMB("emb", "chemical/x-embl-dl-nucleotide", true),

  /**
   *
   */
  MIME_EMBL("embl", "chemical/x-embl-dl-nucleotide", true),

  /**
   *
   */
  MIME_EML("eml", "message/rfc822", true),

  /**
   *
   */
  MIME_ENT("ent", "chemical/x-ncbi-asn1-ascii", true),

  /**
   *
   */
  MIME_ENT1("ent", "chemical/x-pdb", true),

  /**
   *
   */
  MIME_EOT("eot", "application/vnd.ms-fontobject", true),

  /**
   *
   */
  MIME_EPS2("eps2", "application/postscript", true),

  /**
   *
   */
  MIME_EPS3("eps3", "application/postscript", true),

  /**
   *
   */
  MIME_EPS("eps", "application/postscript", true),

  /**
   *
   */
  MIME_EPSF("epsf", "application/postscript", true),

  /**
   *
   */
  MIME_EPSI("epsi", "application/postscript", true),

  /**
   *
   */
  MIME_ERF("erf", "image/x-epson-erf", true),

  /**
   *
   */
  MIME_ES("es", "application/ecmascript", true),

  /**
   *
   */
  MIME_ETX("etx", "text/x-setext", true),

  /**
   *
   */
  MIME_EXE("exe", "application/x-msdos-program", true),

  /**
   *
   */
  MIME_EZ("ez", "application/andrew-inset", true),

  /**
   *
   */
  MIME_FB("fb", "application/x-maker", true),

  /**
   *
   */
  MIME_FBDOC("fbdoc", "application/x-maker", true),

  /**
   *
   */
  MIME_FCH("fch", "chemical/x-gaussian-checkpoint", true),

  /**
   *
   */
  MIME_FCHK("fchk", "chemical/x-gaussian-checkpoint", true),

  /**
   *
   */
  MIME_FIG("fig", "application/x-xfig", true),

  /**
   *
   */
  MIME_FLAC("flac", "audio/flac", true),

  /**
   *
   */
  MIME_FLI("fli", "video/fli", true),

  /**
   *
   */
  MIME_FLV("flv", "video/x-flv", true),

  /**
   *
   */
  MIME_FM("fm", "application/x-maker", true),

  /**
   *
   */
  MIME_FRAME("frame", "application/x-maker", true),

  /**
   *
   */
  MIME_FRM("frm", "application/x-maker", true),

  /**
   *
   */
  MIME_GAL("gal", "chemical/x-gaussian-log", true),

  /**
   *
   */
  MIME_GAM("gam", "chemical/x-gamess-input", true),

  /**
   *
   */
  MIME_GAMIN("gamin", "chemical/x-gamess-input", true),

  /**
   *
   */
  MIME_GAN("gan", "application/x-ganttproject", true),

  /**
   *
   */
  MIME_GAU("gau", "chemical/x-gaussian-input", true),

  /**
   *
   */
  MIME_GCD("gcd", "text/x-pcs-gcd", true),

  /**
   *
   */
  MIME_GCF("gcf", "application/x-graphing-calculator", true),

  /**
   *
   */
  MIME_GCG("gcg", "chemical/x-gcg8-sequence", true),

  /**
   *
   */
  MIME_GEN("gen", "chemical/x-genbank", true),

  /**
   *
   */
  MIME_GF("gf", "application/x-tex-gf", true),

  /**
   *
   */
  MIME_GIF("gif", "image/gif", true),

  /**
   *
   */
  MIME_GJC("gjc", "chemical/x-gaussian-input", true),

  /**
   *
   */
  MIME_GJF("gjf", "chemical/x-gaussian-input", true),

  /**
   *
   */
  MIME_GL("gl", "video/gl", true),

  /**
   *
   */
  MIME_GNUMERIC("gnumeric", "application/x-gnumeric", true),

  /**
   *
   */
  MIME_GPT("gpt", "chemical/x-mopac-graph", true),

  /**
   *
   */
  MIME_GSF("gsf", "application/x-font", true),

  /**
   *
   */
  MIME_GSM("gsm", "audio/x-gsm", true),

  /**
   *
   */
  MIME_GTAR("gtar", "application/x-gtar", true),

  /**
   *
   */
  MIME_HDF("hdf", "application/x-hdf", true),

  /**
   *
   */
  MIME_HH("hh", "text/x-c++hdr", true),

  /**
   *
   */
  MIME_HIN("hin", "chemical/x-hin", true),

  /**
   *
   */
  MIME_HPP("hpp", "text/x-c++hdr", true),

  /**
   *
   */
  MIME_HQX("hqx", "application/mac-binhex40", true),

  /**
   *
   */
  MIME_HS("hs", "text/x-haskell", true),

  /**
   *
   */
  MIME_HTA("hta", "application/hta", true),

  /**
   *
   */
  MIME_HTC("htc", "text/x-component", true),

  /**
   *
   */
  MIME_H("h", "text/x-chdr", true),

  /**
   *
   */
  MIME_HTML("html", "text/html", true),

  /**
   *
   */
  MIME_HTM("htm", "text/html", true),

  /**
   *
   */
  MIME_HWP("hwp", "application/x-hwp", true),

  /**
   *
   */
  MIME_HXX("hxx", "text/x-c++hdr", true),

  /**
   *
   */
  MIME_ICA("ica", "application/x-ica", true),

  /**
   *
   */
  MIME_ICE("ice", "x-conference/x-cooltalk", true),

  /**
   *
   */
  MIME_ICO("ico", "image/vnd.microsoft.icon", true),

  /**
   *
   */
  MIME_ICS("ics", "text/calendar", true),

  /**
   *
   */
  MIME_ICZ("icz", "text/calendar", true),

  /**
   *
   */
  MIME_IEF("ief", "image/ief", true),

  /**
   *
   */
  MIME_IGES("iges", "model/iges", true),

  /**
   *
   */
  MIME_IGS("igs", "model/iges", true),

  /**
   *
   */
  MIME_III("iii", "application/x-iphone", true),

  /**
   *
   */
  MIME_INFO("info", "application/x-info", true),

  /**
   *
   */
  MIME_INP("inp", "chemical/x-gamess-input", true),

  /**
   *
   */
  MIME_INS("ins", "application/x-internet-signup", true),

  /**
   *
   */
  MIME_ISO("iso", "application/x-iso9660-image", true),

  /**
   *
   */
  MIME_ISP("isp", "application/x-internet-signup", true),

  /**
   *
   */
  MIME_IST("ist", "chemical/x-isostar", true),

  /**
   *
   */
  MIME_ISTR("istr", "chemical/x-isostar", true),

  /**
   *
   */
  MIME_JAD("jad", "text/vnd.sun.j2me.app-descriptor", true),

  /**
   *
   */
  MIME_JAM("jam", "application/x-jam", true),

  /**
   *
   */
  MIME_JAR("jar", "application/java-archive", true),

  /**
   *
   */
  MIME_JAVA("java", "text/x-java", true),

  /**
   *
   */
  MIME_JDX("jdx", "chemical/x-jcamp-dx", true),

  /**
   *
   */
  MIME_JMZ("jmz", "application/x-jmol", true),

  /**
   *
   */
  MIME_JNG("jng", "image/x-jng", true),

  /**
   *
   */
  MIME_JNLP("jnlp", "application/x-java-jnlp-file", true),

  /**
   *
   */
  MIME_JP2("jp2", "image/jp2", true),

  /**
   *
   */
  MIME_JPEG("jpeg", "image/jpeg", true),

  /**
   *
   */
  MIME_JPE("jpe", "image/jpeg", true),

  /**
   *
   */
  MIME_JPF("jpf", "image/jpx", true),

  /**
   *
   */
  MIME_JPG2("jpg2", "image/jp2", true),

  /**
   *
   */
  MIME_JPG("jpg", "image/jpeg", true),

  /**
   *
   */
  MIME_JPM("jpm", "image/jpm", true),

  /**
   *
   */
  MIME_JPX("jpx", "image/jpx", true),

  /**
   *
   */
  MIME_JS("js", "application/javascript", true),

  /**
   *
   */
  MIME_JSON("json", "application/json", true),

  /**
   *
   */
  MIME_KAR("kar", "audio/midi", true),

  /**
   *
   */
  MIME_KEY("key", "application/pgp-keys", true),

  /**
   *
   */
  MIME_KIL("kil", "application/x-killustrator", true),

  /**
   *
   */
  MIME_KIN("kin", "chemical/x-kinemage", true),

  /**
   *
   */
  MIME_KML("kml", "application/vnd.google-earth.kml+xml", true),

  /**
   *
   */
  MIME_KMZ("kmz", "application/vnd.google-earth.kmz", true),

  /**
   *
   */
  MIME_KPR("kpr", "application/x-kpresenter", true),

  /**
   *
   */
  MIME_KPT("kpt", "application/x-kpresenter", true),

  /**
   *
   */
  MIME_KSP("ksp", "application/x-kspread", true),

  /**
   *
   */
  MIME_KWD("kwd", "application/x-kword", true),

  /**
   *
   */
  MIME_KWT("kwt", "application/x-kword", true),

  /**
   *
   */
  MIME_LATEX("latex", "application/x-latex", true),

  /**
   *
   */
  MIME_LHA("lha", "application/x-lha", true),

  /**
   *
   */
  MIME_LHS("lhs", "text/x-literate-haskell", true),

  /**
   *
   */
  MIME_LIN("lin", "application/bbolin", true),

  /**
   *
   */
  MIME_LSF("lsf", "video/x-la-asf", true),

  /**
   *
   */
  MIME_LSX("lsx", "video/x-la-asf", true),

  /**
   *
   */
  MIME_LTX("ltx", "text/x-tex", true),

  /**
   *
   */
  MIME_LY("ly", "text/x-lilypond", true),

  /**
   *
   */
  MIME_LYX("lyx", "application/x-lyx", true),

  /**
   *
   */
  MIME_LZH("lzh", "application/x-lzh", true),

  /**
   *
   */
  MIME_LZX("lzx", "application/x-lzx", true),

  /**
   *
   */
  MIME_M3G("m3g", "application/m3g", true),

  /**
   *
   */
  MIME_M3U8("m3u8", "application/x-mpegURL", true),

  /**
   *
   */
  MIME_M3U("m3u", "audio/mpegurl", true),

  /**
   *
   */
  MIME_M3U1("m3u", "audio/x-mpegurl", true),

  /**
   *
   */
  MIME_M4A("m4a", "audio/mpeg", true),

  /**
   *
   */
  MIME_MAKER("maker", "application/x-maker", true),

  /**
   *
   */
  MIME_MAN("man", "application/x-troff-man", true),

  /**
   *
   */
  MIME_MBOX("mbox", "application/mbox", true),

  /**
   *
   */
  MIME_MCIF("mcif", "chemical/x-mmcif", true),

  /**
   *
   */
  MIME_MCM("mcm", "chemical/x-macmolecule", true),

  /**
   *
   */
  MIME_MD5("md5", "application/x-md5", true),

  /**
   *
   */
  MIME_MDB("mdb", "application/msaccess", true),

  /**
   *
   */
  MIME_ME("me", "application/x-troff-me", true),

  /**
   *
   */
  MIME_MESH("mesh", "model/mesh", true),

  /**
   *
   */
  MIME_MID("mid", "audio/midi", true),

  /**
   *
   */
  MIME_MIDI("midi", "audio/midi", true),

  /**
   *
   */
  MIME_MIF("mif", "application/x-mif", true),

  /**
   *
   */
  MIME_MKV("mkv", "video/x-matroska", true),

  /**
   *
   */
  MIME_MM("mm", "application/x-freemind", true),

  /**
   *
   */
  MIME_MMD("mmd", "chemical/x-macromodel-input", true),

  /**
   *
   */
  MIME_MMF("mmf", "application/vnd.smaf", true),

  /**
   *
   */
  MIME_MML("mml", "text/mathml", true),

  /**
   *
   */
  MIME_MMOD("mmod", "chemical/x-macromodel-input", true),

  /**
   *
   */
  MIME_MNG("mng", "video/x-mng", true),

  /**
   *
   */
  MIME_MOC("moc", "text/x-moc", true),

  /**
   *
   */
  MIME_MOL2("mol2", "chemical/x-mol2", true),

  /**
   *
   */
  MIME_MOL("mol", "chemical/x-mdl-molfile", true),

  /**
   *
   */
  MIME_MOO("moo", "chemical/x-mopac-out", true),

  /**
   *
   */
  MIME_MOP("mop", "chemical/x-mopac-input", true),

  /**
   *
   */
  MIME_MOPCRT("mopcrt", "chemical/x-mopac-input", true),

  /**
   *
   */
  MIME_MOVIE("movie", "video/x-sgi-movie", true),

  /**
   *
   */
  MIME_MOV("mov", "video/quicktime", true),

  /**
   *
   */
  MIME_MP2("mp2", "audio/mpeg", true),

  /**
   *
   */
  MIME_MP3("mp3", "audio/mpeg", true),

  /**
   *
   */
  MIME_MP4("mp4", "video/mp4", true),

  /**
   *
   */
  MIME_MPC("mpc", "chemical/x-mopac-input", true),

  /**
   *
   */
  MIME_MPEGA("mpega", "audio/mpeg", true),

  /**
   *
   */
  MIME_MPEG("mpeg", "video/mpeg", true),

  /**
   *
   */
  MIME_MPE("mpe", "video/mpeg", true),

  /**
   *
   */
  MIME_MPGA("mpga", "audio/mpeg", true),

  /**
   *
   */
  MIME_MPG("mpg", "video/mpeg", true),

  /**
   *
   */
  MIME_MPH("mph", "application/x-comsol", true),

  /**
   *
   */
  MIME_MPV("mpv", "video/x-matroska", true),

  /**
   *
   */
  MIME_MS("ms", "application/x-troff-ms", true),

  /**
   *
   */
  MIME_MSH("msh", "model/mesh", true),

  /**
   *
   */
  MIME_MSI("msi", "application/x-msi", true),

  /**
   *
   */
  MIME_MVB("mvb", "chemical/x-mopac-vib", true),

  /**
   *
   */
  MIME_MXF("mxf", "application/mxf", true),

  /**
   *
   */
  MIME_MXU("mxu", "video/vnd.mpegurl", true),

  /**
   *
   */
  MIME_NB("nb", "application/mathematica", true),

  /**
   *
   */
  MIME_NBP("nbp", "application/mathematica", true),

  /**
   *
   */
  MIME_NC("nc", "application/x-netcdf", true),

  /**
   *
   */
  MIME_NEF("nef", "image/x-nikon-nef", true),

  /**
   *
   */
  MIME_NWC("nwc", "application/x-nwc", true),

  /**
   *
   */
  MIME_O("o", "application/x-object", true),

  /**
   *
   */
  MIME_ODA("oda", "application/oda", true),

  /**
   *
   */
  MIME_ODB("odb", "application/vnd.oasis.opendocument.database", true),

  /**
   *
   */
  MIME_ODC("odc", "application/vnd.oasis.opendocument.chart", true),

  /**
   *
   */
  MIME_ODF("odf", "application/vnd.oasis.opendocument.formula", true),

  /**
   *
   */
  MIME_ODG("odg", "application/vnd.oasis.opendocument.graphics", true),

  /**
   *
   */
  MIME_ODI("odi", "application/vnd.oasis.opendocument.image", true),

  /**
   *
   */
  MIME_ODM("odm", "application/vnd.oasis.opendocument.text-master", true),

  /**
   *
   */
  MIME_ODP("odp", "application/vnd.oasis.opendocument.presentation", true),

  /**
   *
   */
  MIME_ODS("ods", "application/vnd.oasis.opendocument.spreadsheet", true),

  /**
   *
   */
  MIME_ODT("odt", "application/vnd.oasis.opendocument.text", true),

  /**
   *
   */
  MIME_OGA("oga", "audio/ogg", true),

  /**
   *
   */
  MIME_OGG("ogg", "audio/ogg", true),

  /**
   *
   */
  MIME_OGV("ogv", "video/ogg", true),

  /**
   *
   */
  MIME_OGX("ogx", "application/ogg", true),

  /**
   *
   */
  MIME_OLD("old", "application/x-trash", true),

  /**
   *
   */
  MIME_ONE("one", "application/onenote", true),

  /**
   *
   */
  MIME_ONEPKG("onepkg", "application/onenote", true),

  /**
   *
   */
  MIME_ONETMP("onetmp", "application/onenote", true),

  /**
   *
   */
  MIME_ONETOC2("onetoc2", "application/onenote", true),

  /**
   *
   */
  MIME_OPUS("opus", "audio/ogg", true),

  /**
   *
   */
  MIME_ORC("orc", "audio/csound", true),

  /**
   *
   */
  MIME_ORF("orf", "image/x-olympus-orf", true),

  /**
   *
   */
  MIME_OTG("otg", "application/vnd.oasis.opendocument.graphics-template", true),

  /**
   *
   */
  MIME_OTH("oth", "application/vnd.oasis.opendocument.text-web", true),

  /**
   *
   */
  MIME_OTP("otp", "application/vnd.oasis.opendocument.presentation-template", true),

  /**
   *
   */
  MIME_OTS("ots", "application/vnd.oasis.opendocument.spreadsheet-template", true),

  /**
   *
   */
  MIME_OTT("ott", "application/vnd.oasis.opendocument.text-template", true),

  /**
   *
   */
  MIME_OZA("oza", "application/x-oz-application", true),

  /**
   *
   */
  MIME_P7R("p7r", "application/x-pkcs7-certreqresp", true),

  /**
   *
   */
  MIME_PAC("pac", "application/x-ns-proxy-autoconfig", true),

  /**
   *
   */
  MIME_PAS("pas", "text/x-pascal", true),

  /**
   *
   */
  MIME_PATCH("patch", "text/x-diff", true),

  /**
   *
   */
  MIME_PAT("pat", "image/x-coreldrawpattern", true),

  /**
   *
   */
  MIME_PBM("pbm", "image/x-portable-bitmap", true),

  /**
   *
   */
  MIME_PCAP("pcap", "application/vnd.tcpdump.pcap", true),

  /**
   *
   */
  MIME_PCF("pcf", "application/x-font", true),

  /**
   *
   */
  MIME_PCX("pcx", "image/pcx", true),

  /**
   *
   */
  MIME_PDB("pdb", "chemical/x-pdb", true),

  /**
   *
   */
  MIME_PDF("pdf", "application/pdf", true),

  /**
   *
   */
  MIME_PFA("pfa", "application/x-font", true),

  /**
   *
   */
  MIME_PFB("pfb", "application/x-font", true),

  /**
   *
   */
  MIME_PGM("pgm", "image/x-portable-graymap", true),

  /**
   *
   */
  MIME_PGN("pgn", "application/x-chess-pgn", true),

  /**
   *
   */
  MIME_PGP("pgp", "application/pgp-encrypted", true),

  /**
   *
   */
  MIME_PK("pk", "application/x-tex-pk", true),

  /**
   *
   */
  MIME_PLS("pls", "audio/x-scpls", true),

  /**
   *
   */
  MIME_PL("pl", "text/x-perl", true),

  /**
   *
   */
  MIME_PM("pm", "text/x-perl", true),

  /**
   *
   */
  MIME_PNG("png", "image/png", true),

  /**
   *
   */
  MIME_PNM("pnm", "image/x-portable-anymap", true),

  /**
   *
   */
  MIME_POTM("potm", "application/vnd.ms-powerpoint.template.macroEnabled.12", true),

  /**
   *
   */
  MIME_POT("pot", "text/plain", false),

  /**
   *
   */
  MIME_POTX("potx", "application/vnd.openxmlformats-officedocument.presentationml.template", true),

  /**
   *
   */
  MIME_PPAM("ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12", true),

  /**
   *
   */
  MIME_PPM("ppm", "image/x-portable-pixmap", true),

  /**
   *
   */
  MIME_PPS("pps", "application/vnd.ms-powerpoint", true),

  /**
   *
   */
  MIME_PPSM("ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12", true),

  /**
   *
   */
  MIME_PPSX("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow", true),

  /**
   *
   */
  MIME_PPT("ppt", "application/vnd.ms-powerpoint", true),

  /**
   *
   */
  MIME_PPTM("pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12", true),

  /**
   *
   */
  MIME_PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", true),

  /**
   *
   */
  MIME_PRF("prf", "application/pics-rules", true),

  /**
   *
   */
  MIME_PRT("prt", "chemical/x-ncbi-asn1-ascii", true),

  /**
   *
   */
  MIME_PS("ps", "application/postscript", true),

  /**
   *
   */
  MIME_PSD("psd", "image/x-photoshop", true),

  /**
   *
   */
  MIME_P("p", "text/x-pascal", true),

  /**
   *
   */
  MIME_PYC("pyc", "application/x-python-code", true),

  /**
   *
   */
  MIME_PYO("pyo", "application/x-python-code", true),

  /**
   *
   */
  MIME_PY("py", "text/x-python", true),

  /**
   *
   */
  MIME_QGS("qgs", "application/x-qgis", true),

  /**
   *
   */
  MIME_QTL("qtl", "application/x-quicktimeplayer", true),

  /**
   *
   */
  MIME_QT("qt", "video/quicktime", true),

  /**
   *
   */
  MIME_RA("ra", "audio/x-pn-realaudio", true),

  /**
   *
   */
  MIME_RA1("ra", "audio/x-realaudio", true),

  /**
   *
   */
  MIME_RAM("ram", "audio/x-pn-realaudio", true),

  /**
   *
   */
  MIME_RAR("rar", "application/rar", true),

  /**
   *
   */
  MIME_RAS("ras", "image/x-cmu-raster", true),

  /**
   *
   */
  MIME_RB("rb", "application/x-ruby", true),

  /**
   *
   */
  MIME_RD("rd", "chemical/x-mdl-rdfile", true),

  /**
   *
   */
  MIME_RDF("rdf", "application/rdf+xml", true),

  /**
   *
   */
  MIME_RDP("rdp", "application/x-rdp", true),

  /**
   *
   */
  MIME_RGB("rgb", "image/x-rgb", true),

  /**
   *
   */
  MIME_RM("rm", "audio/x-pn-realaudio", true),

  /**
   *
   */
  MIME_ROFF("roff", "application/x-troff", true),

  /**
   *
   */
  MIME_ROS("ros", "chemical/x-rosdal", true),

  /**
   *
   */
  MIME_RPM("rpm", "application/x-redhat-package-manager", true),

  /**
   *
   */
  MIME_RSS("rss", "application/x-rss+xml", true),

  /**
   *
   */
  MIME_RTF("rtf", "application/rtf", true),

  /**
   *
   */
  MIME_RTX("rtx", "text/richtext", true),

  /**
   *
   */
  MIME_RXN("rxn", "chemical/x-mdl-rxnfile", true),

  /**
   *
   */
  MIME_SCALA("scala", "text/x-scala", true),

  /**
   *
   */
  MIME_SCE("sce", "application/x-scilab", true),

  /**
   *
   */
  MIME_SCI("sci", "application/x-scilab", true),

  /**
   *
   */
  MIME_SCO("sco", "audio/csound", true),

  /**
   *
   */
  MIME_SCR("scr", "application/x-silverlight", true),

  /**
   *
   */
  MIME_SCT("sct", "text/scriptlet", true),

  /**
   *
   */
  MIME_SD2("sd2", "audio/x-sd2", true),

  /**
   *
   */
  MIME_SDA("sda", "application/vnd.stardivision.draw", true),

  /**
   *
   */
  MIME_SDC("sdc", "application/vnd.stardivision.calc", true),

  /**
   *
   */
  MIME_SD("sd", "chemical/x-mdl-sdfile", true),

  /**
   *
   */
  MIME_SDD("sdd", "application/vnd.stardivision.impress", true),

  /**
   *
   */
  MIME_SDF("sdf", "application/vnd.stardivision.math", true),

  /**
   *
   */
  MIME_SDF1("sdf", "chemical/x-mdl-sdfile", true),

  /**
   *
   */
  MIME_SDS("sds", "application/vnd.stardivision.chart", true),

  /**
   *
   */
  MIME_SDW("sdw", "application/vnd.stardivision.writer", true),

  /**
   *
   */
  MIME_SER("ser", "application/java-serialized-object", true),

  /**
   *
   */
  MIME_SFV("sfv", "text/x-sfv", true),

  /**
   *
   */
  MIME_SGF("sgf", "application/x-go-sgf", true),

  /**
   *
   */
  MIME_SGL("sgl", "application/vnd.stardivision.writer-global", true),

  /**
   *
   */
  MIME_SHA1("sha1", "application/x-sha1", true),

  /**
   *
   */
  MIME_SH("sh", "application/x-sh", true),

  /**
   *
   */
  MIME_SHAR("shar", "application/x-shar", true),

  /**
   *
   */
  MIME_SHP("shp", "application/x-qgis", true),

  /**
   *
   */
  MIME_SH1("sh", "text/x-sh", true),

  /**
   *
   */
  MIME_SHTML("shtml", "text/html", true),

  /**
   *
   */
  MIME_SHX("shx", "application/x-qgis", true),

  /**
   *
   */
  MIME_SID("sid", "audio/prs.sid", true),

  /**
   *
   */
  MIME_SIG("sig", "application/pgp-signature", true),

  /**
   *
   */
  MIME_SIK("sik", "application/x-trash", true),

  /**
   *
   */
  MIME_SILO("silo", "model/mesh", true),

  /**
   *
   */
  MIME_SIS("sis", "application/vnd.symbian.install", true),

  /**
   *
   */
  MIME_SISX("sisx", "x-epoc/x-sisx-app", true),

  /**
   *
   */
  MIME_SIT("sit", "application/x-stuffit", true),

  /**
   *
   */
  MIME_SITX("sitx", "application/x-stuffit", true),

  /**
   *
   */
  MIME_SKD("skd", "application/x-koan", true),

  /**
   *
   */
  MIME_SKM("skm", "application/x-koan", true),

  /**
   *
   */
  MIME_SKP("skp", "application/x-koan", true),

  /**
   *
   */
  MIME_SKT("skt", "application/x-koan", true),

  /**
   *
   */
  MIME_SLDM("sldm", "application/vnd.ms-powerpoint.slide.macroEnabled.12", true),

  /**
   *
   */
  MIME_SLDX("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide", true),

  /**
   *
   */
  MIME_SMI("smi", "application/smil+xml", true),

  /**
   *
   */
  MIME_SMIL("smil", "application/smil+xml", true),

  /**
   *
   */
  MIME_SND("snd", "audio/basic", true),

  /**
   *
   */
  MIME_SPC("spc", "chemical/x-galactic-spc", true),

  /**
   *
   */
  MIME_SPL("spl", "application/futuresplash", true),

  /**
   *
   */
  MIME_SPL1("spl", "application/x-futuresplash", true),

  /**
   *
   */
  MIME_SPX("spx", "audio/ogg", true),

  /**
   *
   */
  MIME_SQL("sql", "application/x-sql", true),

  /**
   *
   */
  MIME_SRC("src", "application/x-wais-source", true),

  /**
   *
   */
  MIME_SRT("srt", "text/plain", false),

  /**
   *
   */
  MIME_STC("stc", "application/vnd.sun.xml.calc.template", true),

  /**
   *
   */
  MIME_STD("std", "application/vnd.sun.xml.draw.template", true),

  /**
   *
   */
  MIME_STI("sti", "application/vnd.sun.xml.impress.template", true),

  /**
   *
   */
  MIME_STL("stl", "application/sla", true),

  /**
   *
   */
  MIME_STW("stw", "application/vnd.sun.xml.writer.template", true),

  /**
   *
   */
  MIME_STY("sty", "text/x-tex", true),

  /**
   *
   */
  MIME_SV4CPIO("sv4cpio", "application/x-sv4cpio", true),

  /**
   *
   */
  MIME_SV4CRC("sv4crc", "application/x-sv4crc", true),

  /**
   *
   */
  MIME_SVG("svg", "image/svg+xml", true),

  /**
   *
   */
  MIME_SVGZ("svgz", "image/svg+xml", true),

  /**
   *
   */
  MIME_SW("sw", "chemical/x-swissprot", true),

  /**
   *
   */
  MIME_SWF("swf", "application/x-shockwave-flash", true),

  /**
   *
   */
  MIME_SWFL("swfl", "application/x-shockwave-flash", true),

  /**
   *
   */
  MIME_SXC("sxc", "application/vnd.sun.xml.calc", true),

  /**
   *
   */
  MIME_SXD("sxd", "application/vnd.sun.xml.draw", true),

  /**
   *
   */
  MIME_SXG("sxg", "application/vnd.sun.xml.writer.global", true),

  /**
   *
   */
  MIME_SXI("sxi", "application/vnd.sun.xml.impress", true),

  /**
   *
   */
  MIME_SXM("sxm", "application/vnd.sun.xml.math", true),

  /**
   *
   */
  MIME_SXW("sxw", "application/vnd.sun.xml.writer", true),

  /**
   *
   */
  MIME_T("t", "application/x-troff", true),

  /**
   *
   */
  MIME_TAR("tar", "application/x-tar", true),

  /**
   *
   */
  MIME_TAZ("taz", "application/x-gtar-compressed", true),

  /**
   *
   */
  MIME_TCL("tcl", "application/x-tcl", true),

  /**
   *
   */
  MIME_TCL1("tcl", "text/x-tcl", true),

  /**
   *
   */
  MIME_TEXI("texi", "application/x-texinfo", true),

  /**
   *
   */
  MIME_TEXINFO("texinfo", "application/x-texinfo", true),

  /**
   *
   */
  MIME_TEX("tex", "text/x-tex", true),

  /**
   *
   */
  MIME_TEXT("text", "text/plain", false),

  /**
   *
   */
  MIME_TGF("tgf", "chemical/x-mdl-tgf", true),

  /**
   *
   */
  MIME_TGZ("tgz", "application/x-gtar-compressed", true),

  /**
   *
   */
  MIME_THMX("thmx", "application/vnd.ms-officetheme", true),

  /**
   *
   */
  MIME_TIFF("tiff", "image/tiff", true),

  /**
   *
   */
  MIME_TIF("tif", "image/tiff", true),

  /**
   *
   */
  MIME_TK("tk", "text/x-tcl", true),

  /**
   *
   */
  MIME_TM("tm", "text/texmacs", true),

  /**
   *
   */
  MIME_TORRENT("torrent", "application/x-bittorrent", true),

  /**
   *
   */
  MIME_TR("tr", "application/x-troff", true),

  /**
   *
   */
  MIME_TSP("tsp", "application/dsptype", true),

  /**
   *
   */
  MIME_TS("ts", "video/MP2T", true),

  /**
   *
   */
  MIME_TSV("tsv", "text/tab-separated-values", true),

  /**
   *
   */
  MIME_TTL("ttl", "text/turtle", true),

  /**
   *
   */
  MIME_TXT("txt", "text/plain", true),

  /**
   *
   */
  MIME_UDEB("udeb", "application/x-debian-package", true),

  /**
   *
   */
  MIME_ULS("uls", "text/iuls", true),

  /**
   *
   */
  MIME_USTAR("ustar", "application/x-ustar", true),

  /**
   *
   */
  MIME_VAL("val", "chemical/x-ncbi-asn1-binary", true),

  /**
   *
   */
  MIME_VCD("vcd", "application/x-cdlink", true),

  /**
   *
   */
  MIME_VCF("vcf", "text/x-vcard", true),

  /**
   *
   */
  MIME_VCS("vcs", "text/x-vcalendar", true),

  /**
   *
   */
  MIME_VMD("vmd", "chemical/x-vmd", true),

  /**
   *
   */
  MIME_VMS("vms", "chemical/x-vamas-iso14976", true),

  /**
   *
   */
  MIME_VRML("vrml", "model/vrml", true),

  /**
   *
   */
  MIME_VRML1("vrml", "x-world/x-vrml", true),

  /**
   *
   */
  MIME_VRM("vrm", "x-world/x-vrml", true),

  /**
   *
   */
  MIME_VSD("vsd", "application/vnd.visio", true),

  /**
   *
   */
  MIME_WAD("wad", "application/x-doom", true),

  /**
   *
   */
  MIME_WAV("wav", "audio/x-wav", true),

  /**
   *
   */
  MIME_WAX("wax", "audio/x-ms-wax", true),

  /**
   *
   */
  MIME_WBMP("wbmp", "image/vnd.wap.wbmp", true),

  /**
   *
   */
  MIME_WBXML("wbxml", "application/vnd.wap.wbxml", true),

  /**
   *
   */
  MIME_WEBM("webm", "video/webm", true),

  /**
   *
   */
  MIME_WK("wk", "application/x-123", true),

  /**
   *
   */
  MIME_WMA("wma", "audio/x-ms-wma", true),

  /**
   *
   */
  MIME_WMD("wmd", "application/x-ms-wmd", true),

  /**
   *
   */
  MIME_WMLC("wmlc", "application/vnd.wap.wmlc", true),

  /**
   *
   */
  MIME_WMLSC("wmlsc", "application/vnd.wap.wmlscriptc", true),

  /**
   *
   */
  MIME_WMLS("wmls", "text/vnd.wap.wmlscript", true),

  /**
   *
   */
  MIME_WML("wml", "text/vnd.wap.wml", true),

  /**
   *
   */
  MIME_WM("wm", "video/x-ms-wm", true),

  /**
   *
   */
  MIME_WMV("wmv", "video/x-ms-wmv", true),

  /**
   *
   */
  MIME_WMX("wmx", "video/x-ms-wmx", true),

  /**
   *
   */
  MIME_WMZ("wmz", "application/x-ms-wmz", true),

  /**
   *
   */
  MIME_WOFF("woff", "application/x-font-woff", true),

  /**
   *
   */
  MIME_WP5("wp5", "application/vnd.wordperfect5.1", true),

  /**
   *
   */
  MIME_WPD("wpd", "application/vnd.wordperfect", true),

  /**
   *
   */
  MIME_WRL("wrl", "model/vrml", true),

  /**
   *
   */
  MIME_WRL1("wrl", "x-world/x-vrml", true),

  /**
   *
   */
  MIME_WSC("wsc", "text/scriptlet", true),

  /**
   *
   */
  MIME_WVX("wvx", "video/x-ms-wvx", true),

  /**
   *
   */
  MIME_WZ("wz", "application/x-wingz", true),

  /**
   *
   */
  MIME_X3DB("x3db", "model/x3d+binary", true),

  /**
   *
   */
  MIME_X3D("x3d", "model/x3d+xml", true),

  /**
   *
   */
  MIME_X3DV("x3dv", "model/x3d+vrml", true),

  /**
   *
   */
  MIME_XBM("xbm", "image/x-xbitmap", true),

  /**
   *
   */
  MIME_XCF("xcf", "application/x-xcf", true),

  /**
   *
   */
  MIME_XCOS("xcos", "application/x-scilab-xcos", true),

  /**
   *
   */
  MIME_XHT("xht", "application/xhtml+xml", true),

  /**
   *
   */
  MIME_XHTML("xhtml", "application/xhtml+xml", true),

  /**
   *
   */
  MIME_XLAM("xlam", "application/vnd.ms-excel.addin.macroEnabled.12", true),

  /**
   *
   */
  MIME_XLB("xlb", "application/vnd.ms-excel", true),

  /**
   *
   */
  MIME_XLS("xls", "application/vnd.ms-excel", true),

  /**
   *
   */
  MIME_XLSB("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12", true),

  /**
   *
   */
  MIME_XLSM("xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12", true),

  /**
   *
   */
  MIME_XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", true),

  /**
   *
   */
  MIME_XLT("xlt", "application/vnd.ms-excel", true),

  /**
   *
   */
  MIME_XLTM("xltm", "application/vnd.ms-excel.template.macroEnabled.12", true),

  /**
   *
   */
  MIME_XLTX("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template", true),

  /**
   *
   */
  MIME_XML("xml", "application/xml", true),

  /**
   *
   */
  MIME_XML1("xml", "text/xml", true),

  /**
   *
   */
  MIME_XPI("xpi", "application/x-xpinstall", true),

  /**
   *
   */
  MIME_XPM("xpm", "image/x-xpixmap", true),

  /**
   *
   */
  MIME_XSD("xsd", "application/xsd", true),

  /**
   *
   */
  MIME_XSL("xsl", "application/xslt+xml", true),

  /**
   *
   */
  MIME_XSLT("xslt", "application/xslt+xml", true),

  /**
   *
   */
  MIME_XSPF("xspf", "application/xspf+xml", true),

  /**
   *
   */
  MIME_XTEL("xtel", "chemical/x-xtel", true),

  /**
   *
   */
  MIME_XUL("xul", "application/vnd.mozilla.xul+xml", true),

  /**
   *
   */
  MIME_XWD("xwd", "image/x-xwindowdump", true),

  /**
   *
   */
  MIME_XYZ("xyz", "chemical/x-xyz", true),

  /**
   *
   */
  MIME_ZIP("zip", "application/zip", true),
  
  /**
   *
   */
  MIME_GZIP("gzip", "application/gzip", true),

  /**
   *
   */
  MIME_ZMT("zmt", "chemical/x-mopac-input", true),
  
  /**
   * this mime is not official!!!
   */
  MIME_ENC("enc", "application/encrypted", true);

  private final String mstrMimeType;
  private final String mstrSuffix;
  private final boolean mDefForMime;

  MimeValues(String suffix, String mimeType, boolean defForMime) {
    this.mstrMimeType = mimeType;
    this.mstrSuffix = suffix;
    this.mDefForMime = defForMime;
  }

  /**
   *
   * @return
   */
  public String getMimeType() {
    return mstrMimeType;
  }
  /**
   *
   * @return
   */
  public boolean getDefForMime() {
    return mDefForMime;
  }

  /**
   *
   * @return
   */
  public String getSuffix() {
    return mstrSuffix;
  }

  /**
   * Method returns suffix for mimetype if no mimetype is found MIME_BIN.getSuffix is returned-
   *
   * @param strMimeType
   * @return suffix
   */
  public static String getSuffixBYMimeType(String strMimeType) {
    String res = MIME_BIN.getSuffix();

    for (MimeValues vm : values()) {
      if (vm.getMimeType().equalsIgnoreCase(strMimeType)) {
        res = vm.getSuffix();
        if (vm.getDefForMime()) {
          break;
        }
      }
    }
    return res;

  }

  /**
   * Method returns mimetype according filename suffix. If filename is null or mimetype is not found
   * "MIME_BIN" mimetype is returned.
   *
   * @param strFileName
   * @return Mimetype string
   */
  public static String getMimeTypeByFileName(String strFileName) {

    return strFileName == null || strFileName.isEmpty() ? MIME_BIN.getMimeType() :
        getMimeTypeBySuffix(strFileName.substring(strFileName.lastIndexOf('.') + 1));
  }

  /**
   * Method returns Mimetype by suffix.
   *
   * @param strSuffix
   * @return Mimetype string
   */
  public static String getMimeTypeBySuffix(String strSuffix) {
    String res = MIME_BIN.getMimeType();
    if (strSuffix != null  && !strSuffix.trim().isEmpty()) {      
      for (MimeValues vm : values()) {
        if (vm.getSuffix().equalsIgnoreCase(strSuffix)) {
          res = vm.getMimeType();
          break;
        }
      }
    }

    return res;
  }

}

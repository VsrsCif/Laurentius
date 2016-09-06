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
  MIME_7Z("7z", "application/x-7z-compressed"),

  /**
   *
   */
  MIME_ABW("abw", "application/x-abiword"),

  /**
   *
   */
  MIME_AI("ai", "application/postscript"),

  /**
   *
   */
  MIME_AIF("aif", "audio/x-aiff"),

  /**
   *
   */
  MIME_AIFC("aifc", "audio/x-aiff"),

  /**
   *
   */
  MIME_AIFF("aiff", "audio/x-aiff"),

  /**
   *
   */
  MIME_ALC("alc", "chemical/x-alchemy"),

  /**
   *
   */
  MIME_AMR("amr", "audio/amr"),

  /**
   *
   */
  MIME_ANX("anx", "application/annodex"),

  /**
   *
   */
  MIME_APK("apk", "application/vnd.android.package-archive"),

  /**
   *
   */
  MIME_APPCACHE("appcache", "text/cache-manifest"),

  /**
   *
   */
  MIME_ART("art", "image/x-jg"),

  /**
   *
   */
  MIME_ASC("asc", "text/plain"),

  /**
   *
   */
  MIME_ASF("asf", "video/x-ms-asf"),

  /**
   *
   */
  MIME_ASN("asn", "chemical/x-ncbi-asn1"),

  /**
   *
   */
  MIME_ASN1("asn", "chemical/x-ncbi-asn1-spec"),

  /**
   *
   */
  MIME_ASO("aso", "chemical/x-ncbi-asn1-binary"),

  /**
   *
   */
  MIME_ASX("asx", "video/x-ms-asf"),

  /**
   *
   */
  MIME_ATOM("atom", "application/atom+xml"),

  /**
   *
   */
  MIME_ATOMCAT("atomcat", "application/atomcat+xml"),

  /**
   *
   */
  MIME_ATOMSRV("atomsrv", "application/atomserv+xml"),

  /**
   *
   */
  MIME_AU("au", "audio/basic"),

  /**
   *
   */
  MIME_AVI("avi", "video/x-msvideo"),

  /**
   *
   */
  MIME_AWB("awb", "audio/amr-wb"),

  /**
   *
   */
  MIME_AXA("axa", "audio/annodex"),

  /**
   *
   */
  MIME_AXV("axv", "video/annodex"),

  /**
   *
   */
  MIME_BAK("bak", "application/x-trash"),

  /**
   *
   */
  MIME_BAT("bat", "application/x-msdos-program"),

  /**
   *
   */
  MIME_B("b", "chemical/x-molconn-Z"),

  /**
   *
   */
  MIME_BCPIO("bcpio", "application/x-bcpio"),

  /**
   *
   */
  MIME_BIB("bib", "text/x-bibtex"),

  /**
   *
   */
  MIME_BIN("bin", "application/octet-stream"),

  /**
   *
   */
  MIME_BMP("bmp", "image/x-ms-bmp"),

  /**
   *
   */
  MIME_BOOK("book", "application/x-maker"),

  /**
   *
   */
  MIME_BOO("boo", "text/x-boo"),

  /**
   *
   */
  MIME_BRF("brf", "text/plain"),

  /**
   *
   */
  MIME_BSD("bsd", "chemical/x-crossfire"),

  /**
   *
   */
  MIME_C3D("c3d", "chemical/x-chem3d"),

  /**
   *
   */
  MIME_CAB("cab", "application/x-cab"),

  /**
   *
   */
  MIME_CAC("cac", "chemical/x-cache"),

  /**
   *
   */
  MIME_CACHE("cache", "chemical/x-cache"),

  /**
   *
   */
  MIME_CAP("cap", "application/vnd.tcpdump.pcap"),

  /**
   *
   */
  MIME_CASCII("cascii", "chemical/x-cactvs-binary"),

  /**
   *
   */
  MIME_CAT("cat", "application/vnd.ms-pki.seccat"),

  /**
   *
   */
  MIME_CBIN("cbin", "chemical/x-cactvs-binary"),

  /**
   *
   */
  MIME_CBR("cbr", "application/x-cbr"),

  /**
   *
   */
  MIME_CBZ("cbz", "application/x-cbz"),

  /**
   *
   */
  MIME_CC("cc", "text/x-c++src"),

  /**
   *
   */
  MIME_CDA("cda", "application/x-cdf"),

  /**
   *
   */
  MIME_CDF("cdf", "application/x-cdf"),

  /**
   *
   */
  MIME_CDR("cdr", "image/x-coreldraw"),

  /**
   *
   */
  MIME_CDT("cdt", "image/x-coreldrawtemplate"),

  /**
   *
   */
  MIME_CDX("cdx", "chemical/x-cdx"),

  /**
   *
   */
  MIME_CDY("cdy", "application/vnd.cinderella"),

  /**
   *
   */
  MIME_CEF("cef", "chemical/x-cxf"),

  /**
   *
   */
  MIME_CER("cer", "chemical/x-cerius"),

  /**
   *
   */
  MIME_CHM("chm", "chemical/x-chemdraw"),

  /**
   *
   */
  MIME_CHRT("chrt", "application/x-kchart"),

  /**
   *
   */
  MIME_CIF("cif", "chemical/x-cif"),

  /**
   *
   */
  MIME_CLS("cls", "text/x-tex"),

  /**
   *
   */
  MIME_CMDF("cmdf", "chemical/x-cmdf"),

  /**
   *
   */
  MIME_CML("cml", "chemical/x-cml"),

  /**
   *
   */
  MIME_COD("cod", "application/vnd.rim.cod"),

  /**
   *
   */
  MIME_COM("com", "application/x-msdos-program"),

  /**
   *
   */
  MIME_CPA("cpa", "chemical/x-compass"),

  /**
   *
   */
  MIME_CPIO("cpio", "application/x-cpio"),

  /**
   *
   */
  MIME_CPP("cpp", "text/x-c++src"),

  /**
   *
   */
  MIME_CPT("cpt", "application/mac-compactpro"),

  /**
   *
   */
  MIME_CP1("cpt", "image/x-corelphotopaint"),

  /**
   *
   */
  MIME_CR2("cr2", "image/x-canon-cr2"),

  /**
   *
   */
  MIME_CRL("crl", "application/x-pkcs7-crl"),

  /**
   *
   */
  MIME_CRT("crt", "application/x-x509-ca-cert"),

  /**
   *
   */
  MIME_CRW("crw", "image/x-canon-crw"),

  /**
   *
   */
  MIME_CSD("csd", "audio/csound"),

  /**
   *
   */
  MIME_CSF("csf", "chemical/x-cache-csf"),

  /**
   *
   */
  MIME_CSH("csh", "application/x-csh"),

  /**
   *
   */
  MIME_CSH1("csh", "text/x-csh"),

  /**
   *
   */
  MIME_CSM("csm", "chemical/x-csml"),

  /**
   *
   */
  MIME_CSML("csml", "chemical/x-csml"),

  /**
   *
   */
  MIME_CSS("css", "text/css"),

  /**
   *
   */
  MIME_CSV("csv", "text/csv"),

  /**
   *
   */
  MIME_CTAB("ctab", "chemical/x-cactvs-binary"),

  /**
   *
   */
  MIME_C("c", "text/x-csrc"),

  /**
   *
   */
  MIME_CTX("ctx", "chemical/x-ctx"),

  /**
   *
   */
  MIME_CU("cu", "application/cu-seeme"),

  /**
   *
   */
  MIME_CUB("cub", "chemical/x-gaussian-cube"),

  /**
   *
   */
  MIME_CXF("cxf", "chemical/x-cxf"),

  /**
   *
   */
  MIME_CXX("cxx", "text/x-c++src"),

  /**
   *
   */
  MIME_DAT("dat", "application/x-ns-proxy-autoconfig"),

  /**
   *
   */
  MIME_DAVMOUNT("davmount", "application/davmount+xml"),

  /**
   *
   */
  MIME_DCM("dcm", "application/dicom"),

  /**
   *
   */
  MIME_DCR("dcr", "application/x-director"),

  /**
   *
   */
  MIME_DEB("deb", "application/x-debian-package"),

  /**
   *
   */
  MIME_DIFF("diff", "text/x-diff"),

  /**
   *
   */
  MIME_DIF("dif", "video/dv"),

  /**
   *
   */
  MIME_DIR("dir", "application/x-director"),

  /**
   *
   */
  MIME_DJV("djv", "image/vnd.djvu"),

  /**
   *
   */
  MIME_DJVU("djvu", "image/vnd.djvu"),

  /**
   *
   */
  MIME_DLL("dll", "application/x-msdos-program"),

  /**
   *
   */
  MIME_DL("dl", "video/dl"),

  /**
   *
   */
  MIME_DMG("dmg", "application/x-apple-diskimage"),

  /**
   *
   */
  MIME_DMS("dms", "application/x-dms"),

  /**
   *
   */
  MIME_DOC("doc", "application/msword"),

  /**
   *
   */
  MIME_DOCM("docm", "application/vnd.ms-word.document.macroEnabled.12"),

  /**
   *
   */
  MIME_DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),

  /**
   *
   */
  MIME_DOT("dot", "application/msword"),

  /**
   *
   */
  MIME_DOTM("dotm", "application/vnd.ms-word.template.macroEnabled.12"),

  /**
   *
   */
  MIME_DOTX("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template"),

  /**
   *
   */
  MIME_D("d", "text/x-dsrc"),

  /**
   *
   */
  MIME_DVI("dvi", "application/x-dvi"),

  /**
   *
   */
  MIME_DV("dv", "video/dv"),

  /**
   *
   */
  MIME_DX("dx", "chemical/x-jcamp-dx"),

  /**
   *
   */
  MIME_DXR("dxr", "application/x-director"),

  /**
   *
   */
  MIME_EMB("emb", "chemical/x-embl-dl-nucleotide"),

  /**
   *
   */
  MIME_EMBL("embl", "chemical/x-embl-dl-nucleotide"),

  /**
   *
   */
  MIME_EML("eml", "message/rfc822"),

  /**
   *
   */
  MIME_ENT("ent", "chemical/x-ncbi-asn1-ascii"),

  /**
   *
   */
  MIME_ENT1("ent", "chemical/x-pdb"),

  /**
   *
   */
  MIME_EOT("eot", "application/vnd.ms-fontobject"),

  /**
   *
   */
  MIME_EPS2("eps2", "application/postscript"),

  /**
   *
   */
  MIME_EPS3("eps3", "application/postscript"),

  /**
   *
   */
  MIME_EPS("eps", "application/postscript"),

  /**
   *
   */
  MIME_EPSF("epsf", "application/postscript"),

  /**
   *
   */
  MIME_EPSI("epsi", "application/postscript"),

  /**
   *
   */
  MIME_ERF("erf", "image/x-epson-erf"),

  /**
   *
   */
  MIME_ES("es", "application/ecmascript"),

  /**
   *
   */
  MIME_ETX("etx", "text/x-setext"),

  /**
   *
   */
  MIME_EXE("exe", "application/x-msdos-program"),

  /**
   *
   */
  MIME_EZ("ez", "application/andrew-inset"),

  /**
   *
   */
  MIME_FB("fb", "application/x-maker"),

  /**
   *
   */
  MIME_FBDOC("fbdoc", "application/x-maker"),

  /**
   *
   */
  MIME_FCH("fch", "chemical/x-gaussian-checkpoint"),

  /**
   *
   */
  MIME_FCHK("fchk", "chemical/x-gaussian-checkpoint"),

  /**
   *
   */
  MIME_FIG("fig", "application/x-xfig"),

  /**
   *
   */
  MIME_FLAC("flac", "audio/flac"),

  /**
   *
   */
  MIME_FLI("fli", "video/fli"),

  /**
   *
   */
  MIME_FLV("flv", "video/x-flv"),

  /**
   *
   */
  MIME_FM("fm", "application/x-maker"),

  /**
   *
   */
  MIME_FRAME("frame", "application/x-maker"),

  /**
   *
   */
  MIME_FRM("frm", "application/x-maker"),

  /**
   *
   */
  MIME_GAL("gal", "chemical/x-gaussian-log"),

  /**
   *
   */
  MIME_GAM("gam", "chemical/x-gamess-input"),

  /**
   *
   */
  MIME_GAMIN("gamin", "chemical/x-gamess-input"),

  /**
   *
   */
  MIME_GAN("gan", "application/x-ganttproject"),

  /**
   *
   */
  MIME_GAU("gau", "chemical/x-gaussian-input"),

  /**
   *
   */
  MIME_GCD("gcd", "text/x-pcs-gcd"),

  /**
   *
   */
  MIME_GCF("gcf", "application/x-graphing-calculator"),

  /**
   *
   */
  MIME_GCG("gcg", "chemical/x-gcg8-sequence"),

  /**
   *
   */
  MIME_GEN("gen", "chemical/x-genbank"),

  /**
   *
   */
  MIME_GF("gf", "application/x-tex-gf"),

  /**
   *
   */
  MIME_GIF("gif", "image/gif"),

  /**
   *
   */
  MIME_GJC("gjc", "chemical/x-gaussian-input"),

  /**
   *
   */
  MIME_GJF("gjf", "chemical/x-gaussian-input"),

  /**
   *
   */
  MIME_GL("gl", "video/gl"),

  /**
   *
   */
  MIME_GNUMERIC("gnumeric", "application/x-gnumeric"),

  /**
   *
   */
  MIME_GPT("gpt", "chemical/x-mopac-graph"),

  /**
   *
   */
  MIME_GSF("gsf", "application/x-font"),

  /**
   *
   */
  MIME_GSM("gsm", "audio/x-gsm"),

  /**
   *
   */
  MIME_GTAR("gtar", "application/x-gtar"),

  /**
   *
   */
  MIME_HDF("hdf", "application/x-hdf"),

  /**
   *
   */
  MIME_HH("hh", "text/x-c++hdr"),

  /**
   *
   */
  MIME_HIN("hin", "chemical/x-hin"),

  /**
   *
   */
  MIME_HPP("hpp", "text/x-c++hdr"),

  /**
   *
   */
  MIME_HQX("hqx", "application/mac-binhex40"),

  /**
   *
   */
  MIME_HS("hs", "text/x-haskell"),

  /**
   *
   */
  MIME_HTA("hta", "application/hta"),

  /**
   *
   */
  MIME_HTC("htc", "text/x-component"),

  /**
   *
   */
  MIME_H("h", "text/x-chdr"),

  /**
   *
   */
  MIME_HTML("html", "text/html"),

  /**
   *
   */
  MIME_HTM("htm", "text/html"),

  /**
   *
   */
  MIME_HWP("hwp", "application/x-hwp"),

  /**
   *
   */
  MIME_HXX("hxx", "text/x-c++hdr"),

  /**
   *
   */
  MIME_ICA("ica", "application/x-ica"),

  /**
   *
   */
  MIME_ICE("ice", "x-conference/x-cooltalk"),

  /**
   *
   */
  MIME_ICO("ico", "image/vnd.microsoft.icon"),

  /**
   *
   */
  MIME_ICS("ics", "text/calendar"),

  /**
   *
   */
  MIME_ICZ("icz", "text/calendar"),

  /**
   *
   */
  MIME_IEF("ief", "image/ief"),

  /**
   *
   */
  MIME_IGES("iges", "model/iges"),

  /**
   *
   */
  MIME_IGS("igs", "model/iges"),

  /**
   *
   */
  MIME_III("iii", "application/x-iphone"),

  /**
   *
   */
  MIME_INFO("info", "application/x-info"),

  /**
   *
   */
  MIME_INP("inp", "chemical/x-gamess-input"),

  /**
   *
   */
  MIME_INS("ins", "application/x-internet-signup"),

  /**
   *
   */
  MIME_ISO("iso", "application/x-iso9660-image"),

  /**
   *
   */
  MIME_ISP("isp", "application/x-internet-signup"),

  /**
   *
   */
  MIME_IST("ist", "chemical/x-isostar"),

  /**
   *
   */
  MIME_ISTR("istr", "chemical/x-isostar"),

  /**
   *
   */
  MIME_JAD("jad", "text/vnd.sun.j2me.app-descriptor"),

  /**
   *
   */
  MIME_JAM("jam", "application/x-jam"),

  /**
   *
   */
  MIME_JAR("jar", "application/java-archive"),

  /**
   *
   */
  MIME_JAVA("java", "text/x-java"),

  /**
   *
   */
  MIME_JDX("jdx", "chemical/x-jcamp-dx"),

  /**
   *
   */
  MIME_JMZ("jmz", "application/x-jmol"),

  /**
   *
   */
  MIME_JNG("jng", "image/x-jng"),

  /**
   *
   */
  MIME_JNLP("jnlp", "application/x-java-jnlp-file"),

  /**
   *
   */
  MIME_JP2("jp2", "image/jp2"),

  /**
   *
   */
  MIME_JPEG("jpeg", "image/jpeg"),

  /**
   *
   */
  MIME_JPE("jpe", "image/jpeg"),

  /**
   *
   */
  MIME_JPF("jpf", "image/jpx"),

  /**
   *
   */
  MIME_JPG2("jpg2", "image/jp2"),

  /**
   *
   */
  MIME_JPG("jpg", "image/jpeg"),

  /**
   *
   */
  MIME_JPM("jpm", "image/jpm"),

  /**
   *
   */
  MIME_JPX("jpx", "image/jpx"),

  /**
   *
   */
  MIME_JS("js", "application/javascript"),

  /**
   *
   */
  MIME_JSON("json", "application/json"),

  /**
   *
   */
  MIME_KAR("kar", "audio/midi"),

  /**
   *
   */
  MIME_KEY("key", "application/pgp-keys"),

  /**
   *
   */
  MIME_KIL("kil", "application/x-killustrator"),

  /**
   *
   */
  MIME_KIN("kin", "chemical/x-kinemage"),

  /**
   *
   */
  MIME_KML("kml", "application/vnd.google-earth.kml+xml"),

  /**
   *
   */
  MIME_KMZ("kmz", "application/vnd.google-earth.kmz"),

  /**
   *
   */
  MIME_KPR("kpr", "application/x-kpresenter"),

  /**
   *
   */
  MIME_KPT("kpt", "application/x-kpresenter"),

  /**
   *
   */
  MIME_KSP("ksp", "application/x-kspread"),

  /**
   *
   */
  MIME_KWD("kwd", "application/x-kword"),

  /**
   *
   */
  MIME_KWT("kwt", "application/x-kword"),

  /**
   *
   */
  MIME_LATEX("latex", "application/x-latex"),

  /**
   *
   */
  MIME_LHA("lha", "application/x-lha"),

  /**
   *
   */
  MIME_LHS("lhs", "text/x-literate-haskell"),

  /**
   *
   */
  MIME_LIN("lin", "application/bbolin"),

  /**
   *
   */
  MIME_LSF("lsf", "video/x-la-asf"),

  /**
   *
   */
  MIME_LSX("lsx", "video/x-la-asf"),

  /**
   *
   */
  MIME_LTX("ltx", "text/x-tex"),

  /**
   *
   */
  MIME_LY("ly", "text/x-lilypond"),

  /**
   *
   */
  MIME_LYX("lyx", "application/x-lyx"),

  /**
   *
   */
  MIME_LZH("lzh", "application/x-lzh"),

  /**
   *
   */
  MIME_LZX("lzx", "application/x-lzx"),

  /**
   *
   */
  MIME_M3G("m3g", "application/m3g"),

  /**
   *
   */
  MIME_M3U8("m3u8", "application/x-mpegURL"),

  /**
   *
   */
  MIME_M3U("m3u", "audio/mpegurl"),

  /**
   *
   */
  MIME_M3U1("m3u", "audio/x-mpegurl"),

  /**
   *
   */
  MIME_M4A("m4a", "audio/mpeg"),

  /**
   *
   */
  MIME_MAKER("maker", "application/x-maker"),

  /**
   *
   */
  MIME_MAN("man", "application/x-troff-man"),

  /**
   *
   */
  MIME_MBOX("mbox", "application/mbox"),

  /**
   *
   */
  MIME_MCIF("mcif", "chemical/x-mmcif"),

  /**
   *
   */
  MIME_MCM("mcm", "chemical/x-macmolecule"),

  /**
   *
   */
  MIME_MD5("md5", "application/x-md5"),

  /**
   *
   */
  MIME_MDB("mdb", "application/msaccess"),

  /**
   *
   */
  MIME_ME("me", "application/x-troff-me"),

  /**
   *
   */
  MIME_MESH("mesh", "model/mesh"),

  /**
   *
   */
  MIME_MID("mid", "audio/midi"),

  /**
   *
   */
  MIME_MIDI("midi", "audio/midi"),

  /**
   *
   */
  MIME_MIF("mif", "application/x-mif"),

  /**
   *
   */
  MIME_MKV("mkv", "video/x-matroska"),

  /**
   *
   */
  MIME_MM("mm", "application/x-freemind"),

  /**
   *
   */
  MIME_MMD("mmd", "chemical/x-macromodel-input"),

  /**
   *
   */
  MIME_MMF("mmf", "application/vnd.smaf"),

  /**
   *
   */
  MIME_MML("mml", "text/mathml"),

  /**
   *
   */
  MIME_MMOD("mmod", "chemical/x-macromodel-input"),

  /**
   *
   */
  MIME_MNG("mng", "video/x-mng"),

  /**
   *
   */
  MIME_MOC("moc", "text/x-moc"),

  /**
   *
   */
  MIME_MOL2("mol2", "chemical/x-mol2"),

  /**
   *
   */
  MIME_MOL("mol", "chemical/x-mdl-molfile"),

  /**
   *
   */
  MIME_MOO("moo", "chemical/x-mopac-out"),

  /**
   *
   */
  MIME_MOP("mop", "chemical/x-mopac-input"),

  /**
   *
   */
  MIME_MOPCRT("mopcrt", "chemical/x-mopac-input"),

  /**
   *
   */
  MIME_MOVIE("movie", "video/x-sgi-movie"),

  /**
   *
   */
  MIME_MOV("mov", "video/quicktime"),

  /**
   *
   */
  MIME_MP2("mp2", "audio/mpeg"),

  /**
   *
   */
  MIME_MP3("mp3", "audio/mpeg"),

  /**
   *
   */
  MIME_MP4("mp4", "video/mp4"),

  /**
   *
   */
  MIME_MPC("mpc", "chemical/x-mopac-input"),

  /**
   *
   */
  MIME_MPEGA("mpega", "audio/mpeg"),

  /**
   *
   */
  MIME_MPEG("mpeg", "video/mpeg"),

  /**
   *
   */
  MIME_MPE("mpe", "video/mpeg"),

  /**
   *
   */
  MIME_MPGA("mpga", "audio/mpeg"),

  /**
   *
   */
  MIME_MPG("mpg", "video/mpeg"),

  /**
   *
   */
  MIME_MPH("mph", "application/x-comsol"),

  /**
   *
   */
  MIME_MPV("mpv", "video/x-matroska"),

  /**
   *
   */
  MIME_MS("ms", "application/x-troff-ms"),

  /**
   *
   */
  MIME_MSH("msh", "model/mesh"),

  /**
   *
   */
  MIME_MSI("msi", "application/x-msi"),

  /**
   *
   */
  MIME_MVB("mvb", "chemical/x-mopac-vib"),

  /**
   *
   */
  MIME_MXF("mxf", "application/mxf"),

  /**
   *
   */
  MIME_MXU("mxu", "video/vnd.mpegurl"),

  /**
   *
   */
  MIME_NB("nb", "application/mathematica"),

  /**
   *
   */
  MIME_NBP("nbp", "application/mathematica"),

  /**
   *
   */
  MIME_NC("nc", "application/x-netcdf"),

  /**
   *
   */
  MIME_NEF("nef", "image/x-nikon-nef"),

  /**
   *
   */
  MIME_NWC("nwc", "application/x-nwc"),

  /**
   *
   */
  MIME_O("o", "application/x-object"),

  /**
   *
   */
  MIME_ODA("oda", "application/oda"),

  /**
   *
   */
  MIME_ODB("odb", "application/vnd.oasis.opendocument.database"),

  /**
   *
   */
  MIME_ODC("odc", "application/vnd.oasis.opendocument.chart"),

  /**
   *
   */
  MIME_ODF("odf", "application/vnd.oasis.opendocument.formula"),

  /**
   *
   */
  MIME_ODG("odg", "application/vnd.oasis.opendocument.graphics"),

  /**
   *
   */
  MIME_ODI("odi", "application/vnd.oasis.opendocument.image"),

  /**
   *
   */
  MIME_ODM("odm", "application/vnd.oasis.opendocument.text-master"),

  /**
   *
   */
  MIME_ODP("odp", "application/vnd.oasis.opendocument.presentation"),

  /**
   *
   */
  MIME_ODS("ods", "application/vnd.oasis.opendocument.spreadsheet"),

  /**
   *
   */
  MIME_ODT("odt", "application/vnd.oasis.opendocument.text"),

  /**
   *
   */
  MIME_OGA("oga", "audio/ogg"),

  /**
   *
   */
  MIME_OGG("ogg", "audio/ogg"),

  /**
   *
   */
  MIME_OGV("ogv", "video/ogg"),

  /**
   *
   */
  MIME_OGX("ogx", "application/ogg"),

  /**
   *
   */
  MIME_OLD("old", "application/x-trash"),

  /**
   *
   */
  MIME_ONE("one", "application/onenote"),

  /**
   *
   */
  MIME_ONEPKG("onepkg", "application/onenote"),

  /**
   *
   */
  MIME_ONETMP("onetmp", "application/onenote"),

  /**
   *
   */
  MIME_ONETOC2("onetoc2", "application/onenote"),

  /**
   *
   */
  MIME_OPUS("opus", "audio/ogg"),

  /**
   *
   */
  MIME_ORC("orc", "audio/csound"),

  /**
   *
   */
  MIME_ORF("orf", "image/x-olympus-orf"),

  /**
   *
   */
  MIME_OTG("otg", "application/vnd.oasis.opendocument.graphics-template"),

  /**
   *
   */
  MIME_OTH("oth", "application/vnd.oasis.opendocument.text-web"),

  /**
   *
   */
  MIME_OTP("otp", "application/vnd.oasis.opendocument.presentation-template"),

  /**
   *
   */
  MIME_OTS("ots", "application/vnd.oasis.opendocument.spreadsheet-template"),

  /**
   *
   */
  MIME_OTT("ott", "application/vnd.oasis.opendocument.text-template"),

  /**
   *
   */
  MIME_OZA("oza", "application/x-oz-application"),

  /**
   *
   */
  MIME_P7R("p7r", "application/x-pkcs7-certreqresp"),

  /**
   *
   */
  MIME_PAC("pac", "application/x-ns-proxy-autoconfig"),

  /**
   *
   */
  MIME_PAS("pas", "text/x-pascal"),

  /**
   *
   */
  MIME_PATCH("patch", "text/x-diff"),

  /**
   *
   */
  MIME_PAT("pat", "image/x-coreldrawpattern"),

  /**
   *
   */
  MIME_PBM("pbm", "image/x-portable-bitmap"),

  /**
   *
   */
  MIME_PCAP("pcap", "application/vnd.tcpdump.pcap"),

  /**
   *
   */
  MIME_PCF("pcf", "application/x-font"),

  /**
   *
   */
  MIME_PCX("pcx", "image/pcx"),

  /**
   *
   */
  MIME_PDB("pdb", "chemical/x-pdb"),

  /**
   *
   */
  MIME_PDF("pdf", "application/pdf"),

  /**
   *
   */
  MIME_PFA("pfa", "application/x-font"),

  /**
   *
   */
  MIME_PFB("pfb", "application/x-font"),

  /**
   *
   */
  MIME_PGM("pgm", "image/x-portable-graymap"),

  /**
   *
   */
  MIME_PGN("pgn", "application/x-chess-pgn"),

  /**
   *
   */
  MIME_PGP("pgp", "application/pgp-encrypted"),

  /**
   *
   */
  MIME_PK("pk", "application/x-tex-pk"),

  /**
   *
   */
  MIME_PLS("pls", "audio/x-scpls"),

  /**
   *
   */
  MIME_PL("pl", "text/x-perl"),

  /**
   *
   */
  MIME_PM("pm", "text/x-perl"),

  /**
   *
   */
  MIME_PNG("png", "image/png"),

  /**
   *
   */
  MIME_PNM("pnm", "image/x-portable-anymap"),

  /**
   *
   */
  MIME_POTM("potm", "application/vnd.ms-powerpoint.template.macroEnabled.12"),

  /**
   *
   */
  MIME_POT("pot", "text/plain"),

  /**
   *
   */
  MIME_POTX("potx", "application/vnd.openxmlformats-officedocument.presentationml.template"),

  /**
   *
   */
  MIME_PPAM("ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12"),

  /**
   *
   */
  MIME_PPM("ppm", "image/x-portable-pixmap"),

  /**
   *
   */
  MIME_PPS("pps", "application/vnd.ms-powerpoint"),

  /**
   *
   */
  MIME_PPSM("ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12"),

  /**
   *
   */
  MIME_PPSX("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow"),

  /**
   *
   */
  MIME_PPT("ppt", "application/vnd.ms-powerpoint"),

  /**
   *
   */
  MIME_PPTM("pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12"),

  /**
   *
   */
  MIME_PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),

  /**
   *
   */
  MIME_PRF("prf", "application/pics-rules"),

  /**
   *
   */
  MIME_PRT("prt", "chemical/x-ncbi-asn1-ascii"),

  /**
   *
   */
  MIME_PS("ps", "application/postscript"),

  /**
   *
   */
  MIME_PSD("psd", "image/x-photoshop"),

  /**
   *
   */
  MIME_P("p", "text/x-pascal"),

  /**
   *
   */
  MIME_PYC("pyc", "application/x-python-code"),

  /**
   *
   */
  MIME_PYO("pyo", "application/x-python-code"),

  /**
   *
   */
  MIME_PY("py", "text/x-python"),

  /**
   *
   */
  MIME_QGS("qgs", "application/x-qgis"),

  /**
   *
   */
  MIME_QTL("qtl", "application/x-quicktimeplayer"),

  /**
   *
   */
  MIME_QT("qt", "video/quicktime"),

  /**
   *
   */
  MIME_RA("ra", "audio/x-pn-realaudio"),

  /**
   *
   */
  MIME_RA1("ra", "audio/x-realaudio"),

  /**
   *
   */
  MIME_RAM("ram", "audio/x-pn-realaudio"),

  /**
   *
   */
  MIME_RAR("rar", "application/rar"),

  /**
   *
   */
  MIME_RAS("ras", "image/x-cmu-raster"),

  /**
   *
   */
  MIME_RB("rb", "application/x-ruby"),

  /**
   *
   */
  MIME_RD("rd", "chemical/x-mdl-rdfile"),

  /**
   *
   */
  MIME_RDF("rdf", "application/rdf+xml"),

  /**
   *
   */
  MIME_RDP("rdp", "application/x-rdp"),

  /**
   *
   */
  MIME_RGB("rgb", "image/x-rgb"),

  /**
   *
   */
  MIME_RM("rm", "audio/x-pn-realaudio"),

  /**
   *
   */
  MIME_ROFF("roff", "application/x-troff"),

  /**
   *
   */
  MIME_ROS("ros", "chemical/x-rosdal"),

  /**
   *
   */
  MIME_RPM("rpm", "application/x-redhat-package-manager"),

  /**
   *
   */
  MIME_RSS("rss", "application/x-rss+xml"),

  /**
   *
   */
  MIME_RTF("rtf", "application/rtf"),

  /**
   *
   */
  MIME_RTX("rtx", "text/richtext"),

  /**
   *
   */
  MIME_RXN("rxn", "chemical/x-mdl-rxnfile"),

  /**
   *
   */
  MIME_SCALA("scala", "text/x-scala"),

  /**
   *
   */
  MIME_SCE("sce", "application/x-scilab"),

  /**
   *
   */
  MIME_SCI("sci", "application/x-scilab"),

  /**
   *
   */
  MIME_SCO("sco", "audio/csound"),

  /**
   *
   */
  MIME_SCR("scr", "application/x-silverlight"),

  /**
   *
   */
  MIME_SCT("sct", "text/scriptlet"),

  /**
   *
   */
  MIME_SD2("sd2", "audio/x-sd2"),

  /**
   *
   */
  MIME_SDA("sda", "application/vnd.stardivision.draw"),

  /**
   *
   */
  MIME_SDC("sdc", "application/vnd.stardivision.calc"),

  /**
   *
   */
  MIME_SD("sd", "chemical/x-mdl-sdfile"),

  /**
   *
   */
  MIME_SDD("sdd", "application/vnd.stardivision.impress"),

  /**
   *
   */
  MIME_SDF("sdf", "application/vnd.stardivision.math"),

  /**
   *
   */
  MIME_SDF1("sdf", "chemical/x-mdl-sdfile"),

  /**
   *
   */
  MIME_SDS("sds", "application/vnd.stardivision.chart"),

  /**
   *
   */
  MIME_SDW("sdw", "application/vnd.stardivision.writer"),

  /**
   *
   */
  MIME_SER("ser", "application/java-serialized-object"),

  /**
   *
   */
  MIME_SFV("sfv", "text/x-sfv"),

  /**
   *
   */
  MIME_SGF("sgf", "application/x-go-sgf"),

  /**
   *
   */
  MIME_SGL("sgl", "application/vnd.stardivision.writer-global"),

  /**
   *
   */
  MIME_SHA1("sha1", "application/x-sha1"),

  /**
   *
   */
  MIME_SH("sh", "application/x-sh"),

  /**
   *
   */
  MIME_SHAR("shar", "application/x-shar"),

  /**
   *
   */
  MIME_SHP("shp", "application/x-qgis"),

  /**
   *
   */
  MIME_SH1("sh", "text/x-sh"),

  /**
   *
   */
  MIME_SHTML("shtml", "text/html"),

  /**
   *
   */
  MIME_SHX("shx", "application/x-qgis"),

  /**
   *
   */
  MIME_SID("sid", "audio/prs.sid"),

  /**
   *
   */
  MIME_SIG("sig", "application/pgp-signature"),

  /**
   *
   */
  MIME_SIK("sik", "application/x-trash"),

  /**
   *
   */
  MIME_SILO("silo", "model/mesh"),

  /**
   *
   */
  MIME_SIS("sis", "application/vnd.symbian.install"),

  /**
   *
   */
  MIME_SISX("sisx", "x-epoc/x-sisx-app"),

  /**
   *
   */
  MIME_SIT("sit", "application/x-stuffit"),

  /**
   *
   */
  MIME_SITX("sitx", "application/x-stuffit"),

  /**
   *
   */
  MIME_SKD("skd", "application/x-koan"),

  /**
   *
   */
  MIME_SKM("skm", "application/x-koan"),

  /**
   *
   */
  MIME_SKP("skp", "application/x-koan"),

  /**
   *
   */
  MIME_SKT("skt", "application/x-koan"),

  /**
   *
   */
  MIME_SLDM("sldm", "application/vnd.ms-powerpoint.slide.macroEnabled.12"),

  /**
   *
   */
  MIME_SLDX("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide"),

  /**
   *
   */
  MIME_SMI("smi", "application/smil+xml"),

  /**
   *
   */
  MIME_SMIL("smil", "application/smil+xml"),

  /**
   *
   */
  MIME_SND("snd", "audio/basic"),

  /**
   *
   */
  MIME_SPC("spc", "chemical/x-galactic-spc"),

  /**
   *
   */
  MIME_SPL("spl", "application/futuresplash"),

  /**
   *
   */
  MIME_SPL1("spl", "application/x-futuresplash"),

  /**
   *
   */
  MIME_SPX("spx", "audio/ogg"),

  /**
   *
   */
  MIME_SQL("sql", "application/x-sql"),

  /**
   *
   */
  MIME_SRC("src", "application/x-wais-source"),

  /**
   *
   */
  MIME_SRT("srt", "text/plain"),

  /**
   *
   */
  MIME_STC("stc", "application/vnd.sun.xml.calc.template"),

  /**
   *
   */
  MIME_STD("std", "application/vnd.sun.xml.draw.template"),

  /**
   *
   */
  MIME_STI("sti", "application/vnd.sun.xml.impress.template"),

  /**
   *
   */
  MIME_STL("stl", "application/sla"),

  /**
   *
   */
  MIME_STW("stw", "application/vnd.sun.xml.writer.template"),

  /**
   *
   */
  MIME_STY("sty", "text/x-tex"),

  /**
   *
   */
  MIME_SV4CPIO("sv4cpio", "application/x-sv4cpio"),

  /**
   *
   */
  MIME_SV4CRC("sv4crc", "application/x-sv4crc"),

  /**
   *
   */
  MIME_SVG("svg", "image/svg+xml"),

  /**
   *
   */
  MIME_SVGZ("svgz", "image/svg+xml"),

  /**
   *
   */
  MIME_SW("sw", "chemical/x-swissprot"),

  /**
   *
   */
  MIME_SWF("swf", "application/x-shockwave-flash"),

  /**
   *
   */
  MIME_SWFL("swfl", "application/x-shockwave-flash"),

  /**
   *
   */
  MIME_SXC("sxc", "application/vnd.sun.xml.calc"),

  /**
   *
   */
  MIME_SXD("sxd", "application/vnd.sun.xml.draw"),

  /**
   *
   */
  MIME_SXG("sxg", "application/vnd.sun.xml.writer.global"),

  /**
   *
   */
  MIME_SXI("sxi", "application/vnd.sun.xml.impress"),

  /**
   *
   */
  MIME_SXM("sxm", "application/vnd.sun.xml.math"),

  /**
   *
   */
  MIME_SXW("sxw", "application/vnd.sun.xml.writer"),

  /**
   *
   */
  MIME_T("t", "application/x-troff"),

  /**
   *
   */
  MIME_TAR("tar", "application/x-tar"),

  /**
   *
   */
  MIME_TAZ("taz", "application/x-gtar-compressed"),

  /**
   *
   */
  MIME_TCL("tcl", "application/x-tcl"),

  /**
   *
   */
  MIME_TCL1("tcl", "text/x-tcl"),

  /**
   *
   */
  MIME_TEXI("texi", "application/x-texinfo"),

  /**
   *
   */
  MIME_TEXINFO("texinfo", "application/x-texinfo"),

  /**
   *
   */
  MIME_TEX("tex", "text/x-tex"),

  /**
   *
   */
  MIME_TEXT("text", "text/plain"),

  /**
   *
   */
  MIME_TGF("tgf", "chemical/x-mdl-tgf"),

  /**
   *
   */
  MIME_TGZ("tgz", "application/x-gtar-compressed"),

  /**
   *
   */
  MIME_THMX("thmx", "application/vnd.ms-officetheme"),

  /**
   *
   */
  MIME_TIFF("tiff", "image/tiff"),

  /**
   *
   */
  MIME_TIF("tif", "image/tiff"),

  /**
   *
   */
  MIME_TK("tk", "text/x-tcl"),

  /**
   *
   */
  MIME_TM("tm", "text/texmacs"),

  /**
   *
   */
  MIME_TORRENT("torrent", "application/x-bittorrent"),

  /**
   *
   */
  MIME_TR("tr", "application/x-troff"),

  /**
   *
   */
  MIME_TSP("tsp", "application/dsptype"),

  /**
   *
   */
  MIME_TS("ts", "video/MP2T"),

  /**
   *
   */
  MIME_TSV("tsv", "text/tab-separated-values"),

  /**
   *
   */
  MIME_TTL("ttl", "text/turtle"),

  /**
   *
   */
  MIME_TXT("txt", "text/plain"),

  /**
   *
   */
  MIME_UDEB("udeb", "application/x-debian-package"),

  /**
   *
   */
  MIME_ULS("uls", "text/iuls"),

  /**
   *
   */
  MIME_USTAR("ustar", "application/x-ustar"),

  /**
   *
   */
  MIME_VAL("val", "chemical/x-ncbi-asn1-binary"),

  /**
   *
   */
  MIME_VCD("vcd", "application/x-cdlink"),

  /**
   *
   */
  MIME_VCF("vcf", "text/x-vcard"),

  /**
   *
   */
  MIME_VCS("vcs", "text/x-vcalendar"),

  /**
   *
   */
  MIME_VMD("vmd", "chemical/x-vmd"),

  /**
   *
   */
  MIME_VMS("vms", "chemical/x-vamas-iso14976"),

  /**
   *
   */
  MIME_VRML("vrml", "model/vrml"),

  /**
   *
   */
  MIME_VRML1("vrml", "x-world/x-vrml"),

  /**
   *
   */
  MIME_VRM("vrm", "x-world/x-vrml"),

  /**
   *
   */
  MIME_VSD("vsd", "application/vnd.visio"),

  /**
   *
   */
  MIME_WAD("wad", "application/x-doom"),

  /**
   *
   */
  MIME_WAV("wav", "audio/x-wav"),

  /**
   *
   */
  MIME_WAX("wax", "audio/x-ms-wax"),

  /**
   *
   */
  MIME_WBMP("wbmp", "image/vnd.wap.wbmp"),

  /**
   *
   */
  MIME_WBXML("wbxml", "application/vnd.wap.wbxml"),

  /**
   *
   */
  MIME_WEBM("webm", "video/webm"),

  /**
   *
   */
  MIME_WK("wk", "application/x-123"),

  /**
   *
   */
  MIME_WMA("wma", "audio/x-ms-wma"),

  /**
   *
   */
  MIME_WMD("wmd", "application/x-ms-wmd"),

  /**
   *
   */
  MIME_WMLC("wmlc", "application/vnd.wap.wmlc"),

  /**
   *
   */
  MIME_WMLSC("wmlsc", "application/vnd.wap.wmlscriptc"),

  /**
   *
   */
  MIME_WMLS("wmls", "text/vnd.wap.wmlscript"),

  /**
   *
   */
  MIME_WML("wml", "text/vnd.wap.wml"),

  /**
   *
   */
  MIME_WM("wm", "video/x-ms-wm"),

  /**
   *
   */
  MIME_WMV("wmv", "video/x-ms-wmv"),

  /**
   *
   */
  MIME_WMX("wmx", "video/x-ms-wmx"),

  /**
   *
   */
  MIME_WMZ("wmz", "application/x-ms-wmz"),

  /**
   *
   */
  MIME_WOFF("woff", "application/x-font-woff"),

  /**
   *
   */
  MIME_WP5("wp5", "application/vnd.wordperfect5.1"),

  /**
   *
   */
  MIME_WPD("wpd", "application/vnd.wordperfect"),

  /**
   *
   */
  MIME_WRL("wrl", "model/vrml"),

  /**
   *
   */
  MIME_WRL1("wrl", "x-world/x-vrml"),

  /**
   *
   */
  MIME_WSC("wsc", "text/scriptlet"),

  /**
   *
   */
  MIME_WVX("wvx", "video/x-ms-wvx"),

  /**
   *
   */
  MIME_WZ("wz", "application/x-wingz"),

  /**
   *
   */
  MIME_X3DB("x3db", "model/x3d+binary"),

  /**
   *
   */
  MIME_X3D("x3d", "model/x3d+xml"),

  /**
   *
   */
  MIME_X3DV("x3dv", "model/x3d+vrml"),

  /**
   *
   */
  MIME_XBM("xbm", "image/x-xbitmap"),

  /**
   *
   */
  MIME_XCF("xcf", "application/x-xcf"),

  /**
   *
   */
  MIME_XCOS("xcos", "application/x-scilab-xcos"),

  /**
   *
   */
  MIME_XHT("xht", "application/xhtml+xml"),

  /**
   *
   */
  MIME_XHTML("xhtml", "application/xhtml+xml"),

  /**
   *
   */
  MIME_XLAM("xlam", "application/vnd.ms-excel.addin.macroEnabled.12"),

  /**
   *
   */
  MIME_XLB("xlb", "application/vnd.ms-excel"),

  /**
   *
   */
  MIME_XLS("xls", "application/vnd.ms-excel"),

  /**
   *
   */
  MIME_XLSB("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12"),

  /**
   *
   */
  MIME_XLSM("xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12"),

  /**
   *
   */
  MIME_XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),

  /**
   *
   */
  MIME_XLT("xlt", "application/vnd.ms-excel"),

  /**
   *
   */
  MIME_XLTM("xltm", "application/vnd.ms-excel.template.macroEnabled.12"),

  /**
   *
   */
  MIME_XLTX("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template"),

  /**
   *
   */
  MIME_XML("xml", "application/xml"),

  /**
   *
   */
  MIME_XML1("xml", "text/xml"),

  /**
   *
   */
  MIME_XPI("xpi", "application/x-xpinstall"),

  /**
   *
   */
  MIME_XPM("xpm", "image/x-xpixmap"),

  /**
   *
   */
  MIME_XSD("xsd", "application/xsd"),

  /**
   *
   */
  MIME_XSL("xsl", "application/xslt+xml"),

  /**
   *
   */
  MIME_XSLT("xslt", "application/xslt+xml"),

  /**
   *
   */
  MIME_XSPF("xspf", "application/xspf+xml"),

  /**
   *
   */
  MIME_XTEL("xtel", "chemical/x-xtel"),

  /**
   *
   */
  MIME_XUL("xul", "application/vnd.mozilla.xul+xml"),

  /**
   *
   */
  MIME_XWD("xwd", "image/x-xwindowdump"),

  /**
   *
   */
  MIME_XYZ("xyz", "chemical/x-xyz"),

  /**
   *
   */
  MIME_ZIP("zip", "application/zip"),
  
  /**
   *
   */
  MIME_GZIP("gzip", "application/gzip"),

  /**
   *
   */
  MIME_ZMT("zmt", "chemical/x-mopac-input");

  private final String mstrMimeType;
  private final String mstrSuffix;

  MimeValues(String suffix, String mimeType) {
    this.mstrMimeType = mimeType;
    this.mstrSuffix = suffix;
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
        break;
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

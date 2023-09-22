package si.vsrs.cif.laurentius.plugin.eodlozisce.exception;

import si.laurentius.commons.ebms.EBMSErrorCodeInterface;

public enum EOdlozisceErrorCode implements EBMSErrorCodeInterface {


    IgnoredAlreadyReceivedMessage("SVEV:0201", "IgnoredAlreadyReceivedMessage", "warning",
            "Processing", "Message was already received in receivers duplicate detection time window! " +
            "According to receivers settings duplicate is eliminated!",
            "reliability"),


    ReceiverNotExists("SVEV:0202", "Receiver address not exists", "failure",
            "Content", "Message receiver not exists!",
            "application"),

    ServerError("SVEV:0250", "Receiver address not exists", "failure",
            "Content", "Message receiver not exists!",
            "application");


    String code;
    String name;
    String severity;
    String category;
    String description;
    String origin;

    EOdlozisceErrorCode(String cd, String nm, String sv, String ct, String desc, String org) {
        code = cd;
        name = nm;
        severity = sv;
        category = ct;
        description = desc;
        origin = org;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSeverity() {
        return severity;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

}

package si.vsrs.cif.filing.lookups.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldOfLawType {
    @JsonProperty("sif")
    String code;
    @JsonProperty("naziv")
    String name;
    @JsonProperty("defVpisnik")
    String defRegisterType;


    public FieldOfLawType() {
    }

    public FieldOfLawType(String code, String name, String defRegisterType) {
        this.code = code;
        this.name = name;
        this.defRegisterType = defRegisterType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefRegisterType() {
        return defRegisterType;
    }

    public void setDefRegisterType(String defRegisterType) {
        this.defRegisterType = defRegisterType;
    }
}

package si.vsrs.cif.laurentius.plugin.eodlozisce.codes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterType {
    @JsonProperty("vpisnik")
    String id;
    @JsonProperty("vpisnikKratica")
    String code;
    @JsonProperty("naziv")
    String name;
    @JsonProperty("aplikSif")
    String applikCode;
    @JsonProperty("pristojnaSodisca")
    List<String> courts;

    public RegisterType() {
    }

    public RegisterType(String id, String code, String name, String applikCode) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.applikCode = applikCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getApplikCode() {
        return applikCode;
    }

    public void setApplikCode(String applikCode) {
        this.applikCode = applikCode;
    }

    public List<String> getCourts() {
        if (courts==null){
            courts = new ArrayList<>();
        }
        return courts;
    }
}

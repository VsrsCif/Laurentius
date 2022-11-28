package si.vsrs.cif.filing.lookups.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Lookup entity for the court codes and names
 */

public class CourtType {

    @JsonProperty("sodiSif")
    String code;
    @JsonProperty("naziv")
    String name;

    public CourtType() {
    }

    public CourtType(String code, String name) {
        this.code = code;
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourtType courtCode = (CourtType) o;
        return Objects.equals(code, courtCode.code) && Objects.equals(name, courtCode.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }
}

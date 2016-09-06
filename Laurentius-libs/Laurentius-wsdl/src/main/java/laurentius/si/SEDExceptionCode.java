
package laurentius.si;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SEDExceptionCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SEDExceptionCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="PModeNotExists"/>
 *     &lt;enumeration value="RequiredDataNotExists"/>
 *     &lt;enumeration value="InvalidPMode"/>
 *     &lt;enumeration value="InvalidData"/>
 *     &lt;enumeration value="MissingData"/>
 *     &lt;enumeration value="ServerError"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SEDExceptionCode")
@XmlEnum
public enum SEDExceptionCode {

    @XmlEnumValue("PModeNotExists")
    P_MODE_NOT_EXISTS("PModeNotExists"),
    @XmlEnumValue("RequiredDataNotExists")
    REQUIRED_DATA_NOT_EXISTS("RequiredDataNotExists"),
    @XmlEnumValue("InvalidPMode")
    INVALID_P_MODE("InvalidPMode"),
    @XmlEnumValue("InvalidData")
    INVALID_DATA("InvalidData"),
    @XmlEnumValue("MissingData")
    MISSING_DATA("MissingData"),
    @XmlEnumValue("ServerError")
    SERVER_ERROR("ServerError");
    private final String value;

    SEDExceptionCode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SEDExceptionCode fromValue(String v) {
        for (SEDExceptionCode c: SEDExceptionCode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

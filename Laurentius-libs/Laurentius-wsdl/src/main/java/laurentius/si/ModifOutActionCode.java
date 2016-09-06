
package laurentius.si;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ModifOutActionCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ModifOutActionCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;maxLength value="20"/>
 *     &lt;enumeration value="ABORT"/>
 *     &lt;enumeration value="DELETE"/>
 *     &lt;enumeration value="RESEND"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ModifOutActionCode")
@XmlEnum
public enum ModifOutActionCode {

    ABORT,
    DELETE,
    RESEND;

    public String value() {
        return name();
    }

    public static ModifOutActionCode fromValue(String v) {
        return valueOf(v);
    }

}

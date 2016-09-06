
package si.laurentius;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ModifyActionCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ModifyActionCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;maxLength value="20"/>
 *     &lt;enumeration value="ACCEPT"/>
 *     &lt;enumeration value="LOCK"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ModifyActionCode")
@XmlEnum
public enum ModifyActionCode {

    ACCEPT,
    LOCK;

    public String value() {
        return name();
    }

    public static ModifyActionCode fromValue(String v) {
        return valueOf(v);
    }

}

package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public interface ValidationStage<T> {
    ValidationResult validate(T data) throws ParserConfigurationException, IOException, SAXException, TransformerException;
}

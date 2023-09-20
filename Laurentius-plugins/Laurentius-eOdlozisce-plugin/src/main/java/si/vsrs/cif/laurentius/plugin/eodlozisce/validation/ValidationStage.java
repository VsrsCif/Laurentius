package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import java.io.InputStream;

public interface ValidationStage {
    ValidationResult validate(InputStream data);
}

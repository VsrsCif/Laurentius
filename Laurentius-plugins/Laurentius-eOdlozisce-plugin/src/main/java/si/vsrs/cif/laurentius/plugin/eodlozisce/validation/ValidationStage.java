package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

public interface ValidationStage<T> {
    ValidationResult validate(T data);
}

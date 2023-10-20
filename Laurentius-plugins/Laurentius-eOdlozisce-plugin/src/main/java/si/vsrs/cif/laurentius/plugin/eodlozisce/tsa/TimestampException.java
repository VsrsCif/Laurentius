package si.vsrs.cif.laurentius.plugin.eodlozisce.tsa;

public class TimestampException extends Throwable {
    public TimestampException(final String errorMessage) {
        super(errorMessage);
    }

    public TimestampException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

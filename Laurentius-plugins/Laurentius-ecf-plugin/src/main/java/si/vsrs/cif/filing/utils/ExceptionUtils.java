package si.vsrs.cif.filing.utils;

import si.laurentius.commons.utils.SEDLogger;
import si.vsrs.cif.filing.enums.EFCError;
import si.vsrs.cif.filing.exception.ECFFault;

import static java.lang.String.format;

public class ExceptionUtils {
    private static final SEDLogger LOG = new SEDLogger(ExceptionUtils.class);

    public static void throwFault(String messageId, EFCError ecfError, String... messageArguments) {
        if (messageArguments == null && ecfError.getArgumentCount() > 0 ||
                messageArguments != null && ecfError.getArgumentCount() != messageArguments.length) {
            LOG.logWarn("Template argument count [" + ecfError + "] and provided argument count mismatch!", null);
        }

        String msgError = format(ecfError.getErrorTemplate(), messageArguments);
        LOG.logWarn(msgError, null);
        throw new ECFFault(ecfError.getEcfFaultCode(), messageId,
                msgError,
                ecfError.getSoapFault());
    }
}

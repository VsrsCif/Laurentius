package si.vsrs.cif.filing.utils;

import org.apache.commons.lang3.StringUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.vsrs.cif.filing.ECFInInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MimeTypeUtils {
    private static final SEDLogger LOG = new SEDLogger(ECFInInterceptor.class);

    public static List<MimeValue> convertStringListToMimeList(String value) {
        if (StringUtils.isBlank(value)) {
            Collections.emptyList();
        }
        return Stream.of(value.trim().split("\\s*,\\s*")).map(MimeTypeUtils::convertStringToMimeValue).filter(mimeValue -> mimeValue != null)
                .collect(Collectors.toList());
    }

    /**
     * Method returns MimeType Object for mimetype. If no mimetype is found null is returned
     *
     * @param strMimeType
     * @return suffix
     */
    public static MimeValue convertStringToMimeValue(String strMimeType) {
        if (StringUtils.isBlank(strMimeType)) {
            return null;
        }
        Optional<MimeValue> optRes = Stream.of(MimeValue.values())
                .filter(mime -> StringUtils.equalsIgnoreCase(StringUtils.trim(strMimeType), mime.getMimeType())).findFirst();
        if (optRes.isPresent()) {
            return optRes.get();
        }
        LOG.logWarn("Failed to parse mimetype list. Unknown mimetype [" + strMimeType + "]", null);
        return null;
    }

    /**
     * Methods validates if part mimetype matches one of the values in the list.
     * If mimeValues list is null or empty then true is returned by default.
     * If part's mimetype is blank or it can not be parsed, then false is return by default.
     *
     *
     *
     * @param part
     * @param mimeValues
     * @return
     */
    public static boolean matchMimeTypes(MSHInPart part, List<MimeValue> mimeValues) {
        if (mimeValues == null || mimeValues.isEmpty()) {
            return true;
        }
        if (part == null || StringUtils.isBlank(part.getMimeType())) {
            return false;
        }
        MimeValue partMimeType = convertStringToMimeValue(part.getMimeType());
        if (partMimeType == null ) {
            return false;
        }

        return mimeValues.contains(partMimeType);
    }

    public static List<MSHInPart> filterPayloadsByMimeTypesAndName(List<MSHInPart> payloads, String name, boolean strictNameValidation, List<MimeValue> mimeValues) {

        List<MSHInPart> filteredMimeTypes = filterPayloadsByMimeTypes(payloads, mimeValues);
        if (filteredMimeTypes.isEmpty()) {
            LOG.formatedlog("No payloads with mimetype [%s]", mimeValues);
            return Collections.emptyList();
        }

        if (StringUtils.isBlank(name)) {
            return filteredMimeTypes;
        }

        if (!strictNameValidation && filteredMimeTypes.size() == 1) {
            LOG.formatedlog("Only one payload with mimetype [%s]. ", mimeValues);
            return filteredMimeTypes;
        }

        LOG.formatedlog("Filter payloads by name [%s]. ", name);
        return payloads.stream().filter(part -> StringUtils.equalsIgnoreCase(part.getName(), name))
                .collect(Collectors.toList());
    }

    public static List<MSHInPart> filterPayloadsByMimeTypes(List<MSHInPart> payloads, List<MimeValue> mimeValues) {
        return payloads.stream().filter(part -> MimeTypeUtils.matchMimeTypes(part, mimeValues))
                .collect(Collectors.toList());
    }

}

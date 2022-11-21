package si.vsrs.cif.filing.lookups;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.vsrs.cif.filing.ECFSystemProperties;
import si.vsrs.cif.filing.lookups.data.CourtType;
import si.vsrs.cif.filing.lookups.data.FieldOfLawType;
import si.vsrs.cif.filing.lookups.data.RegisterType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static si.vsrs.cif.filing.ECFSystemProperties.*;

public class ECFLookups {
    private static final ThreadLocal<ObjectMapper> JSON_MAPPER = ThreadLocal.withInitial(() -> new ObjectMapper());


    private static final SEDLogger LOG = new SEDLogger(ECFLookups.class);
    List<CourtType> courtCodes;
    List<RegisterType> registerTypes;
    List<FieldOfLawType> fieldOfLawTypes;
    boolean initLookups = false;


    public void initLookups() {
        try {
            if (!initLookups) {
                courtCodes = initData(ECF_FILENAME_COURTS, new TypeReference<List<CourtType>>() {
                });
                registerTypes = initData(ECF_FILENAME_REGISTER, new TypeReference<List<RegisterType>>() {
                });
                fieldOfLawTypes = initData(ECF_FILENAME_FIEDS_OF_LAW, new TypeReference<List<FieldOfLawType>>() {
                });
                initLookups = true;
            }
        } catch (IOException e) {
            LOG.logError("Error occurred while initialize ecf lookups: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    public <T> List<T> initData(String fileName, TypeReference<List<T>> reference) throws IOException {
        File dataFile = ECFSystemProperties.getDataFile(fileName);
        if (!dataFile.exists()) {
            LOG.log("Create init data [" + fileName + "] to folder [" + dataFile.getAbsolutePath() + "]");
            Files.copy(ECFLookups.class.getResourceAsStream("/data/" + fileName),
                    dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return JSON_MAPPER.get().readValue(dataFile, reference);
    }

    public List<RegisterType> getRegisterTypes() {
        initLookups();
        return registerTypes == null ? Collections.emptyList() : registerTypes;
    }

    public List<FieldOfLawType> getFieldOfLawTypes() {
        initLookups();
        return fieldOfLawTypes == null ? Collections.emptyList() : fieldOfLawTypes;
    }

    public List<CourtType> getCourtCodes() {
        initLookups();
        return courtCodes == null ? Collections.emptyList() : courtCodes;
    }

    public Optional<CourtType> getCourtByCode(String code) {
        return getCourtCodes().stream().filter(entity ->
                StringUtils.equalsIgnoreCase(code, entity.getCode())).findFirst();
    }

    public Optional<RegisterType> getRegisterByCode(String code) {
        return getRegisterTypes().stream().filter(entity ->
                StringUtils.equalsIgnoreCase(code, entity.getCode())).findFirst();
    }

    public Optional<RegisterType> getRegisterById(String id) {
        return getRegisterTypes().stream().filter(entity ->
                StringUtils.equalsIgnoreCase(id, entity.getId())).findFirst();
    }

    public Optional<FieldOfLawType> getFieldOfLawByCode(String code) {
        return getFieldOfLawTypes().stream().filter(entity ->
                StringUtils.equalsIgnoreCase(code, entity.getCode())).findFirst();
    }
}

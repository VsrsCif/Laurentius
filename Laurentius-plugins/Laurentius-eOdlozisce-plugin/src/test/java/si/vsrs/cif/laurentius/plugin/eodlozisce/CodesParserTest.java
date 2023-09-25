package si.vsrs.cif.laurentius.plugin.eodlozisce;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.CourtType;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.FieldOfLawType;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.RegisterType;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CodesParserTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void parseCourtTypes() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<CourtType> courtTypes = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("codes/sifrant-sodisca.json"), new TypeReference<List<CourtType>>() {
        });
        Assert.assertTrue(courtTypes.size() > 0);
        List<CourtType> filtered = courtTypes.stream().filter(f -> f.getCode().equals("S01")).collect(Collectors.toList());
        Assert.assertTrue(filtered.size() == 1);
    }

    @Test
    public void parseRegisterTypes() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<RegisterType> courtTypes = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("codes/sifrant-vpisniki.json"), new TypeReference<List<RegisterType>>() {
        });
        Assert.assertTrue(courtTypes.size() > 0);
        List<RegisterType> filtered = courtTypes.stream().filter(f -> f.getId().equals("762")).collect(Collectors.toList());
        Assert.assertTrue(filtered.size() == 1);
    }

    @Test
    public void parseFieldOfLawTypes() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<FieldOfLawType> courtTypes = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("codes/sifrant-pravna-podrocja.json"), new TypeReference<List<FieldOfLawType>>() {
        });
        Assert.assertTrue(courtTypes.size() > 0);
        List<FieldOfLawType> filtered = courtTypes.stream().filter(f -> f.getCode().equals("I")).collect(Collectors.toList());
        Assert.assertTrue(filtered.size() == 1);
    }
}

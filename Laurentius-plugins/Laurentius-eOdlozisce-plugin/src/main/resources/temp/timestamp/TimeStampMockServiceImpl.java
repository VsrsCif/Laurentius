package si.sodisce.splosnaVloga.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import si.sodisce.splosnaVloga.service.contract.TimeStampService;

@Service
@ConditionalOnProperty(value = "mock.timestamp", havingValue = "true")
public class TimeStampMockServiceImpl implements TimeStampService {

    private static final Logger logger = LoggerFactory.getLogger(TimeStampMockServiceImpl.class);

    @Override
    public String timeStampXml(String xml) {
        logger.debug("TimeStampServiceImpl.timeStampXml");
        return xml;
    }
}

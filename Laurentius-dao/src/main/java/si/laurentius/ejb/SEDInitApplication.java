package si.laurentius.ejb;

import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDSchedulerInterface;
import si.laurentius.commons.utils.SEDLogger;

import javax.annotation.PostConstruct;
import javax.ejb.*;

@Startup
@Singleton
@DependsOn("SEDInitData")
@Local
@TransactionManagement(TransactionManagementType.BEAN)
public class SEDInitApplication {

    static final SEDLogger LOG = new SEDLogger(SEDInitApplication.class);

    @EJB(mappedName = SEDJNDI.JNDI_SEDSCHEDLER)
    SEDSchedulerInterface mshScheduler;

    @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
    private SEDLookupsInterface mdbLookups;

    @PostConstruct
    void init() {
        // activate cronTasks
        LOG.log("Initialize application.");
        mdbLookups.getSEDCronJobs().forEach(cronJob -> {
            if (cronJob.getActive() != null && cronJob.getActive()) {
                mshScheduler.activateCronJob(cronJob);
            }
        });
    }

}

package si.laurentius.ejb;

import generated.SedLookups;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import si.laurentius.application.SEDApplication;
import si.laurentius.cert.SEDCertPassword;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.ebox.SEDBox;
import si.laurentius.ejb.db.MockUserTransaction;
import si.laurentius.ejb.utils.InitialContextFactoryForTest;
import si.laurentius.ejb.utils.TestLookupUtils;
import si.laurentius.ejb.utils.TestUtils;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.process.SEDProcessor;
import si.laurentius.property.SEDProperty;
import si.laurentius.user.SEDUser;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.TypedQuery;
import javax.transaction.*;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static si.laurentius.ejb.utils.AssertEntities.assertValuesEquals;

public class SEDInitDataIntegrationTest extends TestUtils {

    static SEDInitData mTestInstance = new SEDInitData();

    @BeforeClass
    public static void setUpClass() throws IOException, NamingException, JMSException {

        // ---------------------------------
        // set logger
        setLogger(SEDInitDataIntegrationTest.class.getSimpleName());

        // create initial context factory
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitialContextFactoryForTest.class.getName());

        mTestInstance.memEManager = TestUtils.createEntityManager();
        mTestInstance.mutUTransaction = new MockUserTransaction(mTestInstance.memEManager.getTransaction());

        System.setProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN, LAU_TEST_DOMAIN);

        setUpStorage("target/storage/SEDInitDataIntegrationTest");


    }

    @Test
    public void testGetDatabaseObjects_SEDBox() throws Exception  {
        // given
        SEDBox entity = TestLookupUtils.createSEDBox();
        persist(entity);
        // when
        List<SEDBox> lst = mTestInstance.getDatabaseObjects(SEDBox.class);
        //then
        assertFalse(lst.isEmpty());
        Optional<SEDBox> opt = lst.stream().filter(sedBox -> sedBox.getLocalBoxName().equals(entity.getLocalBoxName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testGetDatabaseObjects_SEDUser() throws Exception  {
        // given
        SEDUser entity = TestLookupUtils.createSEDUser(false);
        SEDBox box = TestLookupUtils.createSEDBox();
        persist(box);
        entity.getSEDBoxes().add(box);
        persist(entity);
        // when
        List<SEDUser> lst = mTestInstance.getDatabaseObjects(SEDUser.class);
        //then
        Optional<SEDUser> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getName(), entity.getName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }
    @Test
    public void testGetDatabaseObjects_SEDApplication() throws Exception  {
        // given
        SEDApplication entity = TestLookupUtils.createSEDApplication(false);
        SEDBox box = TestLookupUtils.createSEDBox();
        persist(box);
        entity.getSEDBoxes().add(box);
        persist(entity);
        // when
        List<SEDApplication> lst = mTestInstance.getDatabaseObjects(SEDApplication.class);
        //then;
        Optional<SEDApplication> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getName(), entity.getName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testGetDatabaseObjects_SEDCronJob() throws Exception  {
        // given
        SEDCronJob entity = TestLookupUtils.createSEDCronJob();
        persist(entity);
        // when
        List<SEDCronJob> lst = mTestInstance.getDatabaseObjects(SEDCronJob.class);
        //then
        assertFalse(lst.isEmpty());
        Optional<SEDCronJob> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getName(), entity.getName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }


    @Test
    public void testGetDatabaseObjects_SEDInterceptor() throws Exception  {
        // given
        SEDInterceptor entity = TestLookupUtils.createSEDInterceptor();
        persist(entity);
        // when
        List<SEDInterceptor> lst = mTestInstance.getDatabaseObjects(SEDInterceptor.class);
        //then
        assertFalse(lst.isEmpty());
        Optional<SEDInterceptor> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getName(), entity.getName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testGetDatabaseObjects_SEDProcessor() throws Exception  {
        // given
        SEDProcessor entity = TestLookupUtils.createSEDProcessor();
        persist(entity);
        // when
        List<SEDProcessor> lst = mTestInstance.getDatabaseObjects(SEDProcessor.class);
        //then
        assertFalse(lst.isEmpty());
        Optional<SEDProcessor> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getName(), entity.getName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testGetDatabaseObjects_SEDCertPassword() throws Exception  {
        // given
        SEDCertPassword entity = TestLookupUtils.createSEDCertPassword();
        persist(entity);
        // when
        List<SEDCertPassword> lst = mTestInstance.getDatabaseObjects(SEDCertPassword.class);
        //then
        assertFalse(lst.isEmpty());
        Optional<SEDCertPassword> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getAlias(), entity.getAlias())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testGetDatabaseObjects_SEDProperty() throws Exception  {
        // given
        SEDProperty entity = TestLookupUtils.createSEDProperty();
        persist(entity);
        // when
        List<SEDProperty> lst = mTestInstance.getDatabaseObjects(SEDProperty.class);
        //then
        assertFalse(lst.isEmpty());
        Optional<SEDProperty> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getKey(), entity.getKey())
                && Objects.equals(listEntity.getGroup(), entity.getGroup())
            ).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testInitLookups_SEDBox(){
        // given
        SEDBox entity = TestLookupUtils.createSEDBox();
        SedLookups initData = new SedLookups();
        initData.setSEDBoxes(new SedLookups.SEDBoxes());
        initData.getSEDBoxes().getSEDBoxes().add(entity);
        // when
        mTestInstance.initLookups(initData);
        // then
        List<SEDBox> lst = getAllObjects(SEDBox.class);
        assertFalse(lst.isEmpty());
        Optional<SEDBox> opt = lst.stream().filter(sedBox -> sedBox.getLocalBoxName().equals(entity.getLocalBoxName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testInitLookups_SEDUser() throws Exception  {
        // given
        SEDUser entity = TestLookupUtils.createSEDUser(false);
        SEDBox box = TestLookupUtils.createSEDBox();
        entity.getSEDBoxes().add(box);

        SedLookups initData = new SedLookups();
        initData.setSEDBoxes(new SedLookups.SEDBoxes());
        initData.setSEDUsers(new SedLookups.SEDUsers());
        initData.getSEDBoxes().getSEDBoxes().add(box);
        initData.getSEDUsers().getSEDUsers().add(entity);
        // when
        mTestInstance.initLookups(initData);

        //then
        List<SEDUser> lst =getAllObjects(SEDUser.class);
        Optional<SEDUser> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getName(), entity.getName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testInitLookups_SEDApplication()   {
        // given
        SEDApplication entity = TestLookupUtils.createSEDApplication(false);
        SEDBox box = TestLookupUtils.createSEDBox();
        entity.getSEDBoxes().add(box);
        SedLookups initData = new SedLookups();
        initData.setSEDBoxes(new SedLookups.SEDBoxes());
        initData.setSEDApplications(new SedLookups.SEDApplications());
        initData.getSEDBoxes().getSEDBoxes().add(box);
        initData.getSEDApplications().getSEDApplications().add(entity);
        // when
        mTestInstance.initLookups(initData);
        //then;
        List<SEDApplication> lst = getAllObjects(SEDApplication.class);
        Optional<SEDApplication> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getName(), entity.getName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testInitLookups_SEDCronJob_NotActivated()  {
        // given
        SEDCronJob entity = TestLookupUtils.createSEDCronJob();
        entity.setActive(false); // dp npt activate just check the db entry
        SedLookups initData = new SedLookups();
        initData.setSEDCronJobs(new SedLookups.SEDCronJobs());
        initData.getSEDCronJobs().getSEDCronJobs().add(entity);
        // when
        mTestInstance.initLookups(initData);
        //then
        List<SEDCronJob> lst = getAllObjects(SEDCronJob.class);
        assertFalse(lst.isEmpty());
        Optional<SEDCronJob> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getName(), entity.getName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }


    @Test
    public void testInitLookups_SEDInterceptor()  {
        // given
        SEDInterceptor entity = TestLookupUtils.createSEDInterceptor();
        SedLookups initData = new SedLookups();
        initData.setSEDInterceptors(new SedLookups.SEDInterceptors());
        initData.getSEDInterceptors().getSEDInterceptors().add(entity);
        // when
        mTestInstance.initLookups(initData);
        //then
        List<SEDInterceptor> lst = getAllObjects(SEDInterceptor.class);
        assertFalse(lst.isEmpty());
        Optional<SEDInterceptor> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getName(), entity.getName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testInitLookups_SEDProcessor()  {
        // given
        SEDProcessor entity = TestLookupUtils.createSEDProcessor();
        SedLookups initData = new SedLookups();
        initData.setSEDProcessors(new SedLookups.SEDProcessors());
        initData.getSEDProcessors().getSEDProcessors().add(entity);
        // when
        mTestInstance.initLookups(initData);
        //then
        List<SEDProcessor> lst = getAllObjects(SEDProcessor.class);
        assertFalse(lst.isEmpty());
        Optional<SEDProcessor> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getName(), entity.getName())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testInitLookups_SEDCertPassword()  {
        // given
        SEDCertPassword entity = TestLookupUtils.createSEDCertPassword();
        SedLookups initData = new SedLookups();
        initData.setSEDCertPassword(new SedLookups.SEDCertPassword());
        initData.getSEDCertPassword().getSEDCertPasswords().add(entity);
        // when
        mTestInstance.initLookups(initData);
        //then
        List<SEDCertPassword> lst = getAllObjects(SEDCertPassword.class);
        assertFalse(lst.isEmpty());
        Optional<SEDCertPassword> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getAlias(), entity.getAlias())).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }

    @Test
    public void testInitLookups_SEDProperty()   {
        // given
        SEDProperty entity = TestLookupUtils.createSEDProperty();
        SedLookups initData = new SedLookups();
        initData.setSEDProperties(new SedLookups.SEDProperties());
        initData.getSEDProperties().getSEDProperties().add(entity);
        // when
        mTestInstance.initLookups(initData);
        //then
        List<SEDProperty> lst = getAllObjects(SEDProperty.class);
        assertFalse(lst.isEmpty());
        Optional<SEDProperty> opt = lst.stream().filter(listEntity -> Objects.equals(listEntity.getKey(), entity.getKey())
                && Objects.equals(listEntity.getGroup(), entity.getGroup())
        ).findAny();
        assertTrue(opt.isPresent());
        assertValuesEquals(entity, opt.get());
    }


    public void persist(Object dbEntity) throws Exception {
        mTestInstance.mutUTransaction.begin();
        mTestInstance.memEManager.persist(dbEntity);
        mTestInstance.mutUTransaction.commit();
        //clear cache
        mTestInstance.memEManager.clear();
    }

    <T> List<T> getAllObjects(Class cls) {
        // clear cached values to make sure to retrieve them from database
        mTestInstance.memEManager.clear();
        TypedQuery<T> query = mTestInstance.memEManager.createNamedQuery(
                cls.getName() + ".getAll", cls);
        return query.getResultList();
    }
}
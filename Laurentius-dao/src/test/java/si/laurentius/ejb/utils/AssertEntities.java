package si.laurentius.ejb.utils;

import org.junit.Assert;
import si.laurentius.application.SEDApplication;
import si.laurentius.cert.SEDCertPassword;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTask;
import si.laurentius.cron.SEDTaskProperty;
import si.laurentius.ebox.SEDBox;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.interceptor.SEDInterceptorInstance;
import si.laurentius.interceptor.SEDInterceptorProperty;
import si.laurentius.process.SEDProcessor;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessorProperty;
import si.laurentius.process.SEDProcessorRule;
import si.laurentius.property.SEDProperty;
import si.laurentius.user.SEDUser;

import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AssertEntities {

    public static void assertValuesEquals(SEDBox expected, SEDBox actual){
        assertEquals(expected.getLocalBoxName(), actual.getLocalBoxName());
        assertEquals(expected.getActiveFromDate(), actual.getActiveFromDate());
        assertEquals(expected.getActiveToDate(), actual.getActiveToDate());
    }

    public static void assertValuesEquals(SEDUser expected, SEDUser actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getDesc(), actual.getDesc());
        assertEquals(expected.getEmail(), actual.getEmail());
        // assertEquals(expected.getAdminRole(), actual.getAdminRole()); admin role is part of security configuration
        assertEquals(expected.getActiveFromDate(), actual.getActiveFromDate());
        assertEquals(expected.getActiveToDate(), actual.getActiveToDate());
        assertEquals(expected.getSEDBoxes().size(), actual.getSEDBoxes().size());
        for (SEDBox actualEBox : actual.getSEDBoxes()) {
            Optional<SEDBox> opt = expected.getSEDBoxes().stream().filter(sedBox -> sedBox.getLocalBoxName().equals(actualEBox.getLocalBoxName())).findAny();
            assertTrue("SEDBox [" + actualEBox.getLocalBoxName() + "] is not expected!", opt.isPresent());
            assertValuesEquals(opt.get(), actualEBox);
        }
    }

    public static void assertValuesEquals(SEDApplication expected, SEDApplication actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getApplicationId(), actual.getApplicationId());
        assertEquals(expected.getDesc(), actual.getDesc());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getActiveFromDate(), actual.getActiveFromDate());
        assertEquals(expected.getActiveToDate(), actual.getActiveToDate());
        assertEquals(expected.getSEDBoxes().size(), actual.getSEDBoxes().size());
        for (SEDBox actualEBox : actual.getSEDBoxes()) {
            Optional<SEDBox> opt = expected.getSEDBoxes().stream().filter(sedBox -> sedBox.getLocalBoxName().equals(actualEBox.getLocalBoxName())).findAny();
            assertTrue("SEDBox [" + actualEBox.getLocalBoxName() + "] is not expected!", opt.isPresent());
            assertValuesEquals(opt.get(), actualEBox);
        }
    }

    public static void assertValuesEquals(SEDCronJob expected, SEDCronJob actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getActive(), actual.getActive());
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
        assertEquals(expected.getDayOfWeek(), actual.getDayOfWeek());
        assertEquals(expected.getHour(), actual.getHour());
        assertEquals(expected.getIgnoreOnWorkFreeDays(), actual.getIgnoreOnWorkFreeDays());
        assertEquals(expected.getMinute(), actual.getMinute());
        assertEquals(expected.getMonth(), actual.getMonth());
        assertEquals(expected.getSecond(), actual.getSecond());

        assertEquals(expected.getSEDTasks().size(), actual.getSEDTasks().size());
        for (SEDTask actualEntity : actual.getSEDTasks()) {
            Optional<SEDTask> opt = expected.getSEDTasks().stream().filter(task ->
                    Objects.equals(actualEntity.getName(),task.getName()) && Objects.equals(actualEntity.getPlugin(),task.getPlugin()) ).findAny();
            assertTrue("SEDTask [" + actualEntity.getName() + "] is not expected!", opt.isPresent());
            assertValuesEquals(opt.get(), actualEntity);
        }
    }

    public static void assertValuesEquals(SEDTask expected, SEDTask actual) {
        assertEquals(expected.getActive(), actual.getActive());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPlugin(), actual.getPlugin());
        assertEquals(expected.getSEDTaskProperties().size(), actual.getSEDTaskProperties().size());

        for (SEDTaskProperty actualEntity : actual.getSEDTaskProperties()) {
            Optional<SEDTaskProperty> opt = expected.getSEDTaskProperties().stream().filter(entity ->
                    Objects.equals(actualEntity.getKey(),entity.getKey())).findAny();
            assertTrue("SEDTaskProperty [" + actualEntity.getKey() + "] is not expected!", opt.isPresent());
            assertValuesEquals(opt.get(), actualEntity);
        }

        for (SEDTaskProperty expectedEntity : expected.getSEDTaskProperties()) {
            Optional<SEDTaskProperty> opt = actual.getSEDTaskProperties().stream().filter(entity ->
                    Objects.equals(expectedEntity.getKey(),entity.getKey())).findAny();
            assertTrue("SEDTaskProperty [" + expectedEntity.getKey() + "] is not present!", opt.isPresent());
            assertValuesEquals(opt.get(), expectedEntity);
        }
    }

    public static void assertValuesEquals(SEDTaskProperty expected, SEDTaskProperty actual) {
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getValue(), actual.getValue());
    }

    public static void assertValuesEquals(SEDInterceptor expected, SEDInterceptor actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getActive(), actual.getActive());
        assertEquals(expected.getInterceptEvent(), actual.getInterceptEvent());
        assertEquals(expected.getInterceptRole(), actual.getInterceptRole());

        assertValuesEquals(expected.getSEDInterceptorInstance(), actual.getSEDInterceptorInstance());
    }
    public static void assertValuesEquals(SEDInterceptorInstance expected, SEDInterceptorInstance actual) {
        assertEquals(expected.getPlugin(), actual.getPlugin());
        assertEquals(expected.getPluginVersion(), actual.getPluginVersion());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getSEDInterceptorProperties().size(), actual.getSEDInterceptorProperties().size());

        for (SEDInterceptorProperty actualEntity : actual.getSEDInterceptorProperties()) {
            Optional<SEDInterceptorProperty> opt = expected.getSEDInterceptorProperties().stream().filter(entity ->
                    Objects.equals(actualEntity.getKey(),entity.getKey())).findAny();
            assertTrue("SEDInterceptorProperties [" + actualEntity.getKey() + "] is not expected!", opt.isPresent());
            assertValuesEquals(opt.get(), actualEntity);
        }
    }

    public static void assertValuesEquals(SEDInterceptorProperty expected, SEDInterceptorProperty actual) {
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getValue(), actual.getValue());
    }

    public static void assertValuesEquals(SEDProcessor expected, SEDProcessor actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getActive(), actual.getActive());
        assertEquals(expected.getDeliveredOnSuccess(), actual.getDeliveredOnSuccess());


        assertEquals(expected.getSEDProcessorRules().size(), actual.getSEDProcessorRules().size());
        for (SEDProcessorRule actualRule : actual.getSEDProcessorRules()) {
            Optional<SEDProcessorRule> opt = expected.getSEDProcessorRules().stream().filter(rule ->
                    Objects.equals(actualRule.getProperty(),rule.getProperty())
                            && Objects.equals(actualRule.getPredicate(),rule.getPredicate())
                            &&  Objects.equals(actualRule.getValue(),rule.getValue())).findAny();
            assertTrue("SEDProcessorRule [" + actualRule.getProperty() + "],[" + actualRule.getPredicate() + "],[" + actualRule.getValue() + "] is not expected!", opt.isPresent());
        }

        assertEquals(expected.getSEDProcessorInstances().size(), actual.getSEDProcessorInstances().size());
        for (SEDProcessorInstance actualInstance : actual.getSEDProcessorInstances()) {
            Optional<SEDProcessorInstance> opt = expected.getSEDProcessorInstances().stream().filter(instance ->
                    Objects.equals(actualInstance.getPlugin(),instance.getPlugin())
                            && Objects.equals(actualInstance.getType(),actualInstance.getType())
                           ).findAny();
            assertTrue("SEDProcessorInstance [" + actualInstance.getPlugin() + "],[" + actualInstance.getType() + "] is not expected!", opt.isPresent());
            assertValuesEquals(opt.get(), actualInstance);
        }
    }

    public static void assertValuesEquals(SEDProcessorInstance expected, SEDProcessorInstance actual) {
        assertEquals(expected.getPlugin(), actual.getPlugin());
        assertEquals(expected.getPluginVersion(), actual.getPluginVersion());
        assertEquals(expected.getType(), actual.getType());

        assertEquals(expected.getSEDProcessorProperties().size(), actual.getSEDProcessorProperties().size());

        for (SEDProcessorProperty actualEntity : actual.getSEDProcessorProperties()) {
            Optional<SEDProcessorProperty> opt = expected.getSEDProcessorProperties().stream().filter(entity ->
                    Objects.equals(actualEntity.getKey(),entity.getKey())).findAny();
            assertTrue("SEDProcessorProperty [" + actualEntity.getKey() + "] is not expected!", opt.isPresent());
            assertValuesEquals(opt.get(), actualEntity);
        }
    }

    public static void assertValuesEquals(SEDProcessorProperty expected, SEDProcessorProperty actual) {
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getValue(), actual.getValue());
    }

    public static void assertValuesEquals(SEDCertPassword expected, SEDCertPassword actual) {
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getPassword(), actual.getPassword());
        assertEquals(expected.isKeyPassword(), actual.isKeyPassword());
    }

    public static void assertValuesEquals(SEDProperty expected, SEDProperty actual) {
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getGroup(), actual.getGroup());
    }


}

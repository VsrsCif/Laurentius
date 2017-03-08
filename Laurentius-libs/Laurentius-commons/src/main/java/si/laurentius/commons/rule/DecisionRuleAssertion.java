/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.rule;

import si.laurentius.commons.enums.SEDRulePredicate;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.rule.SEDDecisionRule;

/**
 *
 * @author sluzba
 */
public class DecisionRuleAssertion {

  private static final SEDLogger LOG = new SEDLogger(DecisionRuleAssertion.class);

  public boolean assertRule(Object obj, SEDDecisionRule dr) {
    boolean bRes = false;
    Class cls = obj.getClass();
    try {
      Method m = cls.getMethod("get" + dr.getProperty());
      try {
        Class rt = m.getReturnType();
        Object val = m.invoke(obj);
        if (val == null) {
          bRes = false;
        } else if (val instanceof Integer) {
          bRes = assertInteger(SEDRulePredicate.getByValue(dr.getPredicate()),
                  Integer.getInteger(dr.getValue()), (Integer) val);
        } else if (val instanceof String) {
          bRes = assertString(SEDRulePredicate.getByValue(dr.getPredicate()),
                  dr.getValue(), (String) val);

        } else if (val instanceof BigInteger) {
          bRes = assertBigInteger(SEDRulePredicate.getByValue(dr.getPredicate()),
                  new BigInteger(dr.getValue()), (BigInteger) val);
        } else if (val instanceof Float) {
          bRes = assertFloat(SEDRulePredicate.getByValue(dr.getPredicate()),
                  Float.valueOf(dr.getValue()), (Float) val);
        } else if (val instanceof Date) {
          // todo !!!
          /* bRes = assertDate(SEDRulePredicate.getByValue(dr.getPredicate()),
                  Float.valueOf(dr.getValue()), (Date) val);  */

        } else {
          LOG.formatedWarning(
                  "Object: %s has unknown return type %s for property %s",
                  obj.getClass().getName(), rt.getClass(), dr.getProperty());
        }

      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        LOG.formatedWarning("Object: %s does not contain property %s",
                obj.getClass().getName(), dr.getProperty(), ex);
      }

      // get property type
    } catch (NoSuchMethodException | SecurityException ex) {
      LOG.formatedWarning("Object: %s does not contain property %s",
              obj.getClass().getName(), dr.getProperty(), ex);
    }

    return bRes;
  }

  boolean assertInteger(SEDRulePredicate pred, Integer expected, Integer value) {
    switch (pred) {
      case EQUALS:
        return value.equals(expected);
      case NOT_EQUALS:
        return !value.equals(expected);
      case GREATER_THAN:
        return value.compareTo(expected) > 0;
      case GREATER_THAN_EQUALS:
        return value.compareTo(expected) >= 0;
      case LESS_THAN:
        return value.compareTo(expected) < 0;
      case LESS_THAN_EQUALS:
        return value.compareTo(expected) <= 0;
    }
    return false;
  }

  boolean assertDate(SEDRulePredicate pred, Date expected, Date value) {
    switch (pred) {
      case EQUALS:
        return value.equals(expected);
      case NOT_EQUALS:
        return !value.equals(expected);
      case GREATER_THAN:
        return value.compareTo(expected) > 0;
      case GREATER_THAN_EQUALS:
        return value.compareTo(expected) >= 0;
      case LESS_THAN:
        return value.compareTo(expected) < 0;
      case LESS_THAN_EQUALS:
        return value.compareTo(expected) <= 0;
    }
    return false;
  }

  boolean assertBigInteger(SEDRulePredicate pred, BigInteger expected,
          BigInteger value) {
    switch (pred) {
      case EQUALS:
        return value.equals(expected);
      case NOT_EQUALS:
        return !value.equals(expected);
      case GREATER_THAN:
        return value.compareTo(expected) > 0;
      case GREATER_THAN_EQUALS:
        return value.compareTo(expected) >= 0;
      case LESS_THAN:
        return value.compareTo(expected) < 0;
      case LESS_THAN_EQUALS:
        return value.compareTo(expected) <= 0;
    }
    return false;
  }

  boolean assertFloat(SEDRulePredicate pred, Float expected, Float value) {
    switch (pred) {
      case EQUALS:
        return value.equals(expected);
      case NOT_EQUALS:
        return !value.equals(expected);
      case GREATER_THAN:
        return value.compareTo(expected) > 0;
      case GREATER_THAN_EQUALS:
        return value.compareTo(expected) >= 0;
      case LESS_THAN:
        return value.compareTo(expected) < 0;
      case LESS_THAN_EQUALS:
        return value.compareTo(expected) <= 0;
    }
    return false;
  }

  boolean assertString(SEDRulePredicate pred, String expected, String value) {
    switch (pred) {
      case EQUALS:
        return value.equals(expected);
      case NOT_EQUALS:
        return !value.equals(expected);
      case STARTS_WITH:
        return value.startsWith(value);
      case ENDS_WITH:
        return value.endsWith(expected);
      case CONTAINS:
        return value.contains(expected);
      case NOT_CONTAIN:
        return !value.contains(expected);
      case IN:
        return expected.contains(value);
      case NOT_IN:
        return !expected.contains(value);
      case IN_LIST:
        return Arrays.asList(expected.split(";")).contains(value);
      case NOT_IN_LIST:
        return !Arrays.asList(expected.split(";")).contains(value);
    }
    return false;
  }

}

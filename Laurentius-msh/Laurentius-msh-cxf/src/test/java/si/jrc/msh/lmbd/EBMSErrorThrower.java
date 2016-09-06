/*
 * Code was inspired by blog: Rafał Borowiec 
 * http://blog.codeleak.pl/2014/07/junit-testing-exception-with-java-8-and-lambda-expressions.html
 */
package si.jrc.msh.lmbd;

import org.apache.cxf.interceptor.Fault;

/**
 *
 * @author Joze Rihtarsic
 */

@FunctionalInterface 
public interface EBMSErrorThrower {
      void throwFault() throws Fault;
}
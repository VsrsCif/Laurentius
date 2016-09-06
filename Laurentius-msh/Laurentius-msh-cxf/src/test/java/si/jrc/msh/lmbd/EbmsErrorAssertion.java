/*
 * Code was inspired by blog: Rafał Borowiec 
 * http://blog.codeleak.pl/2014/07/junit-testing-exception-with-java-8-and-lambda-expressions.html
 */
package si.jrc.msh.lmbd;


import static org.hamcrest.CoreMatchers.*;
import org.junit.Assert;
import si.jrc.msh.exception.EBMSError;
import si.jrc.msh.exception.EBMSErrorCode;
import static org.junit.Assert.*;

/**
 *
 * @author Joze Rihtarsic
 */
public class EbmsErrorAssertion {

    public static EbmsErrorAssertion assertFault(EBMSErrorThrower ft) {
        try {
            ft.throwFault();
        }catch(EBMSError eb) {
          return new EbmsErrorAssertion(eb);
        }
        catch (Throwable c) {
            throw new AssertionErrorBadInstanceThrown(c);
        }
        throw new AssertionErrorNotThrown();
    }

    private final Throwable fault;

    public EbmsErrorAssertion(Throwable t) {
        this.fault = t;
    }

  
    public EbmsErrorAssertion assertEBMSCode(EBMSErrorCode ec) {
        assertEquals(ec, ((EBMSError)this.fault).getEbmsErrorCode() );
        return this;
    }

    public EbmsErrorAssertion assertSubMessageContainsString(String expectedMessage) {
        Assert.assertThat(((EBMSError)this.fault).getSubMessage(),
            containsString(expectedMessage));
        return this;
    }

   
}
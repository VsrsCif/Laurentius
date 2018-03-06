
package si.laurentius.utils;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class Utils {
  protected static final String PERSISTENCE_UNIT_NAME = "ebMS_LAU_PU";
  
  public static void main(String...args){
    System.out.println("test");
    
    
    EntityManagerFactory  memfMSHFactory = Persistence.createEntityManagerFactory(
              PERSISTENCE_UNIT_NAME);
    
  }
  
}

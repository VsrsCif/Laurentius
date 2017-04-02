/*
 * Copyright 2015, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.commons.exception;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class PModeException extends Throwable{

  /**
   *
   * @param message
   */
  public PModeException(String message) {
    super(message);
  }

  /**
   *
   * @param message
   * @param cause
   */
  public PModeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   *
   * @param cause
   */
  public PModeException(Throwable cause) {
    super(cause);
  }

}

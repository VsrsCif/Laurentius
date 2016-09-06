/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package si.jrc.msh.client.sec;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class MSHKeyPasswordCallback implements CallbackHandler {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(MSHKeyPasswordCallback.class);

  SEDCertificate mKey;

  /**
   * Method sets Callback for expected key
   *
   * @param key
   */
  public MSHKeyPasswordCallback(SEDCertificate key) {
    this.mKey = key;
  }

  @Override
  public void handle(Callback[] callbacks)
      throws UnsupportedCallbackException {
    long l = LOG.logStart();

    for (Callback cb : callbacks) {
      if (cb instanceof WSPasswordCallback) {
        WSPasswordCallback pc = (WSPasswordCallback) cb;
        if (pc.getIdentifier() == null) {
          String msg = "Missing key identifier, check ws-securiy configuration";
          throw new UnsupportedCallbackException(cb, msg);
        }
        if (!pc.getIdentifier().equals(mKey.getAlias()) ) {
          String msg = "Key identifier: '"+pc.getIdentifier()+"' not match expected alias '"+mKey.getAlias()+"'";
          throw new UnsupportedCallbackException(cb, msg);
        }
        pc.setPassword(mKey.getKeyPassword());
      } else {
        String msg = "UnsupportedCallback for class: " + cb.getClass();
        throw new UnsupportedCallbackException(cb, msg);
      }
    }
    LOG.logEnd(l);
  }
}

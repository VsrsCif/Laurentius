/*
 * Copyright 2016, Supreme Court Republic of Slovenia
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
package si.laurentius.lce.tls;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
 
import javax.net.ssl.X509KeyManager;
 /**
 * XMLTimeStamp interface
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class X509KeyManagerForAlias implements X509KeyManager{
 
    private final X509KeyManager mkmKeyManager;
    private final String mstrKeyAlias;
 
    public X509KeyManagerForAlias(X509KeyManager keyManager, String alias)
    {
        this.mkmKeyManager=keyManager;        
        this.mstrKeyAlias = alias;
 
    }
 
    @Override
    public String chooseClientAlias(String[] keyTypes, Principal[] issuers,
            Socket socket) {   
        for (String kt: keyTypes) {
            String[] validAliases=mkmKeyManager.getClientAliases(kt, issuers);            
            if (validAliases!=null) {
                for (String als:  validAliases) {
                    if (als.equals(mstrKeyAlias)){
                      return mstrKeyAlias;
                    }
                }
            }
        }
        return null;
    }
 
 
    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers,
            Socket socket){
        return mkmKeyManager.chooseServerAlias(keyType, issuers, socket);
    }
 
    @Override
    public X509Certificate[] getCertificateChain(String alias){
        return mkmKeyManager.getCertificateChain(alias);
    }
 
    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return mkmKeyManager.getClientAliases(keyType, issuers);
    }
 
    @Override
    public PrivateKey getPrivateKey(String alias){
 
        return mkmKeyManager.getPrivateKey(alias);
    }
 
    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers){
        return mkmKeyManager.getServerAliases(keyType, issuers);
    }
 
}
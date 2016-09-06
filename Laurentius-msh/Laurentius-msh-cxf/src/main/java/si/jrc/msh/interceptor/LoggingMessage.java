package si.jrc.msh.interceptor;
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.io.File;

public final class LoggingMessage {

    
    private final StringBuilder address;
    private final StringBuilder contentType;
    private final StringBuilder encoding;
    private final StringBuilder httpMethod;
    private final StringBuilder header;
    private final StringBuilder message;
    private final StringBuilder responseCode;
    private final File fileOut;
    

    public LoggingMessage(File fo, String hd) {
        fileOut = fo;

        contentType = new StringBuilder();
        address = new StringBuilder();
        encoding = new StringBuilder();
        httpMethod = new StringBuilder();
        header = new StringBuilder(hd);
        message = new StringBuilder();        
        responseCode = new StringBuilder();
        
    }
    
   
    
    public StringBuilder getAddress() {
        return address;
    }

    public StringBuilder getEncoding() {
        return encoding;
    }

    public StringBuilder getHeader() {
        return header;
    }
    
    public StringBuilder getHttpMethod() {
        return httpMethod;
    }

    public StringBuilder getContentType() {
        return contentType;
    }

    public StringBuilder getMessage() {
        return message;
    }

 

    public StringBuilder getResponseCode() {
        return responseCode;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

         buffer.append("Log message:\n--------------------------------------");
         if (header.length() > 0) {
            buffer.append("\n");
            buffer.append(header);
        }
        if (fileOut != null) {
            buffer.append("\nFile: ");
            buffer.append(fileOut.getAbsolutePath());
        }  
         
        if (address.length() > 0) {
            buffer.append("\nAddress: ");
            buffer.append(address);
        }
        if (responseCode.length() > 0) {
            buffer.append("\nResponse-Code: ");
            buffer.append(responseCode);
        }
        if (encoding.length() > 0) {
            buffer.append("\nEncoding: ");
            buffer.append(encoding);
        }
        if (httpMethod.length() > 0) {
            buffer.append("\nHttp-Method: ");
            buffer.append(httpMethod);
        }
        buffer.append("\nContent-Type: ");
        buffer.append(contentType);
        buffer.append("\nHeaders: ");
        buffer.append(header);
        if (message.length() > 0) {
            buffer.append("\nMessages: ");
            buffer.append(message);
        }
       
        buffer.append("\n--------------------------------------");
        return buffer.toString();
    }
}
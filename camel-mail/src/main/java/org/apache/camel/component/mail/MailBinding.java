/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.mail;

import java.util.Map;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.camel.Exchange;

/**
 * A Strategy used to convert between a Camel {@Exchange} and {@Message} to and from a
 * Mail {@link MimeMessage}
 *
 * @version $Revision: 521240 $
 */
public class MailBinding {
    public void populateMailMessage(MailEndpoint endpoint, MimeMessage mimeMessage, Exchange exchange) {
        try {
            appendMailHeaders(mimeMessage, exchange.getIn());

            String destination = endpoint.getConfiguration().getDestination();
            if (destination != null ) {
                mimeMessage.setRecipients(Message.RecipientType.TO, destination);
            }
                        
            if (empty(mimeMessage.getFrom())) {
                // lets default the address to the endpoint destination
                String from = endpoint.getConfiguration().getFrom();
                mimeMessage.setFrom(new InternetAddress(from));
            }
            mimeMessage.setText(exchange.getIn().getBody(String.class));
        }
        catch (Exception e) {
            throw new RuntimeMailException("Failed to populate body due to: " + e + ". Exchange: " + exchange, e);
        }
    }

    protected boolean empty(Address[] addresses) {
        return addresses == null || addresses.length == 0;
    }

    /**
     * Extracts the body from the Mail message
     *
     * @param exchange
     * @param message
     */
    public Object extractBodyFromMail(MailExchange exchange, Message message) {
        try {
            return message.getContent();
        }
        catch (Exception e) {
            throw new RuntimeMailException("Failed to extract body due to: " + e + ". Message: " + message, e);
        }
    }

    /**
     * Appends the Mail headers from the Camel {@link MailMessage}
     */
    protected void appendMailHeaders(MimeMessage mimeMessage, org.apache.camel.Message camelMessage) throws MessagingException {
        Set<Map.Entry<String, Object>> entries = camelMessage.getHeaders().entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String headerName = entry.getKey();
            Object headerValue = entry.getValue();
            if (headerValue != null) {
                if (shouldOutputHeader(camelMessage, headerName, headerValue)) {
                	
            		String[] values = new String[]{};
            		Class stringArrayClazz = values.getClass();
            		
            		// Mail messages can repeat the same header...
            		if( headerValue.getClass() == stringArrayClazz ) {
            			mimeMessage.removeHeader(headerName);
                		values = (String[]) headerValue;
                		for (int i = 0; i < values.length; i++) {
                            mimeMessage.addHeader(headerName, values[i]);
						}
            		} else if( headerValue.getClass() == String.class ) {
                        mimeMessage.setHeader(headerName, (String) headerValue);
            		} else {
                		// Unknown type? then use toString()
                        mimeMessage.setHeader(headerName, headerValue.toString());
                	}
                }
            }
        }
    }

    /**
     * Strategy to allow filtering of headers which are put on the Mail message
     */
    protected boolean shouldOutputHeader(org.apache.camel.Message camelMessage, String headerName, Object headerValue) {
        return true;
    }
}

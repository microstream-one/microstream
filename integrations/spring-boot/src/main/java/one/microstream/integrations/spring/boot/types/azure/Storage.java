package one.microstream.integrations.spring.boot.types.azure;

/*-
 * #%L
 * microstream-integrations-spring-boot
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.springframework.boot.context.properties.NestedConfigurationProperty;

public class Storage
{

    @NestedConfigurationProperty
    private Credentials credentials;

    /**
     * Sets the blob service endpoint, additionally parses it for information (SAS token).
     */
    private String endpoint;

    /**
     * Sets the connection string to connect to the service.
     */
    private String connectionString;

    /**
     * Sets the encryption scope that is used to encrypt blob contents on the server.
     */
    private String encryptionScope;

    public Credentials getCredentials()
    {
        return this.credentials;
    }

    public void setCredentials(final Credentials credentials)
    {
        this.credentials = credentials;
    }

    public String getEndpoint()
    {
        return this.endpoint;
    }

    public void setEndpoint(final String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getConnectionString()
    {
        return this.connectionString;
    }

    public void setConnectionString(final String connectionString)
    {
        this.connectionString = connectionString;
    }

    public String getEncryptionScope()
    {
        return this.encryptionScope;
    }

    public void setEncryptionScope(final String encryptionScope)
    {
        this.encryptionScope = encryptionScope;
    }
}

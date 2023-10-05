package one.microstream.integrations.spring.boot.types.oraclecloud;

/*-
 * #%L
 * microstream-integrations-spring-boot3
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

public class ObjectStorage
{

    @NestedConfigurationProperty
    private ConfigFile configFile;

    @NestedConfigurationProperty
    private Client client;

    /**
     * Sets the region to call (ex, 'us-phoenix-1').
     */
    private String region;

    /**
     * Sets the endpoint to call (ex, https://www.example.com).
     */
    private String endpoint;

    public ConfigFile getConfigFile()
    {
        return this.configFile;
    }

    public void setConfigFile(final ConfigFile configFile)
    {
        this.configFile = configFile;
    }

    public Client getClient()
    {
        return this.client;
    }

    public void setClient(final Client client)
    {
        this.client = client;
    }

    public String getRegion()
    {
        return this.region;
    }

    public void setRegion(final String region)
    {
        this.region = region;
    }

    public String getEndpoint()
    {
        return this.endpoint;
    }

    public void setEndpoint(final String endpoint)
    {
        this.endpoint = endpoint;
    }
}

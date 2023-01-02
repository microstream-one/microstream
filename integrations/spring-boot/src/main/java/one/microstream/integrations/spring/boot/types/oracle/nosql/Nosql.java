package one.microstream.integrations.spring.boot.types.oracle.nosql;

/*-
 * #%L
 * microstream-spring
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
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

public class Nosql
{

    /**
     * The name of the KVStore. The store name is used to guard against accidental use of the wrong host or port.
     * The store name must consist entirely of upper or lower-case, letters and digits.
     */
    private String storeName;

    /**
     * Comma separated list containing the host and port of an active node in the KVStore. Each string has the format hostname:port.
     * It is good practice to pass multiple hosts so that if one host is down, the system will attempt to open the next one, and so on.
     */
    private String helperHosts;

    /**
     * Username used for authentication.
     */
    private String username;

    /**
     * Password used for authentication.
     */
    private String password;

    /**
     * Configures the default interval for checking on data definition operation progress.
     */
    private String checkInterval;

    /**
     * Configures the default read Consistency to be used when a Consistency is not specified for a particular read operation.
     * Supported values:
     * <ul>
     * <li>NONE_REQUIRED</li>
     * A consistency policy that lets a transaction on a replica using this policy proceed regardless of the state of the Replica
     * relative to the Master.
     *
     * <li>ABSOLUTE</li>
     * A consistency policy that requires that a transaction be serviced on the Master so that consistency is absolute.
     * </ul>
     */
    private String consistency;

    /**
     * Configures the default write Durability to be used when a Durability is not specified for a particular write operation.
     * Supported values:
     * <ul>
     * <li>COMMIT_SYNC</li>
     * A convenience constant that defines a durability policy with COMMIT_SYNC for Master commit synchronization.
     * The policies default to COMMIT_NO_SYNC for commits of replicated transactions that need acknowledgment
     * and SIMPLE_MAJORITY for the acknowledgment policy.
     *
     * <li>COMMIT_NO_SYNC</li>
     * A convenience constant that defines a durability policy with COMMIT_NO_SYNC for Master commit synchronization.
     * The policies default to COMMIT_NO_SYNC for commits of replicated transactions that need acknowledgment
     * and SIMPLE_MAJORITY for the acknowledgment policy.
     *
     * <li>COMMIT_WRITE_NO_SYNC</li>
     * A convenience constant that defines a durability policy with COMMIT_WRITE_NO_SYNC for Master commit synchronization.
     * The policies default to COMMIT_NO_SYNC for commits of replicated transactions that need acknowledgment
     * and SIMPLE_MAJORITY for the acknowledgment policy.
     * </ul>
     */
    private String durability;

    /**
     * Configures the chunk size associated with the chunks used to store a LOB.
     */
    private String lobChunkSize;

    /**
     * Configures the number of contiguous chunks that can be stored in the same partition for a given LOB.
     */
    private String lobChunksPerPartition;

    /**
     * Configures default timeout value associated with chunk access during operations on LOBs.
     */
    private String lobTimeout;

    /**
     * Configures the number of trailing bytes of a partial LOB that must be verified against the user supplied LOB stream
     * when resuming a putLOB operation. A value⇐0 disables verification.
     */
    private String lobVerificationBytes;

    /**
     * Set the number of times the client will attempt to check status for the execution of an asynchronous data
     * definition or administrative statement execution in the face of network connection problems.
     */
    private String maxCheckRetries;

    /**
     * Configures the amount of time to allow for a single round-trip network communication with the server.
     * This value is added to the request timeout to determine the total amount of time that the client should wait
     * for a request to complete before timing out.
     */
    private String networkRoundtripTimeout;

    /**
     * Comma separated lists of zones in which nodes must be located to be used for read operations.
     * If the argument is null, or this method has not been called, then read operations can be performed on nodes in any zone.
     */
    private String readZones;

    /**
     * Configures the connect/open timeout used when making RMI registry lookup requests.
     */
    private String registryOpenTimeout;

    /**
     * Configures the read timeout associated with sockets used to make RMI registry requests. Shorter timeouts
     * result in more rapid failure detection and recovery. However, this timeout should be sufficiently long to allow
     * for the longest timeout associated with a request.
     */
    private String registryReadTimeout;

    /**
     * Configures the default request timeout.
     */
    private String requestTimeout;

    /**
     * Sets the timeout of cached sequence generator attributes in milliseconds.
     */
    private String sgAttrsCacheTimeout;

    /**
     * Configures the open timeout used when establishing sockets used to make client requests. Shorter timeouts result
     * in more rapid failure detection and recovery. The default open timeout (3000 milliseconds) should be adequate for most applications.
     */
    private String socketOpenTimeout;

    /**
     * Configures the read timeout associated with the underlying sockets used to make client requests.
     * Shorter timeouts result in more rapid failure detection and recovery. However, this timeout should be sufficiently long
     * to allow for the longest timeout associated with a request.
     */
    private String socketReadTimeout;

    /**
     * Specifies whether calls to the store should use the async network protocol.
     */
    private String useAsync;

    /**
     * Configures security properties for the client. The supported properties include both authentication properties
     * and transport properties. See oracle.kv.KVSecurityConstants for details.
     */
    private String securityProperties;

    public String getStoreName()
    {
        return this.storeName;
    }

    public void setStoreName(final String storeName)
    {
        this.storeName = storeName;
    }

    public String getHelperHosts()
    {
        return this.helperHosts;
    }

    public void setHelperHosts(final String helperHosts)
    {
        this.helperHosts = helperHosts;
    }

    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }

    public String getCheckInterval()
    {
        return this.checkInterval;
    }

    public void setCheckInterval(final String checkInterval)
    {
        this.checkInterval = checkInterval;
    }

    public String getConsistency()
    {
        return this.consistency;
    }

    public void setConsistency(final String consistency)
    {
        this.consistency = consistency;
    }

    public String getDurability()
    {
        return this.durability;
    }

    public void setDurability(final String durability)
    {
        this.durability = durability;
    }

    public String getLobChunkSize()
    {
        return this.lobChunkSize;
    }

    public void setLobChunkSize(final String lobChunkSize)
    {
        this.lobChunkSize = lobChunkSize;
    }

    public String getLobChunksPerPartition()
    {
        return this.lobChunksPerPartition;
    }

    public void setLobChunksPerPartition(final String lobChunksPerPartition)
    {
        this.lobChunksPerPartition = lobChunksPerPartition;
    }

    public String getLobTimeout()
    {
        return this.lobTimeout;
    }

    public void setLobTimeout(final String lobTimeout)
    {
        this.lobTimeout = lobTimeout;
    }

    public String getLobVerificationBytes()
    {
        return this.lobVerificationBytes;
    }

    public void setLobVerificationBytes(final String lobVerificationBytes)
    {
        this.lobVerificationBytes = lobVerificationBytes;
    }

    public String getMaxCheckRetries()
    {
        return this.maxCheckRetries;
    }

    public void setMaxCheckRetries(final String maxCheckRetries)
    {
        this.maxCheckRetries = maxCheckRetries;
    }

    public String getNetworkRoundtripTimeout()
    {
        return this.networkRoundtripTimeout;
    }

    public void setNetworkRoundtripTimeout(final String networkRoundtripTimeout)
    {
        this.networkRoundtripTimeout = networkRoundtripTimeout;
    }

    public String getReadZones()
    {
        return this.readZones;
    }

    public void setReadZones(final String readZones)
    {
        this.readZones = readZones;
    }

    public String getRegistryOpenTimeout()
    {
        return this.registryOpenTimeout;
    }

    public void setRegistryOpenTimeout(final String registryOpenTimeout)
    {
        this.registryOpenTimeout = registryOpenTimeout;
    }

    public String getRegistryReadTimeout()
    {
        return this.registryReadTimeout;
    }

    public void setRegistryReadTimeout(final String registryReadTimeout)
    {
        this.registryReadTimeout = registryReadTimeout;
    }

    public String getRequestTimeout()
    {
        return this.requestTimeout;
    }

    public void setRequestTimeout(final String requestTimeout)
    {
        this.requestTimeout = requestTimeout;
    }

    public String getSgAttrsCacheTimeout()
    {
        return this.sgAttrsCacheTimeout;
    }

    public void setSgAttrsCacheTimeout(final String sgAttrsCacheTimeout)
    {
        this.sgAttrsCacheTimeout = sgAttrsCacheTimeout;
    }

    public String getSocketOpenTimeout()
    {
        return this.socketOpenTimeout;
    }

    public void setSocketOpenTimeout(final String socketOpenTimeout)
    {
        this.socketOpenTimeout = socketOpenTimeout;
    }

    public String getSocketReadTimeout()
    {
        return this.socketReadTimeout;
    }

    public void setSocketReadTimeout(final String socketReadTimeout)
    {
        this.socketReadTimeout = socketReadTimeout;
    }

    public String getUseAsync()
    {
        return this.useAsync;
    }

    public void setUseAsync(final String useAsync)
    {
        this.useAsync = useAsync;
    }

    public String getSecurityProperties()
    {
        return this.securityProperties;
    }

    public void setSecurityProperties(final String securityProperties)
    {
        this.securityProperties = securityProperties;
    }
}

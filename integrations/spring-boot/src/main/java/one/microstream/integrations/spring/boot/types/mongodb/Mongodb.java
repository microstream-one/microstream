package one.microstream.integrations.spring.boot.types.mongodb;

/*-
 * #%L
 * microstream-spring
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.util.Map;

public class Mongodb
{


    @NestedConfigurationProperty
    private Credentials credentials;

    /**
     * The database name for the storage target.
     */
    private String database;

    /**
     * Sets the logical name of the application using this MongoClient. The application name may be used by
     * the client to identify the application to the server, for use in server logs, slow query logs, and profile collection.
     */
    private String applicationName;

    /**
     * Sets the connection string to connect to the service.
     */
    private String connectionString;

    /**
     * The read concern level.
     */
    private String readConcern;

    /**
     * The preferred replica set members to which a query or command can be sent.
     */
    private String readPreference;

    /**
     * Controls the acknowledgment of write operations with various options.
     * <ul>
     * <li>w</li>
     * <ul>
     * <li>0: Don’t wait for acknowledgement from the server</li>
     *
     * <li>1: Wait for acknowledgement, but don’t wait for secondaries to replicate</li>
     *
     * <li>>=2: Wait for one or more secondaries to also acknowledge</li>
     *
     * <li>"majority": Wait for a majority of data bearing nodes to acknowledge</li>
     *
     * <li>"<tag set name>": Wait for one or more secondaries to also acknowledge based on a tag set name</li>
     * </ul>
     * <li>wtimeout - how long to wait for secondaries to acknowledge before failing</li>
     * <ul>
     * <li>0: indefinite</li>
     *
     * <li>>0: time to wait in milliseconds</li>
     * </ul>
     * <li>Other options:</li>
     * <p>
     * journal: If true block until write operations have been committed to the journal. Cannot be used in combination with fsync. Write operations will fail with an exception if this option is used when the server is running without journaling.
     * </ul>
     */
    private String writeConcern;

    /**
     * Sets whether reads should be retried if they fail due to a network error.
     */
    private String retryReads;

    /**
     * Sets whether writes should be retried if they fail due to a network error.
     */
    private String retryWrites;

    /**
     * The representation to use when converting a UUID to a BSON binary value.This class is necessary because
     * the different drivers used to have different ways of encoding UUID, with the BSON subtype: \x03 UUID old.
     */
    private String uuidRepresentation;

    /**
     * Further properties for the authentication mechanism.
     */
    private Map<String, String> authMechanismProperties;

    public Credentials getCredentials()
    {
        return credentials;
    }

    public void setCredentials(Credentials credentials)
    {
        this.credentials = credentials;
    }

    public String getDatabase()
    {
        return database;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public String getApplicationName()
    {
        return applicationName;
    }

    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    public String getConnectionString()
    {
        return connectionString;
    }

    public void setConnectionString(String connectionString)
    {
        this.connectionString = connectionString;
    }

    public String getReadConcern()
    {
        return readConcern;
    }

    public void setReadConcern(String readConcern)
    {
        this.readConcern = readConcern;
    }

    public String getReadPreference()
    {
        return readPreference;
    }

    public void setReadPreference(String readPreference)
    {
        this.readPreference = readPreference;
    }

    public String getWriteConcern()
    {
        return writeConcern;
    }

    public void setWriteConcern(String writeConcern)
    {
        this.writeConcern = writeConcern;
    }

    public String getRetryReads()
    {
        return retryReads;
    }

    public void setRetryReads(String retryReads)
    {
        this.retryReads = retryReads;
    }

    public String getRetryWrites()
    {
        return retryWrites;
    }

    public void setRetryWrites(String retryWrites)
    {
        this.retryWrites = retryWrites;
    }

    public String getUuidRepresentation()
    {
        return uuidRepresentation;
    }

    public void setUuidRepresentation(String uuidRepresentation)
    {
        this.uuidRepresentation = uuidRepresentation;
    }

    public Map<String, String> getAuthMechanismProperties()
    {
        return authMechanismProperties;
    }

    public void setAuthMechanismProperties(Map<String, String> authMechanismProperties)
    {
        this.authMechanismProperties = authMechanismProperties;
    }
}

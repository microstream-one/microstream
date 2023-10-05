package one.microstream.integrations.spring.boot.types;

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

import java.util.Map;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import one.microstream.integrations.spring.boot.types.aws.Aws;
import one.microstream.integrations.spring.boot.types.hazelcast.Hazelcast;
import one.microstream.integrations.spring.boot.types.mongodb.Mongodb;
import one.microstream.integrations.spring.boot.types.oracle.Oracle;
import one.microstream.integrations.spring.boot.types.oraclecloud.Oraclecloud;
import one.microstream.integrations.spring.boot.types.redis.Redis;
import one.microstream.integrations.spring.boot.types.sql.Sql;

public class StorageFilesystem
{

    @NestedConfigurationProperty
    private Sql sql;

    @NestedConfigurationProperty
    private Aws aws;

    @NestedConfigurationProperty
    private Hazelcast hazelcast;

    /**
     * Supported properties
     * All supported properties of Kafka, see https://kafka.apache.org/documentation/
     */
    private Map<String, String> kafkaProperties;

    @NestedConfigurationProperty
    private Mongodb mongodb;

    @NestedConfigurationProperty
    private Oraclecloud oraclecloud;

    @NestedConfigurationProperty
    private Oracle oracle;

    @NestedConfigurationProperty
    private Redis redis;

    public Sql getSql()
    {
        return this.sql;
    }

    public void setSql(final Sql sql)
    {
        this.sql = sql;
    }

    public Aws getAws()
    {
        return this.aws;
    }

    public void setAws(final Aws aws)
    {
        this.aws = aws;
    }

    public Hazelcast getHazelcast()
    {
        return this.hazelcast;
    }

    public void setHazelcast(final Hazelcast hazelcast)
    {
        this.hazelcast = hazelcast;
    }

    public Map<String, String> getKafkaProperties()
    {
        return this.kafkaProperties;
    }

    public void setKafkaProperties(final Map<String, String> kafkaProperties)
    {
        this.kafkaProperties = kafkaProperties;
    }

    public Mongodb getMongodb()
    {
        return this.mongodb;
    }

    public void setMongodb(final Mongodb mongodb)
    {
        this.mongodb = mongodb;
    }

    public Oraclecloud getOraclecloud()
    {
        return this.oraclecloud;
    }

    public void setOraclecloud(final Oraclecloud oraclecloud)
    {
        this.oraclecloud = oraclecloud;
    }

    public Oracle getOracle()
    {
        return this.oracle;
    }

    public void setOracle(final Oracle oracle)
    {
        this.oracle = oracle;
    }

    public Redis getRedis()
    {
        return this.redis;
    }

    public void setRedis(final Redis redis)
    {
        this.redis = redis;
    }
}

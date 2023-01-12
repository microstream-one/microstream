package one.microstream.integrations.spring.boot.types;

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

import one.microstream.integrations.spring.boot.types.aws.Aws;
import one.microstream.integrations.spring.boot.types.hazelcast.Hazelcast;
import one.microstream.integrations.spring.boot.types.mongodb.Mongodb;
import one.microstream.integrations.spring.boot.types.oracle.Oracle;
import one.microstream.integrations.spring.boot.types.oraclecloud.Oraclecloud;
import one.microstream.integrations.spring.boot.types.redis.Redis;
import one.microstream.integrations.spring.boot.types.sql.Sql;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

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
        return sql;
    }

    public void setSql(Sql sql)
    {
        this.sql = sql;
    }

    public Aws getAws()
    {
        return aws;
    }

    public void setAws(Aws aws)
    {
        this.aws = aws;
    }

    public Hazelcast getHazelcast()
    {
        return hazelcast;
    }

    public void setHazelcast(Hazelcast hazelcast)
    {
        this.hazelcast = hazelcast;
    }

    public Map<String, String> getKafkaProperties()
    {
        return kafkaProperties;
    }

    public void setKafkaProperties(Map<String, String> kafkaProperties)
    {
        this.kafkaProperties = kafkaProperties;
    }

    public Mongodb getMongodb()
    {
        return mongodb;
    }

    public void setMongodb(Mongodb mongodb)
    {
        this.mongodb = mongodb;
    }

    public Oraclecloud getOraclecloud()
    {
        return oraclecloud;
    }

    public void setOraclecloud(Oraclecloud oraclecloud)
    {
        this.oraclecloud = oraclecloud;
    }

    public Oracle getOracle()
    {
        return oracle;
    }

    public void setOracle(Oracle oracle)
    {
        this.oracle = oracle;
    }

    public Redis getRedis()
    {
        return redis;
    }

    public void setRedis(Redis redis)
    {
        this.redis = redis;
    }
}

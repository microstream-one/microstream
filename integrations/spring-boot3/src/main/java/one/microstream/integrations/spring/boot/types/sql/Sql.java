package one.microstream.integrations.spring.boot.types.sql;

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

public class Sql
{

    @NestedConfigurationProperty
    private Mariadb mariadb;

    @NestedConfigurationProperty
    private Mysql mysql;

    @NestedConfigurationProperty
    private Oracle oracle;

    @NestedConfigurationProperty
    private Postgres postgres;

    @NestedConfigurationProperty
    private Sqlite sqlite;

    public Mariadb getMariadb()
    {
        return mariadb;
    }

    public void setMariadb(Mariadb mariadb)
    {
        this.mariadb = mariadb;
    }

    public Mysql getMysql()
    {
        return mysql;
    }

    public void setMysql(Mysql mysql)
    {
        this.mysql = mysql;
    }

    public Oracle getOracle()
    {
        return oracle;
    }

    public void setOracle(Oracle oracle)
    {
        this.oracle = oracle;
    }

    public Postgres getPostgres()
    {
        return postgres;
    }

    public void setPostgres(Postgres postgres)
    {
        this.postgres = postgres;
    }

    public Sqlite getSqlite()
    {
        return sqlite;
    }

    public void setSqlite(Sqlite sqlite)
    {
        this.sqlite = sqlite;
    }
}

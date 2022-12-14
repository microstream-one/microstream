package test.microstream.integrations.spring.boot;

/*-
 * #%L
 * microstream-integrations-spring-boot3
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

import one.microstream.afs.sql.types.SqlDataSourceProvider;
import one.microstream.configuration.types.Configuration;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

public class MyDataSourceProvider implements SqlDataSourceProvider
{

    public final static PostgreSQLContainer<?> pgsql;

    public final static String USERNAME = "postgres";
    public final static String PASSWORD = "password1";

    static
    {
        pgsql = new PostgreSQLContainer<>("postgres:alpine").withDatabaseName("jpa-test").withUsername(USERNAME).withPassword(PASSWORD);
        pgsql.start();
    }

    @Override
    public DataSource provideDataSource(Configuration configuration)
    {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(pgsql.getJdbcUrl());
        dataSource.setUser(configuration.get("user"));
        dataSource.setPassword(configuration.get("password"));

        return dataSource;
    }
}

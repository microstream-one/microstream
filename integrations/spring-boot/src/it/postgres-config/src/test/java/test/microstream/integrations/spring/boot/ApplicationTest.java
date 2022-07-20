package test.microstream.integrations.spring.boot;

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

import one.microstream.afs.sql.types.SqlConnector;
import one.microstream.afs.sql.types.SqlFileSystem;
import one.microstream.afs.sql.types.SqlProviderPostgres;
import one.microstream.afs.types.AFileSystem;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

@SpringBootTest
public class ApplicationTest
{

    private static final String TEXT = "Hello, i am happy, that you are here";

    @Autowired
    private EmbeddedStorageManager storage;

    public static DataSource provideDataSource()
    {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(MyDataSourceProvider.pgsql.getJdbcUrl());
        dataSource.setUser(MyDataSourceProvider.USERNAME);
        dataSource.setPassword(MyDataSourceProvider.PASSWORD);

        return dataSource;
    }

    @Test
    public void writeIntoPostgresTest()
    {
        //When I use the Microstream Spring Configuration to save data
        MyDataSourceProvider provider = new MyDataSourceProvider();

        Root root = new Root();
        root.setValue(TEXT);
        storage.setRoot(root);
        storage.storeRoot();

        storage.shutdown();

        //Then I start storage manually with the same connection data, so I must receive my data from storage again
        AFileSystem aFileSystem = SqlFileSystem.New(
                SqlConnector.Caching(
                        SqlProviderPostgres.New(provideDataSource())
                )
        );

        Root root2 = new Root();
        EmbeddedStorageManager storage2 = EmbeddedStorage.start(root2, aFileSystem.ensureDirectoryPath("microstream_storage"));

        //check if the String value is loaded from the storage
        Assertions.assertEquals(TEXT, root2.getValue());

        storage2.shutdown();
    }

}

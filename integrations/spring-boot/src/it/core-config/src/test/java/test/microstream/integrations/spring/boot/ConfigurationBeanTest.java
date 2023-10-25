package test.microstream.integrations.spring.boot;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import one.microstream.integrations.spring.boot.types.config.StorageManagerConfiguration;
import one.microstream.integrations.spring.boot.types.config.StorageManagerProvider;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

@SpringBootTest
public class ConfigurationBeanTest
{
    @Autowired
    StorageManagerConfiguration configuration;

    @Autowired
    StorageManagerProvider provider;

    private final String TEST_SENTENCE = "I love MicroStream Spring Extension";

    @Test
    void testReloadData()
    {
        Assertions.assertNotNull(configuration);
        Assertions.assertNotNull(provider);

        EmbeddedStorageFoundation<?> embeddedStorageFoundation = provider.embeddedStorageFoundation(configuration.getValues());
        TestData data = new TestData(TEST_SENTENCE);
        try (EmbeddedStorageManager storage = embeddedStorageFoundation.start(data)) {
        }

        EmbeddedStorageFoundation<?> embeddedStorageFoundation2 = provider.embeddedStorageFoundation(configuration.getValues());
        TestData data2 = new TestData();
        try (EmbeddedStorageManager storage2 = embeddedStorageFoundation2.start(data2)) {
            Assertions.assertEquals(TEST_SENTENCE, data2.getValue());
        }
    }

    @Test
    void testReloadDataWithoutFoundation()
    {
        Assertions.assertNotNull(configuration);
        Assertions.assertNotNull(provider);

        try (EmbeddedStorageManager storageManager = provider.create(configuration.getValues())) {
            TestData data = new TestData(TEST_SENTENCE);
            storageManager.start();
            storageManager.setRoot(data);
            storageManager.storeRoot();
        }

        try (EmbeddedStorageManager storageManager2 = provider.create(configuration.getValues())) {

            TestData data2 = new TestData();
            storageManager2.start();
            TestData readData = (TestData) storageManager2.root();
            Assertions.assertEquals(TEST_SENTENCE, readData.getValue());

        }
    }

}

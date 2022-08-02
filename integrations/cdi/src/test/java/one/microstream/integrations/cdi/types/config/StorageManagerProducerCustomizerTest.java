package one.microstream.integrations.cdi.types.config;

/*-
 * #%L
 * MicroStream Integrations CDI
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

import one.microstream.integrations.cdi.types.config.test.SomeEmbeddedStorageFoundationCustomizer;
import one.microstream.integrations.cdi.types.config.test.SomeStorageManagerInitializer;
import one.microstream.integrations.cdi.types.extension.StorageExtension;
import one.microstream.integrations.cdi.types.logging.TestLogger;
import one.microstream.storage.types.StorageManager;
import org.eclipse.microprofile.config.Config;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@EnableWeld
class StorageManagerProducerCustomizerTest
{
    public static final String STORAGE_DIRECTORY = "one.microstream.storage-directory";
    // Test the StorageManagerProducer
    // - Support for creating a StorageManager based on configuration values (when no MicroProfile Config one used)

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(StorageManagerProducer.class
            , StorageManagerProducerCustomizerTest.class
            , SomeEmbeddedStorageFoundationCustomizer.class
            , SomeStorageManagerInitializer.class);

    @Inject
    private StorageManagerProducer storageManagerProducer;

    @Inject
    private Instance<EmbeddedStorageFoundationCustomizer> customizers;

    @Inject
    private Instance<StorageManagerInitializer> initializers;

    @BeforeEach
    public void setup()
    {
        TestLogger.reset();
    }

    private static Config configMock;

    @Produces
    Config produceConfigMock()
    {
        configMock = Mockito.mock(Config.class);
        Mockito.when(configMock.getPropertyNames())
                .thenReturn(Arrays.asList(STORAGE_DIRECTORY));
        Mockito.when(configMock.getOptionalValue(STORAGE_DIRECTORY, String.class))
                .thenReturn(Optional.of("target/storage-customized"));
        return configMock;
    }


    private static StorageExtension storageExtensionMock;

    @Produces
    StorageExtension produceStorageExtension()
    {
        storageExtensionMock = Mockito.mock(StorageExtension.class);
        // This means we have no @Inject @ConfigProperty StorageManager
        // And thus should create StorageManger using Builder and load all Config Property keys.
        final Set<String> names = Collections.emptySet();
        Mockito.when(storageExtensionMock.getStorageManagerConfigInjectionNames())
                .thenReturn(names);

        return storageExtensionMock;
    }

    @Test
    void getStoreManager_customizeInitializer()
    {

        StorageManager storeManager = storageManagerProducer.getStoreManager();
        Assertions.assertNotNull(storeManager);

        Mockito.verify(configMock)
                .getPropertyNames();  // Test if all config property keys are used.

        final List<SomeEmbeddedStorageFoundationCustomizer> foundationCustomizers = customizers.stream()
                .map(SomeEmbeddedStorageFoundationCustomizer.class::cast)
                .collect(Collectors.toList());
        Assertions.assertEquals(foundationCustomizers.size(), 1);

        Assertions.assertTrue(foundationCustomizers.get(0)
                                      .isCustomizeCalled());

        final List<SomeStorageManagerInitializer> managerInitializers = initializers.stream()
                .map(SomeStorageManagerInitializer.class::cast)
                .collect(Collectors.toList());
        Assertions.assertEquals(managerInitializers.size(), 1);

        Assertions.assertTrue(managerInitializers.get(0)
                                      .isInitializerCalled());
        Assertions.assertTrue(managerInitializers.get(0)
                                      .isManagerRunning());


    }
}

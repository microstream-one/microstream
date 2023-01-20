package one.microstream.integrations.cdi.types.config;

/*-
 * #%L
 * microstream-integrations-cdi3
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
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@EnableWeld
class StorageManagerProducerFromConfigPropertyInjectionTest
{
    // Test the StorageManagerProducer
    // - Use the StorageManager from MicroProfile Config Converter (which uses external files like ini or xml)

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(StorageManagerProducer.class, StorageManagerProducerFromConfigPropertyInjectionTest.class);

    @Inject
    private StorageManagerProducer storageManagerProducer;

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
        return configMock;
    }


    private static StorageExtension storageExtensionMock;

    @Produces
    StorageExtension produceStorageExtension()
    {
        storageExtensionMock = Mockito.mock(StorageExtension.class);
        // This means we do have a @Inject @ConfigProperty StorageManager construct
        // And thus should 'take' StorageManger from MicroProfileConfig directly.
        final Set<String> names = Set.of("one.microstream.ini");
        Mockito.when(storageExtensionMock.getStorageManagerConfigInjectionNames())
                .thenReturn(names);

        return storageExtensionMock;
    }

    @Test
    void getStoreManager_fromConfigPropertyInjection()
    {

        StorageManager storageManager = storageManagerProducer.getStorageManager();
        Assertions.assertNull(storageManager);  // Since we did not mock ConfigMock.getValue()

        //
        Mockito.verify(configMock)
                .getValue("one.microstream.ini", StorageManager.class);  // Test if StorageManager from Config taken
        Mockito.verifyNoMoreInteractions(configMock);

        // Another test to prove we did not create a real Storage Manager
        List<LoggingEvent> messages = TestLogger.getMessagesOfLevel(Level.INFO);
        Optional<LoggingEvent> loggingEvent = messages.stream()
                .filter(le -> le.getMessage()
                        .equals("Embedded storage manager initialized"))
                .findAny();

        Assertions.assertFalse(loggingEvent.isPresent());

    }
}

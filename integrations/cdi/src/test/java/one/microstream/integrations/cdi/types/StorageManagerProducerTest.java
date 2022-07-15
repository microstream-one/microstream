package one.microstream.integrations.cdi.types;

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

import one.microstream.integrations.cdi.types.extension.StorageExtension;
import one.microstream.integrations.cdi.types.logging.TestLogger;
import one.microstream.storage.types.StorageManager;
import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class StorageManagerProducerTest
{
    // Test the StorageManagerProducer
    // - Support for creating a StorageManager based on configuration values
    // - Use the StorageManager from MicroProfile Config Converter (which uses external files like ini or xml)

    @Mock
    private Config configMock;

    @Mock
    private StorageExtension storageExtensionMock;

    @InjectMocks
    private StorageManagerProducer storageManagerProducer;

    @BeforeEach
    public void setup()
    {
        TestLogger.reset();
    }

    @Test
    void getStoreManager_noConfigPropertyInjection()
    {
        // This means we have no @Inject @ConfigProperty StorageManager
        // And thus should create StorageManger using Builder and load all Config Property keys.
        final Set<String> names = Collections.emptySet();
        Mockito.when(storageExtensionMock.getStorageManagerConfigInjectionNames())
                .thenReturn(names);

        StorageManager storeManager = storageManagerProducer.getStoreManager();
        Assertions.assertNotNull(storeManager);

        //
        Mockito.verify(configMock)
                .getPropertyNames();  // Test if all config property keys are used.
        Mockito.verifyNoMoreInteractions(configMock);

        // Another test to prove a real StorageManager is created to have a look at the logs
        List<LoggingEvent> messages = TestLogger.getMessagesOfLevel(Level.INFO);
        Optional<LoggingEvent> loggingEvent = messages.stream()
                .filter(le -> le.getMessage()
                        .equals("Embedded storage manager initialized"))
                .findAny();

        Assertions.assertTrue(loggingEvent.isPresent());

    }

    @Test
    void getStoreManager_fromConfigPropertyInjection()
    {
        // This means we do have a @Inject @ConfigProperty StorageManager construct
        // And thus should 'take'' StorageManger from MicroProfileConfig directly.
        final Set<String> names = Set.of("one.microstream.ini");
        Mockito.when(storageExtensionMock.getStorageManagerConfigInjectionNames())
                .thenReturn(names);

        StorageManager storeManager = storageManagerProducer.getStoreManager();
        Assertions.assertNull(storeManager);  // Since we did not mock ConfigMock.getValue()

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

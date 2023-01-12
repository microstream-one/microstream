package test.microstream.integrations.spring.boot;

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

import one.microstream.storage.types.StorageManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StorageManagerTest {

    @Autowired
    private StorageManager manager;

    @Autowired
    @Qualifier("secondary")
    private StorageManager secondaryManager;


    @Test
    public void managerConfigurationTest() {
        Assertions.assertNotNull(manager);

        Assertions.assertEquals(manager.databaseName(), "Primary");
        String path = manager.configuration().fileProvider().baseDirectory().toPathString();
        Assertions.assertTrue(path.endsWith("microstream-storage-test/"));
        Assertions.assertEquals(manager.configuration().channelCountProvider().getChannelCount(), 2);
    }

    @Test
    public void secondaryManagerConfigurationTest() {
        Assertions.assertNotNull(secondaryManager);

        Assertions.assertEquals(secondaryManager.databaseName(), "secondary");
        String path = secondaryManager.configuration().fileProvider().baseDirectory().toPathString();
        Assertions.assertTrue(path.endsWith("microstream-secondary-storage-test/"));
        Assertions.assertEquals(secondaryManager.configuration().channelCountProvider().getChannelCount(), 1);
    }

}

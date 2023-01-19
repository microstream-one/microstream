package test.microstream.integrations.spring.boot;

/*-
 * #%L
 * microstream-integrations-spring-boot
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

import one.microstream.storage.types.StorageManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StorageManagerTest {

    @Autowired
    @Qualifier("red")
    private StorageManager redManager;

    @Autowired
    @Qualifier("green")
    private StorageManager greenManager;


    @Test
    public void redManagerConfigurationTest() {
        Assertions.assertNotNull(redManager);

        Assertions.assertEquals(redManager.databaseName(), "red");
        String path = redManager.configuration().fileProvider().baseDirectory().toPathString();
        Assertions.assertTrue(path.endsWith("microstream-red-storage-test/"));
        Assertions.assertEquals(redManager.configuration().channelCountProvider().getChannelCount(), 2);
    }

    @Test
    public void greenManagerConfigurationTest() {
        Assertions.assertNotNull(greenManager);

        Assertions.assertEquals(greenManager.databaseName(), "green");
        String path = greenManager.configuration().fileProvider().baseDirectory().toPathString();
        Assertions.assertTrue(path.endsWith("microstream-green-storage-test/"));
        Assertions.assertEquals(greenManager.configuration().channelCountProvider().getChannelCount(), 1);
    }

}

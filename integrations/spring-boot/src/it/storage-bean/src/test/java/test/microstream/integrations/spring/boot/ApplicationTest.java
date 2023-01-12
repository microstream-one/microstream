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

import one.microstream.integrations.spring.boot.types.config.MicrostreamConfigurationProperties;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test.microstream.integrations.spring.boot.database.Root;
import test.microstream.integrations.spring.boot.database.UserRepository;
import test.microstream.integrations.spring.boot.dto.CreateUser;
import test.microstream.integrations.spring.boot.model.User;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class ApplicationTest
{

    private static final String EMAIL = "rudy@microstream.one";
    private static final String EMAIL_UPDATE = "r.debusscher@microstream.one";
    @Autowired
    private UserRepository repository;

    @Autowired
    private MicrostreamConfigurationProperties configurationProperties;

    @Test
    public void storageBeanTest() throws InterruptedException
    {
        Assertions.assertNotNull(repository);

        // Check StorageManagerInitializer execution
        List<User> users = repository.getAll();
        Assertions.assertEquals(2, users.size());

        CreateUser newUser = new User("Rudy", EMAIL);
        repository.add(newUser);

        users = repository.getAll();
        Assertions.assertEquals(3, users.size());

        Optional<User> byEmail = repository.findByEmail(EMAIL);
        if (byEmail.isEmpty())
        {
            Assertions.fail("Unable to find added user by email");
        }

        repository.updateEmail(byEmail.get()
                                       .getId(), EMAIL_UPDATE);

        Thread.sleep(500L);  // Allow for asynchronous processing

        // Check if data is stored
        Path storageDirectory = Paths.get(configurationProperties.getStorageDirectory());
        System.out.println(storageDirectory);
        Root root = new Root();

        try (StorageManager storageManager = EmbeddedStorage.start(root, storageDirectory)) {
            Assertions.assertEquals(3, root.getUsers().size(), "Added User not found");

            Optional<User> user = root.getUsers()
                    .stream()
                    .filter(u -> EMAIL_UPDATE.equals(u.getEmail()))
                    .findAny();

            if (user.isEmpty()) {
                Assertions.fail("User with updated mail not found");
            }
        }

    }

}

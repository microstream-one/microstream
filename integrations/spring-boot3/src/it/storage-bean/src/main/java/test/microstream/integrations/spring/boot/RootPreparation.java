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

import one.microstream.integrations.spring.boot.types.config.StorageManagerInitializer;
import one.microstream.storage.types.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import test.microstream.integrations.spring.boot.database.Root;
import test.microstream.integrations.spring.boot.model.User;

@Component
public class RootPreparation implements StorageManagerInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootPreparation.class);

    @Override
    public void initialize(StorageManager storageManager) {
        LOGGER.info("(From the App) Add basic data if needed");

        // Since we have @Storage used, we are sure that Root object is initialized in StorageManager
        // We only need to check if there is an initialization of data required or not (since we already ran it before)

        Root root = (Root) storageManager.root();
        // Init 'database' with some data
        if (root.getUsers().isEmpty()) {
            init(root);
            // We have made changes outside of the normal way (the repositories) that have automatic
            // storage through @Store.
            // We store the root using an Eager Storer since we need to store all info anyway.
            storageManager.createEagerStorer().store(root);
        }

    }

    public void init(Root root) {
        User johnDoe = new User("John Doe", "john.doe@acme.org");
        User janeDoe = new User("Jane Doe", "jane.doe@acme.org");

        root.addUser(johnDoe);
        root.addUser(janeDoe);

    }

}

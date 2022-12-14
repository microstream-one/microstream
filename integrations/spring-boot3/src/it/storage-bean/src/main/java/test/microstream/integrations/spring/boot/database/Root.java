package test.microstream.integrations.spring.boot.database;

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


import one.microstream.integrations.spring.boot.types.Storage;
import one.microstream.storage.types.StorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import test.microstream.integrations.spring.boot.model.User;

import java.util.ArrayList;
import java.util.List;

@Storage
public class Root {

    @Autowired
    private transient StorageManager storageManager;

    private final List<User> users = new ArrayList<>();

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public User addUser(User user) {
        users.add(user);
        storageManager.store(users);
        return user;
    }

    /**
     * Since the User instance is already part of the User Collection, we just need
     * to make it is stored externally.
     *
     * @param user
     */
    public void updateUser(User user) {
        storageManager.store(user);
    }

    public void removeUser(User user) {
        users.remove(user);
        storageManager.store(users);
    }

}

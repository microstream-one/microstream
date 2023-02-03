package test.microstream.integrations.spring.boot.database;

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

import org.springframework.stereotype.Component;
import test.microstream.integrations.spring.boot.dto.CreateUser;
import test.microstream.integrations.spring.boot.exception.UserAlreadyExistsException;
import test.microstream.integrations.spring.boot.exception.UserNotFoundException;
import test.microstream.integrations.spring.boot.model.User;

import java.util.List;
import java.util.Optional;

@Component
public class UserRepository {

    private static final Object USER_LOCK = new Object();

    private final Root root;

    public UserRepository(Root root) {
        this.root = root;
    }

    public List<User> getAll() {
        return root.getUsers();
    }

    public Optional<User> getById(String id) {
        return root.getUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findAny();
    }

    public Optional<User> findByEmail(String email) {
        return root.getUsers().stream()
                .filter(u -> email.equals(u.getEmail()))
                .findAny();
    }

    public User add(CreateUser user) {
        User result;
        synchronized (USER_LOCK) {
            Optional<User> byEmail = findByEmail(user.getEmail());
            if (byEmail.isPresent()) {
                throw new UserAlreadyExistsException();
            }
            result = root.addUser(new User(user.getName(), user.getEmail()));
        }
        return result;
    }

    public User updateEmail(String id, String email) {
        Optional<User> byId = getById(id);
        if (byId.isEmpty()) {
            throw new UserNotFoundException();
        }
        User user = byId.get();
        user.setEmail(email);
        root.updateUser(user);
        return user;
    }
}

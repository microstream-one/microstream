package one.microstream.experimental.demo.csv.model;

/*-
 * #%L
 * demo
 * %%
 * Copyright (C) 2023 MicroStream Software
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

import java.util.ArrayList;
import java.util.List;

public class Root {

    private transient StorageManager storageManager;

    public void setStorageManager(final StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    private final List<User> users = new ArrayList<>();
    private final List<Book> books = new ArrayList<>();

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public User addUser(final User user) {
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
    public void updateUser(final User user) {
        storageManager.store(user);
    }

    public void removeUser(final User user) {
        users.remove(user);
        storageManager.store(users);
    }

    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }

    public void addBook(final Book book) {
        books.add(book);
        storageManager.store(books);
    }

    /**
     * User instance must already be part of the Object graph of the root managed by MicroStream.
     *
     * @param user
     * @param book
     */
    public void addBookToUser(final User user, final Book book) {
        user.addBook(book, storageManager);
    }
}

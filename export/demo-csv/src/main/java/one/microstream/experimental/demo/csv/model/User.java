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
import java.util.UUID;

public class User {

    private final String id;
    // We don't need a technical id when using MicroStream but need some kind of identification of the instance.
    // but email address is not a good candidate as that might change.

    private final String name;
    private String email;

    private final List<Book> books = new ArrayList<>();

    public User(final String name, final String email) {
        id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }

    public void addBook(final Book book, final StorageManager storageManager) {
        books.add(book);
        // Since we don't like to expose the actual list of Books (through getBooks)  as that
        // means we could alter the list outside the root, we provide the StorageManager as
        // parameter.
        storageManager.store(books);
    }
}

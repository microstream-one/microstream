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

import one.microstream.integrations.spring.boot.types.Storage;
import one.microstream.storage.types.StorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashSet;
import java.util.Set;

@Storage
@Qualifier("secondary")
public class Names {

    // No constructor injection supported on @Storage Beans.
    @Autowired
    @Qualifier("secondary")
    private transient StorageManager storageManager;

    private final Set<String> names = new HashSet<>();

    public Set<String> getNames() {
        return new HashSet<>(this.names);
    }

    public void addName(String name) {
        this.names.add(name);
    }
}

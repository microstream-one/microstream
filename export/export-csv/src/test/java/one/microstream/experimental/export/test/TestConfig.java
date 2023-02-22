package one.microstream.experimental.export.test;

/*-
 * #%L
 * export
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

import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageManager;

public class TestConfig {
    public static StorageManager createStorageManager(final Object root) {
        return createStorageFoundation()
                .setRoot(root)

                .createEmbeddedStorageManager()
                .start();
    }

    public static EmbeddedStorageFoundation<?> createStorageFoundation() {
        return EmbeddedStorageConfiguration.Builder()
                .setStorageDirectory("target/data")

                .createEmbeddedStorageFoundation();
    }
}

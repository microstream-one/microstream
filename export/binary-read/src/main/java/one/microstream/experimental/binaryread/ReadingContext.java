package one.microstream.experimental.binaryread;

/*-
 * #%L
 * binary-read
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

import one.microstream.experimental.binaryread.structure.Storage;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;

import java.util.Objects;

/**
 * A single holder with the instances that needs to be accessed from various locations.
 * An instance will be passed on as a method parameter.
 */
public class ReadingContext
{

    private final EmbeddedStorageFoundation<?> storageFoundation;

    private final Storage storage;

    /**
     * Step 1 for building reading context, we assign the StorageFoundation and FIXME, the configuration options
     * @param storageFoundation
     */
    public ReadingContext(final EmbeddedStorageFoundation<?> storageFoundation)
    {
        this.storageFoundation = storageFoundation;
        this.storage = null;
    }

    /**
     * Step 2 for building reading context, we assign the Storage.
     * @param readingContext The previous reading Context
     * @param storage
     */
    public ReadingContext(final ReadingContext readingContext, final Storage storage)
    {
        this.storageFoundation = Objects.requireNonNull(readingContext.storageFoundation);
        this.storage = storage;
    }

    public EmbeddedStorageFoundation<?> getStorageFoundation()
    {
        return storageFoundation;
    }

    public Storage getStorage()
    {
        return Objects.requireNonNull(storage, "ReadingContext is not yet in the state of having a Storage");
    }

}

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

import one.microstream.experimental.binaryread.config.BinaryReadConfig;
import one.microstream.experimental.binaryread.storage.CachedStorageBytes;
import one.microstream.experimental.binaryread.structure.Storage;
import one.microstream.experimental.binaryread.structure.util.BinaryDataHelper;

import java.util.Objects;

/**
 * A single holder with the instances that needs to be accessed from various locations.
 * An instance will be passed on as a method parameter.
 */
public class ReadingContext
{

    private final BinaryReadConfig binaryReadConfig;

    private final Storage storage;

    private final CachedStorageBytes cachedStorageBytes;

    private final BinaryDataHelper binaryDataHelper;

    /**
     * Step 1 for building reading context, we assign the read configuration options.
     *
     * @param config The configuration for reading the data storage.
     */
    public ReadingContext(final BinaryReadConfig config)
    {
        this.binaryReadConfig = config;
        this.storage = null;
        this.binaryDataHelper = null;
        this.cachedStorageBytes = null;
    }

    /**
     * Step 2 for building reading context, we assign the Storage. This also creates instances of
     * {@link CachedStorageBytes} and {@link BinaryDataHelper}
     *
     * @param readingContext The previous reading Context
     * @param storage        The Storage instance for accessing the data.
     */
    public ReadingContext(final ReadingContext readingContext, final Storage storage)
    {
        this.binaryReadConfig = Objects.requireNonNull(readingContext.binaryReadConfig);
        this.storage = storage;
        this.binaryDataHelper = new BinaryDataHelper(this.binaryReadConfig.isReverse());
        this.cachedStorageBytes = new CachedStorageBytes(this);
    }

    public BinaryReadConfig getBinaryReadConfig()
    {
        return binaryReadConfig;
    }

    public Storage getStorage()
    {
        return Objects.requireNonNull(storage, "ReadingContext is not yet in the state of having a Storage");
    }

    public CachedStorageBytes getCachedStorageBytes()
    {
        return Objects.requireNonNull(cachedStorageBytes, "ReadingContext is not yet in the state of having a CachedStorageBytes");
    }

    public BinaryDataHelper getBinaryDataHelper()
    {
        return Objects.requireNonNull(binaryDataHelper, "ReadingContext is not yet in the state of having a BinaryDataHelper");
    }
}

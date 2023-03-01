package one.microstream.experimental.binaryread.config;

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

import one.microstream.experimental.binaryread.exception.InvalidCacheSizeConfigValueException;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;

public class BinaryReadConfig
{
    private final EmbeddedStorageFoundation<?> storageFoundation;

    private final boolean makeDataCopy; // is a copy required of the data storage so we work on 'stable' data?

    private final int cacheSize; // Cache size in bytes for data storage in memory (increase performance reading from storage)

    private final boolean reverse; // Are bytes stored reversed (Big Endian / Little endian)

    public BinaryReadConfig(final EmbeddedStorageFoundation<?> storageFoundation, final boolean makeDataCopy, final boolean reverse, final int cacheSize)
    {
        this.storageFoundation = storageFoundation;
        this.makeDataCopy = makeDataCopy;
        this.cacheSize = cacheSize;
        if (cacheSize < 1024) {
            throw new InvalidCacheSizeConfigValueException(cacheSize);
        }
        this.reverse = reverse;
    }

    public EmbeddedStorageFoundation<?> getStorageFoundation()
    {
        return storageFoundation;
    }

    public boolean makeDataCopy()
    {
        return makeDataCopy;
    }

    public int getCacheSize()
    {
        return cacheSize;
    }

    public boolean isReverse()
    {
        return reverse;
    }

}

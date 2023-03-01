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

import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;

public class BinaryReadConfigBuilder
{
    private EmbeddedStorageFoundation<?> storageFoundation;
    private boolean makeDataCopy = false;
    private boolean reverse = true;

    private int cacheSize = 16384;
    // TODO initial tests doesn't show an improvement when 16384 -> 65536

    public BinaryReadConfigBuilder withStorageFoundation(final EmbeddedStorageFoundation<?> storageFoundation)
    {
        this.storageFoundation = storageFoundation;
        return this;
    }

    public BinaryReadConfigBuilder withMakeDataCopy(final boolean makeDataCopy)
    {
        this.makeDataCopy = makeDataCopy;
        return this;
    }

    public BinaryReadConfigBuilder withReverse(final boolean reverse)
    {
        this.reverse = reverse;
        return this;
    }

    public BinaryReadConfigBuilder withCacheSize(final int cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    public BinaryReadConfig build()
    {
        return new BinaryReadConfig(storageFoundation, makeDataCopy, reverse, cacheSize);
    }
}

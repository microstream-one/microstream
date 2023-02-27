package one.microstream.experimental.binaryread.storage;

/*-
 * #%L
 * binary-read
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


import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageDataInventoryFile;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class DataFiles
{

    private DataFiles()
    {
    }

    /**
     * Determine the data files we have in the storage.
     *
     * @param storageFoundation Configuration of the Storage.
     * @return List of the data files.
     */
    public static List<StorageDataInventoryFile> defineDataFiles(final EmbeddedStorageFoundation<?> storageFoundation)
    {

        final List<StorageDataInventoryFile> result = new ArrayList<>();

        final int channelCount = storageFoundation.getConfiguration()
                .channelCountProvider()
                .getChannelCount();

        for (int channelIndex = 0; channelIndex < channelCount; channelIndex++)
        {
            storageFoundation.getConfiguration()
                    .fileProvider()
                    .collectDataFiles(
                            StorageDataInventoryFile::New
                            , result::add
                            , channelIndex);
        }

        return result;
    }
}

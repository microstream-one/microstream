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


import one.microstream.experimental.binaryread.structure.ArrayHeader;
import one.microstream.experimental.binaryread.structure.Entity;
import one.microstream.experimental.binaryread.structure.EntityHeader;
import one.microstream.experimental.binaryread.structure.util.BinaryData;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageDataInventoryFile;

import java.nio.ByteBuffer;
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

    /**
     * Reads the 'entity' header of a data block, 3 long values indicating the block length,
     * typeId and objectId.
     *
     * @param dataFile The file we need to read the information from
     * @param pos      Position where the block starts and start position to read the 3 long values.
     * @return {@link Entity} Object representing the entity (The EntityMembers are not yet filled)
     */
    public static Entity readEntityHeader(final StorageDataInventoryFile dataFile, final long pos)
    {
        final ByteBuffer buff = ByteBuffer.allocate(Long.BYTES * 3);
        dataFile.readBytes(buff, pos);

        final long length = BinaryData.bytesToLong(buff);
        final long typeId = BinaryData.bytesToLong(buff, Long.BYTES);

        final long objectId = BinaryData.bytesToLong(buff, 2 * Long.BYTES);

        return new Entity(dataFile, pos, new EntityHeader(length, typeId, objectId));
    }

    /**
     * Read the Array header consisting of Size (total amount of bytes in block) and length (number of items).
     *
     * @param dataFile The file we need to read the information from
     * @param pos      Position where the block starts and start position to read the 2 long values.
     * @return A ArrayHeader instance with size and length info.
     */
    public static ArrayHeader readArrayHeader(final StorageDataInventoryFile dataFile, final long pos)
    {
        final ByteBuffer buff = ByteBuffer.allocate(Long.BYTES * 2);
        dataFile.readBytes(buff, pos);

        final long size = BinaryData.bytesToLong(buff);
        final long length = BinaryData.bytesToLong(buff, Long.BYTES);

        return new ArrayHeader(size, length);
    }
}
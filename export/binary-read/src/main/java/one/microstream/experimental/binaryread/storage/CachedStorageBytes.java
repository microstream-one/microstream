package one.microstream.experimental.binaryread.storage;

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

import one.microstream.experimental.binaryread.structure.ArrayHeader;
import one.microstream.experimental.binaryread.structure.Entity;
import one.microstream.experimental.binaryread.structure.EntityHeader;
import one.microstream.experimental.binaryread.structure.util.BinaryData;
import one.microstream.storage.types.StorageDataInventoryFile;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps data from the storage into memory to reduce the amount of reads to the
 * file/storage.
 * Instead of performing many small reads for retrieving the headers, we read a large block
 * into memory and use this source when bytes are available.
 */
public final class CachedStorageBytes
{

    private static final CachedStorageBytes INSTANCE = new CachedStorageBytes();
    private static final int CACHE_SIZE = 16384;
    // TODO initial tests doesn't show an improvement when 16384 -> 65536

    private final Map<DataFileKey, DataFileCache> dataFileCacheMap = new HashMap<>();

    private CachedStorageBytes()
    {
    }

    /**
     * Reads the 'entity' header of a data block, 3 long values indicating the block length,
     * typeId and objectId.
     *
     * @param dataFile The file we need to read the information from
     * @param pos      Position where the block starts and start position to read the 3 long values.
     * @return {@link Entity} Object representing the entity (The EntityMembers are not yet filled)
     */
    public Entity readEntityHeader(final StorageDataInventoryFile dataFile, final long pos)
    {
        final DataFileCache dataFileCache = getDataFileCache(dataFile, pos, 3 * Long.BYTES);

        // We recalculate the position of the header according the start position of the cache.
        final int cachePos = (int) (pos - dataFileCache.startPosition);

        // read the data
        final long length = BinaryData.bytesToLong(dataFileCache.byteBuffer, cachePos);
        final long typeId = BinaryData.bytesToLong(dataFileCache.byteBuffer, cachePos + Long.BYTES);

        final long objectId = BinaryData.bytesToLong(dataFileCache.byteBuffer, cachePos + 2 * Long.BYTES);

        return new Entity(dataFile, pos, new EntityHeader(length, typeId, objectId));
    }

    /**
     * Read the Array header consisting of Size (total amount of bytes in block) and length (number of items).
     *
     * @param dataFile The file we need to read the information from
     * @param pos      Position where the block starts and start position to read the 2 long values.
     * @return A ArrayHeader instance with size and length info.
     */
    public ArrayHeader readArrayHeader(final StorageDataInventoryFile dataFile, final long pos)
    {
        final DataFileCache dataFileCache = getDataFileCache(dataFile, pos, 2 * Long.BYTES);

        // We recalculate the position of the header according the start position of the cache.
        final int cachePos = (int) (pos - dataFileCache.startPosition);

        final long size = BinaryData.bytesToLong(dataFileCache.byteBuffer, cachePos);
        final long length = BinaryData.bytesToLong(dataFileCache.byteBuffer, cachePos + Long.BYTES);

        return new ArrayHeader(size, length);
    }

    private DataFileCache getDataFileCache(final StorageDataInventoryFile dataFile, final long pos, final int minLength)
    {
        DataFileCache result;
        DataFileKey key = new DataFileKey(dataFile);
        if (!dataFileCacheMap.containsKey(key))
        {
            result = readDataAndCache(dataFile, key, pos);
        }
        else
        {
            result = dataFileCacheMap.get(key);
            if (!cacheContainsData(result, pos, minLength))
            {
                // The data is not within cache, prepare a new cache.
                result = readDataAndCache(dataFile, key, pos);
            }
        }
        return result;
    }

    private boolean cacheContainsData(final DataFileCache dataFileCache, final long pos, final int minLength)
    {
        final long startPosition = dataFileCache.startPosition;
        // We perform - minLength (ike 3 * Long.BYTES) so we know that the 3 long values are included within the cache
        final long endPosition = startPosition + dataFileCache.byteBuffer.capacity() - minLength;
        return pos > startPosition && pos < endPosition;
    }

    private DataFileCache readDataAndCache(final StorageDataInventoryFile dataFile, final DataFileKey key, final long pos)
    {
        final long fileSize = dataFile.size();
        int bufferSize;
        if (fileSize - pos > CACHE_SIZE)
        {

            bufferSize = CACHE_SIZE;
        }
        else
        {
            bufferSize = (int) (fileSize - pos);
        }
        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        dataFile.readBytes(buffer, pos);
        final DataFileCache cache = new DataFileCache(buffer, pos);
        // Keep for next time
        dataFileCacheMap.put(key, cache);
        return cache;
    }

    /**
     * Channel index and data file number uniquely identify the source.
     */
    private static class DataFileKey implements Serializable
    {
        private final int channelIndex;
        private final long number;

        public DataFileKey(final StorageDataInventoryFile dataFile)
        {
            channelIndex = dataFile.channelIndex();
            number = dataFile.number();
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            DataFileKey that = (DataFileKey) o;

            if (channelIndex != that.channelIndex)
            {
                return false;
            }
            return number == that.number;
        }

        @Override
        public int hashCode()
        {
            int result = channelIndex;
            result = 31 * result + (int) (number ^ (number >>> 32));
            return result;
        }
    }

    private static class DataFileCache
    {
        private final ByteBuffer byteBuffer;
        private final long startPosition;

        public DataFileCache(final ByteBuffer byteBuffer, final long startPosition)
        {
            this.byteBuffer = byteBuffer;
            this.startPosition = startPosition;
        }
    }

    public static CachedStorageBytes getInstance() {
        return INSTANCE;
    }

}

package net.jadoth.storage.types;

import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.binary.types.MemoryRangeCopier;



/**
 * Implementation-level type of an entry/item representing a single entity.
 *
 * @author TM
 * @param <I>
 */
public interface StorageEntityCacheItem<I extends StorageEntityCacheItem<I>>
{
	public long objectId();

	public long dataLength();

	public long storagePosition();

	public StorageDataFile<I> storageFile();

	public void copyCachedData(MemoryRangeCopier entityDataCollector);

	public long clearCache();

	public boolean iterateReferenceIds(_longProcedure referenceIdIterator);

	public long exportTo(StorageLockedFile file);

}

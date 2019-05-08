package one.microstream.storage.types;

import one.microstream.persistence.binary.types.MemoryRangeReader;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;



/**
 * Internal type of an entry/item representing a single entity.
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

	public void copyCachedData(MemoryRangeReader entityDataCollector);

	public long clearCache();

	public boolean iterateReferenceIds(PersistenceObjectIdAcceptor referenceIdIterator);

	public long exportTo(StorageLockedFile file);

}

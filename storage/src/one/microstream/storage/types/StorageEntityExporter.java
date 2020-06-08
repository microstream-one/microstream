package one.microstream.storage.types;


public interface StorageEntityExporter<E extends StorageEntityCacheItem<E>>
{
	public void exportEntities(StorageEntityType<E> type, ZStorageLockedFile file);

	public void cleanup();




	public final class Default implements StorageEntityExporter<StorageEntity.Default>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void exportEntities(
			final StorageEntityType<StorageEntity.Default> type,
			final ZStorageLockedFile                        file
		)
		{
			type.iterateEntities(e ->
				e.exportTo(file)
			);
		}

		@Override
		public final void cleanup()
		{
			// nothing to cleanup in simple storage copying implementation
		}

	}

}

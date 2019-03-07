package net.jadoth.storage.types;


public interface StorageEntityExporter<E extends StorageEntityCacheItem<E>>
{
	public void exportEntities(StorageEntityType<E> type, StorageLockedFile file);

	public void cleanup();




	public final class Implementation implements StorageEntityExporter<StorageEntity.Implementation>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void exportEntities(
			final StorageEntityType<StorageEntity.Implementation> type,
			final StorageLockedFile                               file
		)
		{
			type.iterateEntities(e -> e.exportTo(file));
		}

		@Override
		public final void cleanup()
		{
			// nothing to cleanup in simple storage copying implementation
		}

	}

}

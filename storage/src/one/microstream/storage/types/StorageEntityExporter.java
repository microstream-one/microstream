package one.microstream.storage.types;

public interface StorageEntityExporter<E extends StorageEntity>
{
	public void exportEntities(StorageEntityType<E> type, StorageFile file);

	public void cleanup();




	public final class Default implements StorageEntityExporter<StorageEntity.Default>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void exportEntities(
			final StorageEntityType<StorageEntity.Default> type,
			final StorageFile                              file
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

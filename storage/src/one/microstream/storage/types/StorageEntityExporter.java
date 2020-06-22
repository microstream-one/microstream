package one.microstream.storage.types;

import one.microstream.afs.AWritableFile;

public interface StorageEntityExporter<E extends StorageEntity>
{
	public void exportEntities(StorageEntityType<E> type, AWritableFile file);

	public void cleanup();




	public final class Default implements StorageEntityExporter<StorageEntity.Default>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void exportEntities(
			final StorageEntityType<StorageEntity.Default> type,
			final AWritableFile                            file
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

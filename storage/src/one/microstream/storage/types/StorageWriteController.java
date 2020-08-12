package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.afs.WriteController;
import one.microstream.persistence.types.PersistenceWriteController;


public interface StorageWriteController extends PersistenceWriteController
{
	public interface Provider
	{
		public StorageWriteController provideWriteController(StorageLiveFileProvider fileProvider);
	}
	
	
	
	public static StorageWriteController Wrap(
		final WriteController writeController
	)
	{
		return new StorageWriteController.Wrapper(
			notNull(writeController)
		);
	}
	
	public final class Wrapper implements StorageWriteController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final WriteController writeController;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Wrapper(final WriteController writeController)
		{
			super();
			this.writeController = writeController;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final void validateIsWritable()
		{
			this.writeController.validateIsWritable();
		}
		
		@Override
		public final boolean isWritable()
		{
			return this.writeController.isWritable();
		}
		
		@Override
		public final void validateIsStoringEnabled()
		{
			this.validateIsWritable();
		}
		
		@Override
		public final boolean isStoringEnabled()
		{
			return this.isWritable();
		}
				
	}
		
}

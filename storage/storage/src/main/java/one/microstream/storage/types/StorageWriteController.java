package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.afs.types.WriteController;
import one.microstream.persistence.types.PersistenceWriteController;
import one.microstream.storage.exceptions.StorageExceptionBackupDisabled;
import one.microstream.storage.exceptions.StorageExceptionDeletionDirectoryDisabled;
import one.microstream.storage.exceptions.StorageExceptionFileCleanupDisabled;
import one.microstream.storage.exceptions.StorageExceptionFileDeletionDisabled;


public interface StorageWriteController extends PersistenceWriteController
{
	public default void validateIsFileCleanupEnabled()
	{
		if(this.isFileCleanupEnabled())
		{
			return;
		}

		throw new StorageExceptionFileCleanupDisabled("File Cleanup is not enabled.");
	}
	
	public boolean isFileCleanupEnabled();
	
	
	
	public default void validateIsBackupEnabled()
	{
		if(this.isBackupEnabled())
		{
			return;
		}

		throw new StorageExceptionBackupDisabled("Backup is not enabled.");
	}
	
	public boolean isBackupEnabled();
	
	
	public default void validateIsDeletionDirectoryEnabled()
	{
		if(this.isDeletionDirectoryEnabled())
		{
			return;
		}

		throw new StorageExceptionDeletionDirectoryDisabled("Deletion directory is not enabled.");
	}
	
	public boolean isDeletionDirectoryEnabled();
	
	public default void validateIsFileDeletionEnabled()
	{
		if(this.isFileDeletionEnabled())
		{
			return;
		}

		throw new StorageExceptionFileDeletionDisabled("File deletion is not enabled.");
	}
	
	public boolean isFileDeletionEnabled();
	
	
	
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
		
		@Override
		public final boolean isFileCleanupEnabled()
		{
			return this.isWritable();
		}
		
		@Override
		public final boolean isBackupEnabled()
		{
			return this.isWritable();
		}
		
		@Override
		public final boolean isDeletionDirectoryEnabled()
		{
			return this.isWritable();
		}
		
		@Override
		public final boolean isFileDeletionEnabled()
		{
			return this.isWritable();
		}
				
	}
		
}

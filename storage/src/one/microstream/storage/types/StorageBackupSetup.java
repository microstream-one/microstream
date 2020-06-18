package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.file.Path;

import one.microstream.afs.ADirectory;
import one.microstream.afs.nio.NioFileSystem;

public interface StorageBackupSetup
{
	public StorageBackupFileProvider backupFileProvider();
	
	public StorageFileWriter.Provider setupWriterProvider(
		StorageFileWriter.Provider writerProvider
	);
	
	public StorageBackupHandler setupHandler(
		StorageOperationController operationController,
		StorageDataFileValidator   validator
	);
	

	
	/**
	 * Pseudo-constructor method to create a new {@link StorageBackupSetup} instance
	 * using the passed directory as the backup location.
	 * <p>
	 * For explanations and customizing values, see {@link StorageBackupSetup#New(StorageFileProvider)}.
	 * 
	 * @param backupDirectory the directory where the backup shall be located.
	 * 
	 * @return {@linkDoc StorageBackupSetup#New(StorageFileProvider)@return}
	 * 
	 * @see StorageBackupSetup#New(StorageFileProvider)
	 * @see StorageBackupHandler
	 */
	public static StorageBackupSetup New(final Path backupDirectory)
	{
		final ADirectory dir = NioFileSystem.New().ensureDirectory(backupDirectory);
		
		return New(dir);
	}
	
	public static StorageBackupSetup New(final ADirectory backupDirectory)
	{
		final StorageBackupFileProvider backupFileProvider = StorageBackupFileProvider.Builder()
			.setBackupDirectory(backupDirectory)
			.createBackupFileProvider()
		;
		return New(backupFileProvider);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageBackupSetup} instance
	 * using the passed {@link StorageFileProvider}.
	 * <p>
	 * A StorageBackupSetup basically defines where the backup files will be located by the {@link StorageBackupHandler}.
	 * 
	 * @param backupFileProvider the {@link StorageBackupFileProvider} to define where the backup files will be located.
	 * 
	 * @return a new {@link StorageBackupSetup} instance.
	 * 
	 * @see StorageBackupSetup#New(Path)
	 * @see StorageBackupHandler
	 */
	public static StorageBackupSetup New(final StorageBackupFileProvider backupFileProvider)
	{
		return new StorageBackupSetup.Default(
			notNull(backupFileProvider) ,
			StorageBackupItemQueue.New()
		);
	}
	
	public final class Default implements StorageBackupSetup
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageBackupFileProvider backupFileProvider;
		private final StorageBackupItemQueue    itemQueue         ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final StorageBackupFileProvider backupFileProvider,
			final StorageBackupItemQueue    itemQueue
		)
		{
			super();
			this.backupFileProvider = backupFileProvider;
			this.itemQueue          = itemQueue         ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final StorageBackupFileProvider backupFileProvider()
		{
			return this.backupFileProvider;
		}
		
		@Override
		public StorageFileWriter.Provider setupWriterProvider(
			final StorageFileWriter.Provider writerProvider
		)
		{
			return StorageFileWriterBackupping.Provider(this.itemQueue, writerProvider);
		}
		
		@Override
		public StorageBackupHandler setupHandler(
			final StorageOperationController operationController,
			final StorageDataFileValidator   validator
		)
		{
			final int channelCount = operationController.channelCountProvider().getChannelCount();
			return StorageBackupHandler.New(
				this               ,
				channelCount       ,
				this.itemQueue     ,
				operationController,
				validator
			);
		}
		
	}
	
}

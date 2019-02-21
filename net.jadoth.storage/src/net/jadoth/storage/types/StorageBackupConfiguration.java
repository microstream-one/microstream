package net.jadoth.storage.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

public interface StorageBackupConfiguration
{
	public String threadName();
	
	public String graveDirectoryName();
	
	public StorageFileProvider backupFileProvider();
	
	
	
	public static StorageBackupConfiguration New(
		final String              threadName        ,
		final String              graveDirectoryName,
		final StorageFileProvider backupFileProvider
	)
	{
		return new StorageBackupConfiguration.Implementation(
			notNull(threadName)        ,
			mayNull(graveDirectoryName),
			notNull(backupFileProvider)
		);
	}
	
	public final class Implementation implements StorageBackupConfiguration
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String              threadName        ;
		private final String              graveDirectoryName;
		private final StorageFileProvider backupFileProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Implementation(
			final String              threadName        ,
			final String              graveDirectoryName,
			final StorageFileProvider backupFileProvider
		)
		{
			super();
			this.threadName         = threadName        ;
			this.graveDirectoryName = graveDirectoryName;
			this.backupFileProvider = backupFileProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String threadName()
		{
			return this.threadName;
		}

		@Override
		public final String graveDirectoryName()
		{
			return this.graveDirectoryName;
		}

		@Override
		public final StorageFileProvider backupFileProvider()
		{
			return this.backupFileProvider;
		}
		
	}
	
}

package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageLockFileSetup
{
	public StorageFileProvider backupFileProvider();

	public String processIdentity();
	
	public long updateInterval();
	
	
	
	public static StorageLockFileSetup New(
		final StorageFileProvider            backupFileProvider     ,
		final StorageProcessIdentityProvider processIdentityProvider,
		final long                           updateInterval
	)
	{
		return new StorageLockFileSetup.Default(
			notNull(backupFileProvider)     ,
			notNull(processIdentityProvider),
			notNull(updateInterval)
		);
	}
	
	public final class Default implements StorageLockFileSetup
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageFileProvider            backupFileProvider     ;
		private final StorageProcessIdentityProvider processIdentityProvider;
		private final long                           updateInterval         ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final StorageFileProvider            backupFileProvider     ,
			final StorageProcessIdentityProvider processIdentityProvider,
			final long                           updateInterval
		)
		{
			super();
			this.backupFileProvider      = backupFileProvider     ;
			this.processIdentityProvider = processIdentityProvider;
			this.updateInterval          = updateInterval         ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final StorageFileProvider backupFileProvider()
		{
			return this.backupFileProvider;
		}

		@Override
		public final String processIdentity()
		{
			return this.processIdentityProvider.provideProcessIdentity();
		}

		@Override
		public final long updateInterval()
		{
			return this.updateInterval;
		}
		
	}
	
}

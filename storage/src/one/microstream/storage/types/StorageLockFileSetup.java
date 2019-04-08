package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageLockFileSetup
{
	public StorageFileProvider lockFileProvider();

	public String processIdentity();
	
	public long updateInterval();
	
	
	
	public static StorageLockFileSetup New(
		final StorageFileProvider            lockFileProvider       ,
		final StorageProcessIdentityProvider processIdentityProvider,
		final long                           updateInterval
	)
	{
		return new StorageLockFileSetup.Default(
			notNull(lockFileProvider)       ,
			notNull(processIdentityProvider),
			notNull(updateInterval)
		);
	}
	
	public final class Default implements StorageLockFileSetup
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageFileProvider            lockFileProvider       ;
		private final StorageProcessIdentityProvider processIdentityProvider;
		private final long                           updateInterval         ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final StorageFileProvider            lockFileProvider       ,
			final StorageProcessIdentityProvider processIdentityProvider,
			final long                           updateInterval
		)
		{
			super();
			this.lockFileProvider        = lockFileProvider       ;
			this.processIdentityProvider = processIdentityProvider;
			this.updateInterval          = updateInterval         ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final StorageFileProvider lockFileProvider()
		{
			return this.lockFileProvider;
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

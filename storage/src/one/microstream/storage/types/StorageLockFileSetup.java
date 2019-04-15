package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.charset.Charset;

import one.microstream.persistence.types.Persistence;
import one.microstream.util.ProcessIdentityProvider;

public interface StorageLockFileSetup
{
	public StorageFileProvider lockFileProvider();
	
	public Charset charset();

	public String processIdentity();
	
	public long updateInterval();
	
	
	public interface Defaults
	{
		public static long defaultUpdateInterval()
		{
			// default of 10 seconds (meaning the lock file content is read, validated and written every 10 seconds)
			return 2_000L;
		}
		
		public static Charset defaultCharset()
		{
			// permanent re-aliasing of the same one and only reasonable CharSet which is UTF-8.
			return Persistence.standardCharset();
		}
	}
	
	
	public static StorageLockFileSetup New(
		final StorageFileProvider            lockFileProvider       ,
		final ProcessIdentityProvider processIdentityProvider,
		final Charset                        charset                ,
		final long                           updateInterval
	)
	{
		return new StorageLockFileSetup.Default(
			notNull(lockFileProvider)       ,
			notNull(processIdentityProvider),
			notNull(charset)                ,
			notNull(updateInterval)
		);
	}
	
	public final class Default implements StorageLockFileSetup
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageFileProvider            lockFileProvider       ;
		private final ProcessIdentityProvider processIdentityProvider;
		private final Charset                        charset                ;
		private final long                           updateInterval         ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final StorageFileProvider            lockFileProvider       ,
			final ProcessIdentityProvider processIdentityProvider,
			final Charset                        charset                ,
			final long                           updateInterval
		)
		{
			super();
			this.lockFileProvider        = lockFileProvider       ;
			this.processIdentityProvider = processIdentityProvider;
			this.charset                 = charset                ;
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
		
		@Override
		public final Charset charset()
		{
			return this.charset;
		}
		
	}
	
}

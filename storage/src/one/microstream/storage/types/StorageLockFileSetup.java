package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

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
		public static Charset defaultCharset()
		{
			// permanent re-aliasing of the same one and only reasonable CharSet which is UTF-8.
			return Persistence.standardCharset();
		}
		
		public static long defaultUpdateInterval()
		{
			// default of 10 seconds (meaning the lock file content is read, validated and written every 10 seconds)
			return 10_000L;
		}
	}
	
	public static StorageLockFileSetup New(
		final StorageFileProvider     lockFileProvider       ,
		final ProcessIdentityProvider processIdentityProvider
	)
	{
		return New(
			lockFileProvider                ,
			processIdentityProvider         ,
			Defaults.defaultCharset()       ,
			Defaults.defaultUpdateInterval()
		);
	}
	
	public static StorageLockFileSetup New(
		final StorageFileProvider     lockFileProvider       ,
		final ProcessIdentityProvider processIdentityProvider,
		final Charset                 charset                ,
		final long                    updateInterval
	)
	{
		return new StorageLockFileSetup.Default(
			notNull(lockFileProvider)       ,
			notNull(processIdentityProvider),
			notNull(charset)                ,
			positive(updateInterval)
		);
	}
	
	public final class Default implements StorageLockFileSetup
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageFileProvider     lockFileProvider       ;
		private final ProcessIdentityProvider processIdentityProvider;
		private final Charset                 charset                ;
		private final long                    updateInterval         ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final StorageFileProvider     lockFileProvider       ,
			final ProcessIdentityProvider processIdentityProvider,
			final Charset                 charset                ,
			final long                    updateInterval
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
	
	
	
	public static StorageLockFileSetup.Provider Provider()
	{
		return Provider(
			StorageLockFileSetup.Defaults.defaultCharset()       ,
			StorageLockFileSetup.Defaults.defaultUpdateInterval()
		);
	}
	
	public static StorageLockFileSetup.Provider Provider(
		final Charset charset
	)
	{
		return Provider(
			charset                                              ,
			StorageLockFileSetup.Defaults.defaultUpdateInterval()
		);
	}
	
	public static StorageLockFileSetup.Provider Provider(
		final long updateInterval
	)
	{
		return Provider(
			StorageLockFileSetup.Defaults.defaultCharset(),
			updateInterval
		);
	}
	
	public static StorageLockFileSetup.Provider Provider(
		final Charset charset       ,
		final long    updateInterval
	)
	{
		return new StorageLockFileSetup.Provider.Default(
			notNull(charset)        ,
			positive(updateInterval)
		);
	}
	
	@FunctionalInterface
	public interface Provider
	{
		public StorageLockFileSetup provideLockFileSetup(StorageFoundation<?> foundation);
		
		public final class Default implements StorageLockFileSetup.Provider
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final Charset charset       ;
			final long    updateInterval;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default(
				final Charset charset       ,
				final long    updateInterval
			)
			{
				super();
				this.charset        = charset       ;
				this.updateInterval = updateInterval;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public StorageLockFileSetup provideLockFileSetup(
				final StorageFoundation<?> foundation
			)
			{
				return StorageLockFileSetup.New(
					foundation.getConfiguration().fileProvider(),
					foundation.getProcessIdentityProvider()     ,
					this.charset                                ,
					this.updateInterval
				);
			}
			
		}
		
	}
	
}

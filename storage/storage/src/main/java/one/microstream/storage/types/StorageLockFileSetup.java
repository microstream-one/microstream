package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import java.nio.charset.Charset;

import one.microstream.persistence.types.Persistence;
import one.microstream.util.ProcessIdentityProvider;

public interface StorageLockFileSetup
{
	public StorageLiveFileProvider lockFileProvider();

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
		final StorageLiveFileProvider     lockFileProvider       ,
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
		final StorageLiveFileProvider     lockFileProvider       ,
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

		private final StorageLiveFileProvider     lockFileProvider       ;
		private final ProcessIdentityProvider processIdentityProvider;
		private final Charset                 charset                ;
		private final long                    updateInterval         ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageLiveFileProvider     lockFileProvider       ,
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
		public final StorageLiveFileProvider lockFileProvider()
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



	/**
	 * Pseudo-constructor method to create a new {@link StorageLockFileSetup.Provider} instance
	 * using default values specified by {@link StorageLockFileSetup.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageLockFileSetup#Provider(Charset, long)}.
	 *
	 * @return {@linkDoc StorageLockFileSetup#Provider(Charset, long)@return}
	 *
	 * @see StorageLockFileSetup
	 */
	public static StorageLockFileSetup.Provider Provider()
	{
		return Provider(
			StorageLockFileSetup.Defaults.defaultCharset()       ,
			StorageLockFileSetup.Defaults.defaultUpdateInterval()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageLockFileSetup.Provider} instance
	 * using the passed values and default values specified by {@link StorageLockFileSetup.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageLockFileSetup#Provider(Charset, long)}.
	 *
	 * @param charset {@linkDoc StorageLockFileSetup#Provider(Charset, long):}
	 *
	 * @return {@linkDoc StorageLockFileSetup#Provider(Charset, long)@return}
	 *
	 * @see StorageLockFileSetup
	 */
	public static StorageLockFileSetup.Provider Provider(
		final Charset charset
	)
	{
		return Provider(
			charset                                              ,
			StorageLockFileSetup.Defaults.defaultUpdateInterval()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageLockFileSetup.Provider} instance
	 * using the passed values and default values specified by {@link StorageLockFileSetup.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageLockFileSetup#Provider(Charset, long)}.
	 *
	 * @param updateInterval {@linkDoc StorageLockFileSetup#Provider(Charset, long):}
	 *
	 * @return {@linkDoc StorageLockFileSetup#Provider(Charset, long)@return}
	 *
	 * @see StorageLockFileSetup
	 */
	public static StorageLockFileSetup.Provider Provider(
		final long updateInterval
	)
	{
		return Provider(
			StorageLockFileSetup.Defaults.defaultCharset(),
			updateInterval
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link Provider} instance
	 * using the passed values.
	 * <p>
	 * A {@link Provider} instance created by this method provides new {@link StorageLockFileSetup}
	 * instances that use the passed {@literal Charset} and {@literal updateInterval}.
	 *
	 * @param charset the {@link Charset} to be used for the lock file content.
	 * @param updateInterval the update interval in ms.
	 *
	 * @return a new {@link Provider} instance.
	 *
	 * @see StorageLockFileSetup
	 */
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
		// (03.06.2019 TM)TODO: every provider should be changed to this pattern of getting the foundation passed
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
